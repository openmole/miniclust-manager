package miniclust.manager.db

/*
 * Copyright (C) 2025 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import slick.*
import slick.dbio.*
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api.*
import gears.async.*
import gears.async.ScalaConverters.*
import gears.async.default.given

import miniclust.manager.Tool.given_ExecutionContext

import java.sql.DriverManager
import java.util.logging.Logger

import better.files.*

case class Upgrade(upgrade: DBIO[Unit], version: Int)

class DB(dbFile: File):
  import DBSchemaV1.*

  val logger = Logger.getLogger(getClass.getName)

  lazy val db: Database =
    DriverManager.registerDriver(new org.h2.Driver())
    Database.forURL(url = s"jdbc:h2:${dbFile.pathAsString}")

  def runTransaction[E <: Effect, T](action: DBIOAction[T, NoStream, E]): T =
    Async.blocking:
      db.run(action.transactionally).asGears.await
  

  object DatabaseInfo:
    case class Data(version: Int)

  class DatabaseInfo(tag: Tag) extends Table[DatabaseInfo.Data](tag, "DB_INFO"):
    def version = column[Int]("VERSION")

    def * = version.mapTo[DatabaseInfo.Data]

  val databaseInfoTable = TableQuery[DatabaseInfo]


  def upgrades: Seq[Upgrade] = Seq(DBSchemaV1.upgrade)

  def initDB() =
    runTransaction:
      def createDBInfo: DBIO[Int] =
        for
          _ <- databaseInfoTable.schema.createIfNotExists
          v <- databaseInfoTable.map(_.version).result
        yield v.headOption.getOrElse(0)

      def updateVersion =
        for
          _ <- databaseInfoTable.delete
          _ <- databaseInfoTable += DatabaseInfo.Data(dbVersion)
        yield ()

      def upgradesValue(version: Int) =
        for
          u <- upgrades.dropWhile(_.version <= version)
          _ = logger.info(s"upgrade db to version ${u.version}")
        yield u.upgrade

      def create =
        for
          v <- createDBInfo
          _ = logger.info(s"found db version $v")
          _ <- if v > dbVersion then DBIO.failed(new RuntimeException(s"Can't downgrade DB (version ${v} to ${dbVersion})")) else DBIO.successful(())
          ups <- DBIO.sequence(upgradesValue(v))
          _ <- updateVersion
        yield ()

      create

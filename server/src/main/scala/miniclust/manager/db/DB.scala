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
import miniclust.manager.db.DBSchemaV1.ManagerUser.Role
import miniclust.manager.{Salt, Tool}

import java.util.UUID

case class Upgrade(upgrade: DBIO[Unit], version: Int)

object DB:
  export DBSchemaV1.*



class DB(dbFile: File):
  import DBSchemaV1.*

  val logger = Logger.getLogger(getClass.getName)

  lazy val db: Database =
    DriverManager.registerDriver(new org.h2.Driver())
    Database.forURL(url = s"jdbc:h2:${dbFile.pathAsString}")

  def runTransaction[E <: Effect, T](action: DBIOAction[T, NoStream, E]): T =
    Async.blocking:
      db.run(action.transactionally).asGears.await

  def lastId: Option[String] =
    runTransaction:
      accountingTable.sortBy(_.id.desc).map(_.id).result.headOption

  def addAccounting(a: Accounting) =
    runTransaction:
      accountingTable += a

  def salted(password: Password)(using salt: Salt) = Tool.hash(password, salt.value)

  def userWithDefault(name: String, firstName: String, email: String, password: Password, institution: Institution, role: Role = Role.Manager, id: String = Tool.randomUUID) =
    ManagerUser(
      id = id,
      name = name,
      firstName = firstName,
      email = email,
      password = password,
      institution = institution,
      created = Tool.now,
      role = role)


  def autenticate(email: Email, password: Password)(using salt: Salt) =
    val user: Option[ManagerUser] =
      runTransaction:
        managerUserTable.filter(u => u.email === email).result.headOption

    user match
      case None => Left("No user found")
      case Some(u) =>
        if u.password == Tool.hash(password, salt.value)
        then Right(u)
        else Left("Wrong password")

  def addUser(user: DB.ManagerUser) =
    runTransaction:
      managerUserTable += user

  def user(id: String) =
    runTransaction:
      managerUserTable.filter(_.id === id).result.headOption

  def miniClustUser(login: String): Option[DB.MiniClustUser]  =
    runTransaction:
      miniClustUserTable.filter(_.login === login).result.headOption
//  def getOrCreateUser(login: String): ManagerUser =
//    runTransaction:
//      val insert =
//        val u =
//          ManagerUser(
//            name = null,
//            firstName = null,
//            login = login,
//            email = null,
//            emailStatus = EmailStatus.Unchecked,
//            institution = null,
//            created = Tool.now
//          )
//        for
//          _ <- managerUserTable += u
//        yield u
//
//      for
//        u <- managerUserTable.filter(u => u.login === login).result
//        ru <- if u.isEmpty then insert else DBIO.successful(u.head)
//      yield ru
//
//  def addRegisterUser(user: MiniClustUser) =
//    runTransaction:
//      registerUserTable += user

  object DatabaseInfo:
    case class Data(version: Int)

  class DatabaseInfo(tag: Tag) extends Table[DatabaseInfo.Data](tag, "DB_INFO"):
    def version = column[Int]("VERSION")
    def * = version.mapTo[DatabaseInfo.Data]

  val databaseInfoTable = TableQuery[DatabaseInfo]

  def upgrades: Seq[Upgrade] = Seq(DBSchemaV1.upgrade)

  def initDB()(using Salt) =
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

    runTransaction:
      val admin = userWithDefault("Admin", "Admin", "admin@miniclust.org", salted("admin"), "OpenMOLE", role = Role.Admin)
      for
        e <- managerUserTable.result
        _ <- if e.isEmpty then managerUserTable += admin else DBIO.successful(())
      yield ()

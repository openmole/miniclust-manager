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
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api.*
import miniclust.manager.Tool

object DBSchemaV1:
  def dbVersion = 1

  case class Accounting(
    id: String,
    duration: Long,
    bucket: String,
    key: String
  )

  class AccountingTable(tag: Tag) extends Table[Accounting](tag, "ACCOUNTING"):
    def duration = column[Long]("DURATION")
    def id = column[String]("ID", O.PrimaryKey)
    def bucket = column[String]("BUCKET")
    def key = column[String]("KEY")

    def * = (id, duration, bucket, key).mapTo[Accounting]

  val accountingTable = TableQuery[AccountingTable]

  type Email = String
  type Institution = String
  type Password = String

  object ManagerUser:
    object Role:
      given BaseColumnType[Role] = MappedColumnType.base[Role, Int](
        s => s.ordinal,
        v => Role.fromOrdinal(v)
      )

    enum Role:
      case Admin, Manager

  case class ManagerUser(
    id: String,
    name: String,
    firstName: String,
    email: Email,
    password: Password,
    institution: Institution,
    created: Long,
    role: ManagerUser.Role)

  class ManagerUserTable(tag: Tag) extends Table[ManagerUser](tag, "MANAGER_USER"):
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def firstName = column[String]("FIRST_NAME")
    def email = column[String]("EMAIL")
    def password = column[Password]("PASSWORD")
    def institution = column[String]("INSTITUTION")
    def created = column[Long]("CREATED")
    def role = column[ManagerUser.Role]("ROLE")

    def * = (id, name, firstName, email, password, institution, created, role).mapTo[ManagerUser]

  val managerUserTable = TableQuery[ManagerUserTable]

  case class MiniClustUser(
    login: String,
    name: String,
    firstName: String,
    email: Email,
    institution: Institution)

  class MiniClustUserTable(tag: Tag) extends Table[MiniClustUser](tag, "MINICLUST_USER"):
    def login = column[String]("LOGIN", O.PrimaryKey)
    def name = column[String]("NAME")
    def firstName = column[String]("FIRST_NAME")
    def email = column[String]("EMAIL")
    def institution = column[String]("INSTITUTION")

    def * = (login, name, firstName, email, institution).mapTo[MiniClustUser]

  val miniClustUserTable = TableQuery[MiniClustUserTable]


  def upgrade =
    val schema = accountingTable.schema ++ managerUserTable.schema ++ miniClustUserTable.schema
    def upgrade = schema.createIfNotExists
    Upgrade(upgrade = upgrade, version = dbVersion)


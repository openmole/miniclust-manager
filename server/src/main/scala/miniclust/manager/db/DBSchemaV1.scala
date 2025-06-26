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

  object EmailStatus:
    def hasBeenChecked(s: EmailStatus) = s == EmailStatus.Checked

  enum EmailStatus:
    case Unchecked, Checked

  given BaseColumnType[EmailStatus] = MappedColumnType.base[EmailStatus, Int](
    s => s.ordinal,
    v => EmailStatus.fromOrdinal(v)
  )

  case class User(
    id: String,
    name: String,
    firstName: String,
    login: String,
    email: Email,
    institution: Institution,
    emailStatus: EmailStatus,
    created: Long)

  class UserTable(tag: Tag) extends Table[User](tag, "USER"):
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def firstName = column[String]("FIRST_NAME")
    def login = column[String]("LOGIN")
    def email = column[String]("EMAIL")
    def institution = column[String]("INSTITUTION")
    def emailStatus = column[EmailStatus]("EMAIL_STATUS")
    def created = column[Long]("CREATED")

    def * = (id, name, firstName, login, email, institution, emailStatus, created).mapTo[User]

  val userTable = TableQuery[UserTable]

  case class RegisterUser(
    id: String,
    name: String,
    firstName: String,
    login: String,
    email: Email,
    password: Password,
    institution: Institution,
    created: Long = Tool.now,
    emailStatus: EmailStatus = EmailStatus.Unchecked)

  class RegisterUserTable(tag: Tag) extends Table[RegisterUser](tag, "REGISTER_USER"):
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def firstName = column[String]("FIRST_NAME")
    def login = column[String]("LOGIN")
    def email = column[String]("EMAIL")
    def password = column[String]("PASSWORD")
    def institution = column[String]("INSTITUTION")
    def created = column[Long]("CREATED")
    def emailStatus = column[Int]("EMAIL_STATUS")

    def * = (id, name, firstName, login, email, password, institution, created, emailStatus).mapTo[RegisterUser]

  val registerUserTable = TableQuery[RegisterUserTable]

  //  given BaseColumnType[EmailStatus] = MappedColumnType.base[EmailStatus, Int] (
//    s => s.ordinal,
//    v => EmailStatus.fromOrdinal(v)
//  )
//
//
//  given BaseColumnType[Role] = MappedColumnType.base[Role, Int] (
//    s => s.ordinal,
//    v => Role.fromOrdinal(v)
//  )
//
//
//  given BaseColumnType[UserStatus] = MappedColumnType.base[UserStatus, Int](
//    s => s.ordinal,
//    v => UserStatus.fromOrdinal(v)
//  )
//
//  case class User(
//                   name: String,
//                   firstName: String,
//                   email: Email,
//                   emailStatus: Data.EmailStatus,
//                   password: Password,
//                   institution: Institution,
//                   omVersion: Version,
//                   memory: Memory,
//                   cpu: Double,
//                   openMOLEMemory: Memory,
//                   lastAccess: Long,
//                   created: Long,
//                   role: Data.Role = Role.User,
//                   status: Data.UserStatus = Data.UserStatus.Active,
//                   uuid: UUID = randomUUID)
//
//  case class RegisterUser(
//                           name: String,
//                           firstName: String,
//                           email: Email,
//                           password: Password,
//                           institution: Institution,
//                           created: Long = now,
//                           emailStatus: Data.EmailStatus = Data.EmailStatus.Unchecked,
//                           uuid: UUID = DB.randomUUID)
//
//  class Users(tag: Tag) extends Table[User](tag, "USERS"):
//    def uuid = column[UUID]("UUID", O.PrimaryKey)
//    def name = column[String]("NAME")
//    def firstName = column[String]("FIRST_NAME")
//
//    def email = column[Email]("EMAIL", O.Unique)
//    def emailStatus = column[Data.EmailStatus]("EMAIL_STATUS")
//
//    def password = column[Password]("PASSWORD")
//    def institution = column[Institution]("INSTITUTION")
//    def role = column[Role]("ROLE")
//    def status = column[UserStatus]("STATUS")
//    def memory = column[Storage]("MEMORY_LIMIT")
//    def cpu = column[Double]("CPU_LIMIT")
//    def omMemory = column[Storage]("OPENMOLE_MEMORY")
//
//    def omVersion = column[Version]("OPENMOLE_VERSION")
//
//    def lastAccess = column[Long]("LAST_ACCESS")
//    def created = column[Long]("CREATED")
//
//    def * = (name, firstName, email, emailStatus, password, institution, omVersion, memory, cpu, omMemory, lastAccess, created, role, status, uuid).mapTo[User]
//    def mailIndex = index("index_mail", email, unique = true)
//
//  val userTable = TableQuery[Users]
//
//  class RegisterUsers(tag: Tag) extends Table[RegisterUser](tag, "REGISTERING_USERS"):
//    def uuid = column[UUID]("UUID", O.PrimaryKey)
//    def name = column[String]("NAME")
//    def firstName = column[String]("FIRST_NAME")
//    def email = column[Email]("EMAIL", O.Unique)
//    def emailStatus = column[Data.EmailStatus]("EMAIL_STATUS")
//    def password = column[Password]("PASSWORD")
//    def institution = column[Institution]("INSTITUTION")
//    def created = column[Long]("CREATED")
//
//    def * = (name, firstName, email, password, institution, created, emailStatus, uuid).mapTo[RegisterUser]
//
//  val registerUserTable = TableQuery[RegisterUsers]
//
//  case class ValidationSecret(
//                               uuid: UUID,
//                               secret: Secret)
//
//  class ValidationSecrets(tag: Tag) extends Table[ValidationSecret](tag, "VALIDATION_SECRETS"):
//    def uuid = column[UUID]("UUID", O.PrimaryKey)
//    def validationSecret = column[DB.Secret]("VALIDATION_SECRET")
//
//    def * = (uuid, validationSecret).mapTo[ValidationSecret]
//
//  val validationSecretTable = TableQuery[ValidationSecrets]
//
//  object DatabaseInfo:
//    case class Data(version: Int)
//
//  class DatabaseInfo(tag: Tag) extends Table[DatabaseInfo.Data](tag, "DB_INFO"):
//    def version = column[Int]("VERSION")
//
//    def * = (version).mapTo[DatabaseInfo.Data]
//
//  val databaseInfoTable = TableQuery[DatabaseInfo]
//
//  def upgrade =
//    val schema = validationSecretTable.schema.createIfNotExists
//
//    val modif =
//      sqlu"""
//        INSERT INTO VALIDATION_SECRETS (UUID, VALIDATION_SECRET)
//          SELECT UUID, VALIDATION_SECRET FROM REGISTERING_USERS;
//
//        ALTER TABLE REGISTERING_USERS DROP COLUMN VALIDATION_SECRET;"""
//
//    Upgrade(
//      upgrade = DBIO.seq(schema, modif),
//      version = dbVersion
//    )


  def upgrade =
    val schema = accountingTable.schema ++ registerUserTable.schema ++ userTable.schema
    def upgrade = schema.createIfNotExists
    Upgrade(upgrade = upgrade, version = dbVersion)


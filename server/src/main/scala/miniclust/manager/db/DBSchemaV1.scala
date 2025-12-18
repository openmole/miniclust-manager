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


  object AccountingJob:
    extension (j: AccountingJob) 
      def job = miniclust.message.MiniClust.Accounting.Job.parse(j.jobJson)

  case class AccountingJob(
    id: String,
    time: Long,
    duration: Long,
    bucket: String,
    nodeId: String,
    jobJson: String                  
  )

  class AccountingJobTable(tag: Tag) extends Table[AccountingJob](tag, "ACCOUNTING_JOB"):
    def duration = column[Long]("DURATION")
    def time = column[Long]("TIME")
    def id = column[String]("ID", O.PrimaryKey)
    def bucket = column[String]("BUCKET")
    def nodeId = column[String]("NODE_ID")
    def jobJSON = column[String]("JOB_JSON")

    def * = (id, time, duration, bucket, nodeId, jobJSON).mapTo[AccountingJob]

  val accountingJobTable = TableQuery[AccountingJobTable]

  object AccountingWorker:
    case class MiniClust(version: String, build: Long)
    case class Usage(cores: Int, availableSpace: Long, availableMemory: Long, load: Double)
    case class NodeInfo(id: String, ip: String, key: String, hostname: Option[String], cores: Int, machineCores: Int, space: Long, memory: Long)


  case class AccountingWorker(
    id: String,
    time: Long,
    nodeInfo: AccountingWorker.NodeInfo,
    miniclust: AccountingWorker.MiniClust,
    usage: AccountingWorker.Usage)


  class AccountingWorkerTable(tag: Tag) extends Table[AccountingWorker](tag, "ACCOUNTING_WORKER"):
    def id = column[String]("ID", O.PrimaryKey)
    def time = column[Long]("TIME")
    def nodeId = column[String]("NODE_ID")
    def nodeIP = column[String]("NODE_IP")
    def nodeKey = column[String]("NODE_KEY")
    def nodeHostName = column[Option[String]]("NODE_HOST_NAME")
    def nodeCores = column[Int]("NODE_CORES")
    def nodeMachineCores = column[Int]("NODE_MACHINE_CORES")
    def nodeSpace = column[Long]("NODE_SPACE")
    def nodeMemory = column[Long]("NODE_MEMORY")
    def miniclustVersion = column[String]("MINICLUST_VERSION")
    def miniclustBuild = column[Long]("MINICLUST_BUILD")
    def usageCores = column[Int]("USAGE_CORES")
    def usageAvailableMemory = column[Long]("USAGE_AVAILABLE_MEMORY")
    def usageAvailableSpace = column[Long]("USAGE_AVAILABLE_SPACE")
    def usageLoad = column[Double]("USAGE_LOAD")

    def * =
      (
        id,
        time,
        (nodeId, nodeIP, nodeKey, nodeHostName, nodeCores, nodeMachineCores, nodeSpace, nodeMemory).mapTo[AccountingWorker.NodeInfo],
        (miniclustVersion, miniclustBuild).mapTo[AccountingWorker.MiniClust],
        (usageCores, usageAvailableSpace, usageAvailableMemory, usageLoad).mapTo[AccountingWorker.Usage]
      ).mapTo[AccountingWorker]

  val accountingWorkerTable = TableQuery[AccountingWorkerTable]

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
    val schema = accountingJobTable.schema ++ accountingWorkerTable.schema ++  managerUserTable.schema ++ miniClustUserTable.schema
    def upgrade = schema.createIfNotExists
    Upgrade(upgrade = upgrade, version = dbVersion)


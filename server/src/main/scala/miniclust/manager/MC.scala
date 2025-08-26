package miniclust.manager

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

import miniclust.message.Minio
import better.files.*
import io.circe.derivation

object MC:
  given omrCirceDefault: io.circe.derivation.Configuration =
    io.circe.derivation.Configuration.default.withDefaults

  object UserInfo:
    object UserStatus:
      import io.circe.*

      given Decoder[UserStatus] =
        Decoder.instance(v => v.as[String].map(v => UserStatus.valueOf(v)))

    enum UserStatus derives derivation.ConfiguredCodec:
      case enabled, disabled

    case class MemberOf(name: String, policies: Seq[String]) derives derivation.ConfiguredCodec

  case class UserInfo(accessKey: String, policyName: Option[String], userStatus: UserInfo.UserStatus, memberOf: Seq[UserInfo.MemberOf] = Seq.empty) derives derivation.ConfiguredCodec:
    def enabled = userStatus == UserInfo.UserStatus.enabled

  enum AuthenticationResult:
    case Success(userInfo: UserInfo)
    case Disabled(userInfo: UserInfo)
    case Failed

  def createAlias(minio: Minio, directory: File) =
    import sys.process.*
    s"mc --config-dir ${directory} alias set server --quiet ${minio.server.url} ${minio.server.user} ${minio.server.password}".!!

  //     def listGroup(minio: Minio, group: String) = withTmpDirectory(): dir =>
  //       import sys.process.*
  //       createAlias(minio, dir)
  //       val json = s"mc admin group info --config-dir ${dir} babar-admin ${group} --json".!!
  //       ujson.read(json)("members").arr.map(_.str).toSeq

  //     def isUser(minio: Minio, login: String) = listGroup(minio, "user").contains(login)
  //     def isAdmin(minio: Minio, login: String) = listGroup(minio, "admin").contains(login)

  def userInfo(minio: Minio, login: String): UserInfo = Tool.withTmpDirectory(): dir =>
    import io.circe.generic.auto.*
    import io.circe.parser.*

    import sys.process.*
    createAlias(minio, dir)
    val json = s"mc --config-dir ${dir} admin user info server ${login} --json".!!
    parse(json).toTry.get.as[UserInfo].toTry.get

  def userList(minio: Minio): Seq[UserInfo] = Tool.withTmpDirectory(): dir =>
    import io.circe.generic.auto.*
    import io.circe.parser.*
    import scala.jdk.CollectionConverters.*

    import sys.process.*
    createAlias(minio, dir)

    val json = s"mc --config-dir ${dir} admin user list server --json".!!

    json.lines().toList.asScala.map: l =>
      parse(l).toTry.get.as[UserInfo].toTry.get
    .toSeq

  def authenticate(minio: Minio, key: String, password: String): AuthenticationResult =
    import scala.util.*

    def correctPassword =
      try
        val m2 = minio.copy(server = minio.server.copy(user = key, password = password))
        miniclust.message.Minio.withClient(m2): client =>
          client.listBuckets()
        true
      catch
        case e: software.amazon.awssdk.services.s3.model.S3Exception =>
          val code = e.awsErrorDetails().errorCode()
          if code == "InvalidAccessKeyId" || code == "SignatureDoesNotMatch"
          then false
          else true

    Try(userInfo(minio, key)) match
      case Success(info) =>
        if info.userStatus == UserInfo.UserStatus.disabled
        then AuthenticationResult.Disabled(info)
        else if correctPassword
        then AuthenticationResult.Success(info)
        else AuthenticationResult.Failed
      case Failure(_) => AuthenticationResult.Failed



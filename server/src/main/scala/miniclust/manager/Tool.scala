package miniclust.manager

import miniclust.message.*

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import better.files.*

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
 
 object Tool:
   given ExecutionContext = ExecutionContext.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())
   def now = System.currentTimeMillis()

   def withTmpDirectory[R]()(f: File => R) =
     val dir = File.newTemporaryDirectory()
     try f(dir)
     finally dir.delete(true)

   object mc:

     object UserInfo:
       object UserStatus:
         import io.circe.*

         given Decoder[UserStatus] =
           Decoder.instance(v => v.as[String].map(v => UserStatus.valueOf(v)))

       enum UserStatus:
         case enabled, disabled

       case class MemberOf(name: String, policies: Seq[String])

     case class UserInfo(policyName: Option[String], userStatus: UserInfo.UserStatus, memberOf: Seq[UserInfo.MemberOf]):
       def enabled = userStatus == UserInfo.UserStatus.enabled

     enum AuthenticationResult:
       case Success(userInfo: UserInfo)
       case Disabled(userInfo: UserInfo)
       case Failed

     def createAlias(minio: Minio, directory: File) =
       import sys.process.*
       s"mc alias set server --quiet --config-dir ${directory} ${minio.server.url} ${minio.server.user} ${minio.server.password}".!!

//     def listGroup(minio: Minio, group: String) = withTmpDirectory(): dir =>
//       import sys.process.*
//       createAlias(minio, dir)
//       val json = s"mc admin group info --config-dir ${dir} babar-admin ${group} --json".!!
//       ujson.read(json)("members").arr.map(_.str).toSeq

//     def isUser(minio: Minio, login: String) = listGroup(minio, "user").contains(login)
//     def isAdmin(minio: Minio, login: String) = listGroup(minio, "admin").contains(login)

     def userInfo(minio: Minio, login: String): UserInfo =withTmpDirectory(): dir =>
       import io.circe.generic.auto.*
       import io.circe.parser.*

       import sys.process.*
       createAlias(minio, dir)
       val json = s"mc admin user info --config-dir ${dir} babar-admin ${login} --json".!!
       parse(json).toTry.get.as[UserInfo].toTry.get

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





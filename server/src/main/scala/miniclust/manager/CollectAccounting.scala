package miniclust.manager

import miniclust.message.*
import miniclust.manager.db.*
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import scala.jdk.CollectionConverters.*

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

object CollectAccounting:

  def collect(minio: Minio, bucket: Minio.Bucket, db: DB) =
    def dirPrefix = s"${MiniClust.Coordination.accountingDirectory}/"
    def last = db.lastId.map(id => s"$dirPrefix$id")

    Minio.listAndApply(minio, bucket, MiniClust.Coordination.accountingDirectory, maxKeys = Some(100), startAfter = last): o =>
      val content = Minio.content(minio, bucket, o.name)
      val job = MiniClust.JobResourceUsage.parse(content)
      val a = DB.Accounting(o.name.drop(dirPrefix.length), job.second, job.bucket, job.nodeInfo.key)
      db.addAccounting(a)
      println(a)
//          Minio.content(minio, bucket, k)
//          val objectIdentifiers = keys.map(k => ObjectIdentifier.builder().key(k).build())
//          delete(minio, bucket, keys.toSeq *)


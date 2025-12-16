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

import io.github.arainko.ducktape.*
import com.github.f4b6a3.ulid.*
import miniclust.message.*
import miniclust.manager.db.*
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request

import java.util.logging.Logger
import scala.jdk.CollectionConverters.*

object CollectAccounting:

  val logger = Logger.getLogger(getClass.getName)

  def collectWorkerActivity(minio: Minio, bucket: Minio.Bucket, db: DB, old: Option[Long]) =
    def dirPrefix = MiniClust.Coordination.workerAccountingDirectory

    def last =
      old.map: old =>
        MiniClust.Coordination.workerAccountingDirectory + "/" + UlidCreator.getUlid(System.currentTimeMillis - old * 1000).toLowerCase.substring(0, Ulid.TIME_CHARS)

    Minio.listAndApply(minio, bucket, MiniClust.Coordination.workerAccountingDirectory + "/", startAfter = last): o =>
      val id = o.name.drop(MiniClust.Coordination.workerAccountingDirectory.length + 1)
      if !db.workerAccountingExists(id)
      then
        val content = Minio.content(minio, bucket, o.name)

        val worker: DBSchemaV1.AccountingWorker =
          MiniClust.Accounting.Worker.parse(content)
            .into[DBSchemaV1.AccountingWorker]
            .transform(
              Field.const(_.id, id), //.const(_.time, o.lastModified.get))
              Field.const(_.time, o.lastModified.get)
            )

       // logger.info("Insert worker activity: " + worker)
        db.addWorkerAccounting(worker)
//          Minio.content(minio, bucket, k)
//          val objectIdentifiers = keys.map(k => ObjectIdentifier.builder().key(k).build())
//          delete(minio, bucket, keys.toSeq *)
      else logger.info("skip worker activity: " + id)

/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen.db

import auth.UserId
import dmscreen.*
import io.getquill.*
import just.semver.SemVer
import zio.json.*
import zio.json.ast.Json

// a object that wraps a business concept with extra stuff that's needed in the database (such as deleted, createdDate, etc)
case class DBObject[T <: DMScreenEntity[?, ?, ?]](
  value:   T,
  deleted: Boolean = false
)

given MappedEncoding[Json, String] = MappedEncoding[Json, String](_.toJson)

given MappedEncoding[String, Json] =
  MappedEncoding[String, Json](s =>
    Json.decoder
      .decodeJson(s).fold(
        msg => throw new RuntimeException(msg),
        identity
      )
  )

given MappedEncoding[Long, CampaignId] = MappedEncoding[Long, CampaignId](CampaignId.apply)

given MappedEncoding[CampaignId, Long] = MappedEncoding[CampaignId, Long](_.value)

given MappedEncoding[Long, UserId] = MappedEncoding[Long, UserId](UserId.apply)

given MappedEncoding[UserId, Long] = MappedEncoding[UserId, Long](_.value)

given MappedEncoding[SemVer, String] = MappedEncoding[SemVer, String](_.toString)

given MappedEncoding[String, SemVer] =
  MappedEncoding[String, SemVer](SemVer.parse(_).getOrElse(SemVer.unsafeParse("0.0.0")))

val jsonInsert = quote {
  (
    doc:   Json,
    path:  String,
    value: Json
  ) =>
    sql"JSON_INSERT($doc, $path, $value)".as[Json]
}
val jsonReplace = quote {
  (
    doc:   Json,
    path:  String,
    value: Json
  ) =>
    sql"JSON_REPLACE($doc, $path, $value)".as[Json]
}
val jsonRemove = quote {
  (
    doc:  Json,
    path: String
  ) =>
    sql"JSON_REMOVE($doc, $path)".as[Json]
}

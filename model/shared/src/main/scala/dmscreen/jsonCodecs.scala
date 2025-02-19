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

package dmscreen

import zio.json.{JsonCodec, JsonDecoder}
import auth.given

given JsonCodec[CampaignId] = JsonCodec.long.transform(CampaignId.apply, _.value)
given JsonCodec[CampaignInfo] = JsonCodec.derived[CampaignInfo]
given JsonCodec[DMScreenSession] = JsonCodec.derived[DMScreenSession]

extension (json: zio.json.ast.Json.Obj) {

  def getEither[T: JsonDecoder](name: String): Either[String, T] =
    json.fields.find(_._1 == name) match {
      case Some(value) => value._2.as[T]
      case None        => Left(s"Missing field '$name'")
    }

  def getEitherOption[T: JsonDecoder](name: String): Either[String, Option[T]] =
    json.fields.find(_._1 == name) match {
      case Some(value) => value._2.as[Option[T]]
      case None        => Right(None)
    }

  def getOption[T: JsonDecoder](name: String): Option[T] =
    json.fields.find(_._1 == name) match {
      case Some(value) => value._2.as[T].toOption
      case None        => None
    }

}

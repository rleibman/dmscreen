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

import just.semver.SemVer
import zio.{IO, ZIO}
import zio.json.ast.Json
import zio.json.{JsonDecoder, JsonEncoder}

trait HasId[Id] {

  def id: Id

}

trait EntityType[EntityId] {

  def name:               String
  def createId(id: Long): EntityId

}

trait DMScreenEntity[Id, Header <: HasId[Id], Info: {JsonEncoder, JsonDecoder}] {

  def entityType: EntityType[Id]
  def version:    SemVer
  def header:     Header
  def jsonInfo:   Json
  final def id = header.id
  lazy val info: Info = jsonInfo
    .as[Info].fold(
      e => throw DMScreenError(e), // This should never be an error, if it is, something major has gone wrong
      identity
    )

}

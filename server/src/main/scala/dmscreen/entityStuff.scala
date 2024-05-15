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

import zio.json.JsonCodec
import zio.json.ast.Json

// An attempt to abstract everything so that we can use the same code for all games.

opaque type EntityId = Long

case class EntityType(
  name: String
)

case class EntityHeader(
  id:         EntityId,
  entityType: EntityType,
  name:       String
)

case class Entity(entityHeader: EntityHeader) {}

enum EntityValueType {

  case Id, String, Number, Boolean, Array, Object

}

case class EntityMetadata(
  entityType: EntityType,
  name:       String,
  path:       String,
  isOptional: Boolean,
  valueType:  EntityValueType
)

trait EntityValue[+Type] {

  def value: Type

}

case class StringValue(value: String) extends EntityValue[String] {}
case class NumberValue(value: Number) extends EntityValue[Number] {}
case class BooleanValue(value: Boolean) extends EntityValue[Boolean] {}
case class ObjectValue[Type: JsonCodec](value: Type) extends EntityValue[Type] {}
case class ArrayValue[Type](value: Seq[Type]) extends EntityValue[Seq[Type]] {}

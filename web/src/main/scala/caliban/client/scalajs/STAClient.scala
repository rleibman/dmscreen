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

package caliban.client.scalajs

import caliban.client.*
import caliban.client.CalibanClientError.DecodingError
import caliban.client.FieldBuilder.*
import caliban.client.__Value.*

object STAClient {

  sealed trait EncounterStatus extends scala.Product with scala.Serializable { def value: String }
  object EncounterStatus {

    case object active extends EncounterStatus { val value: String = "active" }
    case object archived extends EncounterStatus { val value: String = "archived" }
    case object planned extends EncounterStatus { val value: String = "planned" }

    implicit val decoder: ScalarDecoder[EncounterStatus] = {
      case __StringValue("active")   => Right(EncounterStatus.active)
      case __StringValue("archived") => Right(EncounterStatus.archived)
      case __StringValue("planned")  => Right(EncounterStatus.planned)
      case other                     => Left(DecodingError(s"Can't build EncounterStatus from input $other"))
    }
    implicit val encoder: ArgEncoder[EncounterStatus] = {
      case EncounterStatus.active   => __EnumValue("active")
      case EncounterStatus.archived => __EnumValue("archived")
      case EncounterStatus.planned  => __EnumValue("planned")
    }

    val values: scala.collection.immutable.Vector[EncounterStatus] =
      scala.collection.immutable.Vector(active, archived, planned)

  }

  type Add
  object Add {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Add, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Add, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Character
  object Character {

    def header[A](innerSelection: SelectionBuilder[CharacterHeader, A]): SelectionBuilder[Character, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Character, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Character, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type CharacterHeader
  object CharacterHeader {

    def id: SelectionBuilder[CharacterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[CharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[CharacterHeader, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("name", OptionOf(Scalar()))
    def playerName: SelectionBuilder[CharacterHeader, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("playerName", OptionOf(Scalar()))

  }

  type Copy
  object Copy {

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type Encounter
  object Encounter {

    def header[A](innerSelection: SelectionBuilder[EncounterHeader, A]): SelectionBuilder[Encounter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Encounter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Encounter, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type EncounterHeader
  object EncounterHeader {

    def id: SelectionBuilder[EncounterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[EncounterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[EncounterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def status: SelectionBuilder[EncounterHeader, EncounterStatus] =
      _root_.caliban.client.SelectionBuilder.Field("status", Scalar())
    def sceneId: SelectionBuilder[EncounterHeader, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("sceneId", OptionOf(Scalar()))
    def orderCol: SelectionBuilder[EncounterHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("orderCol", Scalar())

  }

  type JsonPath
  object JsonPath {

    def value: SelectionBuilder[JsonPath, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Move
  object Move {

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Move, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Move, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type NonPlayerCharacter
  object NonPlayerCharacter {

    def header[A](innerSelection: SelectionBuilder[NonPlayerCharacterHeader, A])
      : SelectionBuilder[NonPlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[NonPlayerCharacter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[NonPlayerCharacter, String] =
      _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type NonPlayerCharacterHeader
  object NonPlayerCharacterHeader {

    def id: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[NonPlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def isActive: SelectionBuilder[NonPlayerCharacterHeader, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("isActive", Scalar())

  }

  type Remove
  object Remove {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Remove, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))

  }

  type Replace
  object Replace {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Replace, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Replace, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Scene
  object Scene {

    def header[A](innerSelection: SelectionBuilder[SceneHeader, A]): SelectionBuilder[Scene, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Scene, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Scene, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type SceneHeader
  object SceneHeader {

    def id: SelectionBuilder[SceneHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[SceneHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[SceneHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def orderCol: SelectionBuilder[SceneHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("orderCol", Scalar())
    def isActive: SelectionBuilder[SceneHeader, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("isActive", Scalar())

  }

  type Starship
  object Starship {

    def header[A](innerSelection: SelectionBuilder[StarshipHeader, A]): SelectionBuilder[Starship, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Starship, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Starship, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type StarshipHeader
  object StarshipHeader {

    def id: SelectionBuilder[StarshipHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[StarshipHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[StarshipHeader, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("name", OptionOf(Scalar()))
    def playerName: SelectionBuilder[StarshipHeader, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("playerName", OptionOf(Scalar()))

  }

  type Test
  object Test {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Test, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Test, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  final case class CharacterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       scala.Option[String] = None,
    playerName: scala.Option[String] = None
  )
  object CharacterHeaderInput {

    implicit val encoder: ArgEncoder[CharacterHeaderInput] = new ArgEncoder[CharacterHeaderInput] {
      override def encode(value: CharacterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name" -> value.name.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[String]].encode(value)),
            "playerName" -> value.playerName.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            )
          )
        )
    }

  }
  final case class EncounterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String,
    status:     EncounterStatus,
    sceneId:    scala.Option[Long] = None,
    orderCol:   Int
  )
  object EncounterHeaderInput {

    implicit val encoder: ArgEncoder[EncounterHeaderInput] = new ArgEncoder[EncounterHeaderInput] {
      override def encode(value: EncounterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name"       -> implicitly[ArgEncoder[String]].encode(value.name),
            "status"     -> implicitly[ArgEncoder[EncounterStatus]].encode(value.status),
            "sceneId"  -> value.sceneId.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[Long]].encode(value)),
            "orderCol" -> implicitly[ArgEncoder[Int]].encode(value.orderCol)
          )
        )
    }

  }
  final case class NonPlayerCharacterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String,
    isActive:   Boolean
  )
  object NonPlayerCharacterHeaderInput {

    implicit val encoder: ArgEncoder[NonPlayerCharacterHeaderInput] = new ArgEncoder[NonPlayerCharacterHeaderInput] {
      override def encode(value: NonPlayerCharacterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name"       -> implicitly[ArgEncoder[String]].encode(value.name),
            "isActive"   -> implicitly[ArgEncoder[Boolean]].encode(value.isActive)
          )
        )
    }

  }
  final case class SceneHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String,
    orderCol:   Int,
    isActive:   Boolean
  )
  object SceneHeaderInput {

    implicit val encoder: ArgEncoder[SceneHeaderInput] = new ArgEncoder[SceneHeaderInput] {
      override def encode(value: SceneHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name"       -> implicitly[ArgEncoder[String]].encode(value.name),
            "orderCol"   -> implicitly[ArgEncoder[Int]].encode(value.orderCol),
            "isActive"   -> implicitly[ArgEncoder[Boolean]].encode(value.isActive)
          )
        )
    }

  }
  final case class StarshipHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       scala.Option[String] = None,
    playerName: scala.Option[String] = None
  )
  object StarshipHeaderInput {

    implicit val encoder: ArgEncoder[StarshipHeaderInput] = new ArgEncoder[StarshipHeaderInput] {
      override def encode(value: StarshipHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name" -> value.name.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[String]].encode(value)),
            "playerName" -> value.playerName.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            )
          )
        )
    }

  }
  type Queries = _root_.caliban.client.Operations.RootQuery
  object Queries {

    def characters[A](value: Long)(innerSelection: SelectionBuilder[Character, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "characters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def ships[A](value: Long)(innerSelection: SelectionBuilder[Starship, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "ships",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def scenes[A](value: Long)(innerSelection: SelectionBuilder[Scene, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "scenes",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def nonPlayerCharacters[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[NonPlayerCharacter, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "nonPlayerCharacters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def encounters[A](value: Long)(innerSelection: SelectionBuilder[Encounter, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "encounters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )

  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {

    def upsertCharacter(
      header:   CharacterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[CharacterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertCharacter",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "CharacterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def upsertStarship(
      header:   StarshipHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[StarshipHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertStarship",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "StarshipHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def upsertScene(
      header:   SceneHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[SceneHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertScene",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "SceneHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def upsertNonPlayerCharacter(
      header:   NonPlayerCharacterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[NonPlayerCharacterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertNonPlayerCharacter",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "NonPlayerCharacterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def upsertEncounter(
      header:   EncounterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[EncounterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertEncounter",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "EncounterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def deleteEntity(
      entityType: String,
      id:         Long,
      softDelete: Boolean
    )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[Boolean]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "deleteEntity",
        OptionOf(Scalar()),
        arguments = List(
          Argument("entityType", entityType, "String!")(encoder0),
          Argument("id", id, "Long!")(encoder1),
          Argument("softDelete", softDelete, "Boolean!")(encoder2)
        )
      )

  }

  type Subscriptions = _root_.caliban.client.Operations.RootSubscription
  object Subscriptions {

    def campaignStream[A](
      entityType: String,
      id:         Long,
      events:     List[zio.json.ast.Json] = Nil
    )(
      onAdd:     SelectionBuilder[Add, A],
      onCopy:    SelectionBuilder[Copy, A],
      onMove:    SelectionBuilder[Move, A],
      onRemove:  SelectionBuilder[Remove, A],
      onReplace: SelectionBuilder[Replace, A],
      onTest:    SelectionBuilder[Test, A]
    )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[List[zio.json.ast.Json]]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootSubscription, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "campaignStream",
        OptionOf(
          ChoiceOf(
            Map(
              "Add"     -> Obj(onAdd),
              "Copy"    -> Obj(onCopy),
              "Move"    -> Obj(onMove),
              "Remove"  -> Obj(onRemove),
              "Replace" -> Obj(onReplace),
              "Test"    -> Obj(onTest)
            )
          )
        ),
        arguments = List(
          Argument("entityType", entityType, "String!")(encoder0),
          Argument("id", id, "Long!")(encoder1),
          Argument("events", events, "[Json!]!")(encoder2)
        )
      )

  }

}

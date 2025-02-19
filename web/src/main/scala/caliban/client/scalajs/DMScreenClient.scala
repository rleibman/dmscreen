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

import caliban.client.CalibanClientError.DecodingError
import caliban.client.FieldBuilder._
import caliban.client._
import caliban.client.__Value._

object DMScreenClient {

  sealed trait CampaignStatus extends scala.Product with scala.Serializable { def value: String }
  object CampaignStatus {

    case object active extends CampaignStatus { val value: String = "active" }
    case object archived extends CampaignStatus { val value: String = "archived" }

    implicit val decoder: ScalarDecoder[CampaignStatus] = {
      case __StringValue("active")   => Right(CampaignStatus.active)
      case __StringValue("archived") => Right(CampaignStatus.archived)
      case other                     => Left(DecodingError(s"Can't build CampaignStatus from input $other"))
    }
    implicit val encoder: ArgEncoder[CampaignStatus] = {
      case CampaignStatus.active   => __EnumValue("active")
      case CampaignStatus.archived => __EnumValue("archived")
    }

    val values: scala.collection.immutable.Vector[CampaignStatus] = scala.collection.immutable.Vector(active, archived)

  }

  sealed trait GameSystem extends scala.Product with scala.Serializable { def value: String }
  object GameSystem {

    case object callOfCthulhu extends GameSystem { val value: String = "callOfCthulhu" }
    case object dnd3_5 extends GameSystem { val value: String = "dnd3_5" }
    case object dnd4e extends GameSystem { val value: String = "dnd4e" }
    case object dnd5e extends GameSystem { val value: String = "dnd5e" }
    case object fateAccelerated extends GameSystem { val value: String = "fateAccelerated" }
    case object fateCore extends GameSystem { val value: String = "fateCore" }
    case object pathfinder1e extends GameSystem { val value: String = "pathfinder1e" }
    case object pathfinder2e extends GameSystem { val value: String = "pathfinder2e" }
    case object savageWorlds extends GameSystem { val value: String = "savageWorlds" }
    case object starTrekAdventures extends GameSystem { val value: String = "starTrekAdventures" }
    case object starfinder extends GameSystem { val value: String = "starfinder" }

    implicit val decoder: ScalarDecoder[GameSystem] = {
      case __StringValue("callOfCthulhu")      => Right(GameSystem.callOfCthulhu)
      case __StringValue("dnd3_5")             => Right(GameSystem.dnd3_5)
      case __StringValue("dnd4e")              => Right(GameSystem.dnd4e)
      case __StringValue("dnd5e")              => Right(GameSystem.dnd5e)
      case __StringValue("fateAccelerated")    => Right(GameSystem.fateAccelerated)
      case __StringValue("fateCore")           => Right(GameSystem.fateCore)
      case __StringValue("pathfinder1e")       => Right(GameSystem.pathfinder1e)
      case __StringValue("pathfinder2e")       => Right(GameSystem.pathfinder2e)
      case __StringValue("savageWorlds")       => Right(GameSystem.savageWorlds)
      case __StringValue("starTrekAdventures") => Right(GameSystem.starTrekAdventures)
      case __StringValue("starfinder")         => Right(GameSystem.starfinder)
      case other                               => Left(DecodingError(s"Can't build GameSystem from input $other"))
    }
    implicit val encoder: ArgEncoder[GameSystem] = {
      case GameSystem.callOfCthulhu      => __EnumValue("callOfCthulhu")
      case GameSystem.dnd3_5             => __EnumValue("dnd3_5")
      case GameSystem.dnd4e              => __EnumValue("dnd4e")
      case GameSystem.dnd5e              => __EnumValue("dnd5e")
      case GameSystem.fateAccelerated    => __EnumValue("fateAccelerated")
      case GameSystem.fateCore           => __EnumValue("fateCore")
      case GameSystem.pathfinder1e       => __EnumValue("pathfinder1e")
      case GameSystem.pathfinder2e       => __EnumValue("pathfinder2e")
      case GameSystem.savageWorlds       => __EnumValue("savageWorlds")
      case GameSystem.starTrekAdventures => __EnumValue("starTrekAdventures")
      case GameSystem.starfinder         => __EnumValue("starfinder")
    }

    val values: scala.collection.immutable.Vector[GameSystem] = scala.collection.immutable.Vector(
      callOfCthulhu,
      dnd3_5,
      dnd4e,
      dnd5e,
      fateAccelerated,
      fateCore,
      pathfinder1e,
      pathfinder2e,
      savageWorlds,
      starTrekAdventures,
      starfinder
    )

  }

  type Add
  object Add {

    final case class AddView[PathSelection](
      path:  PathSelection,
      value: zio.json.ast.Json
    )

    type ViewSelection[PathSelection] = SelectionBuilder[Add, AddView[PathSelection]]

    def view[PathSelection](pathSelection: SelectionBuilder[JsonPath, PathSelection]): ViewSelection[PathSelection] =
      (path(pathSelection) ~ value).map { case (path, value) => AddView(path, value) }

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Add, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Add, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Campaign
  object Campaign {

    final case class CampaignView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[Campaign, CampaignView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[CampaignHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        CampaignView(header, jsonInfo, version)
      }

    def header[A](innerSelection: SelectionBuilder[CampaignHeader, A]): SelectionBuilder[Campaign, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Campaign, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Campaign, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type CampaignHeader
  object CampaignHeader {

    final case class CampaignHeaderView(
      id:             Long,
      dmUserId:       Long,
      name:           String,
      gameSystem:     GameSystem,
      campaignStatus: CampaignStatus
    )

    type ViewSelection = SelectionBuilder[CampaignHeader, CampaignHeaderView]

    def view: ViewSelection =
      (id ~ dmUserId ~ name ~ gameSystem ~ campaignStatus).map {
        case (id, dmUserId, name, gameSystem, campaignStatus) =>
          CampaignHeaderView(id, dmUserId, name, gameSystem, campaignStatus)
      }

    def id: SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def dmUserId: SelectionBuilder[CampaignHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("dmUserId", Scalar())
    def name: SelectionBuilder[CampaignHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def gameSystem: SelectionBuilder[CampaignHeader, GameSystem] =
      _root_.caliban.client.SelectionBuilder.Field("gameSystem", Scalar())
    def campaignStatus: SelectionBuilder[CampaignHeader, CampaignStatus] =
      _root_.caliban.client.SelectionBuilder.Field("campaignStatus", Scalar())

  }

  type CampaignLogEntry
  object CampaignLogEntry {

    final case class CampaignLogEntryView(
      campaignId: Long,
      message:    String,
      timestamp:  java.time.LocalDateTime
    )

    type ViewSelection = SelectionBuilder[CampaignLogEntry, CampaignLogEntryView]

    def view: ViewSelection =
      (campaignId ~ message ~ timestamp).map { case (campaignId, message, timestamp) =>
        CampaignLogEntryView(campaignId, message, timestamp)
      }

    def campaignId: SelectionBuilder[CampaignLogEntry, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def message: SelectionBuilder[CampaignLogEntry, String] =
      _root_.caliban.client.SelectionBuilder.Field("message", Scalar())
    def timestamp: SelectionBuilder[CampaignLogEntry, java.time.LocalDateTime] =
      _root_.caliban.client.SelectionBuilder.Field("timestamp", Scalar())

  }

  type Copy
  object Copy {

    final case class CopyView[FromSelection, ToSelection](
      from: FromSelection,
      to:   ToSelection
    )

    type ViewSelection[FromSelection, ToSelection] = SelectionBuilder[Copy, CopyView[FromSelection, ToSelection]]

    def view[FromSelection, ToSelection](
      fromSelection: SelectionBuilder[JsonPath, FromSelection],
      toSelection:   SelectionBuilder[JsonPath, ToSelection]
    ): ViewSelection[FromSelection, ToSelection] =
      (from(fromSelection) ~ to(toSelection)).map { case (from, to) => CopyView(from, to) }

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type JsonPath
  object JsonPath {

    final case class JsonPathView(value: String)

    type ViewSelection = SelectionBuilder[JsonPath, JsonPathView]

    def view: ViewSelection = value.map(value => JsonPathView(value))

    def value: SelectionBuilder[JsonPath, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Move
  object Move {

    final case class MoveView[FromSelection, ToSelection](
      from: FromSelection,
      to:   ToSelection
    )

    type ViewSelection[FromSelection, ToSelection] = SelectionBuilder[Move, MoveView[FromSelection, ToSelection]]

    def view[FromSelection, ToSelection](
      fromSelection: SelectionBuilder[JsonPath, FromSelection],
      toSelection:   SelectionBuilder[JsonPath, ToSelection]
    ): ViewSelection[FromSelection, ToSelection] =
      (from(fromSelection) ~ to(toSelection)).map { case (from, to) => MoveView(from, to) }

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Move, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Move, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type Remove
  object Remove {

    final case class RemoveView[PathSelection](path: PathSelection)

    type ViewSelection[PathSelection] = SelectionBuilder[Remove, RemoveView[PathSelection]]

    def view[PathSelection](pathSelection: SelectionBuilder[JsonPath, PathSelection]): ViewSelection[PathSelection] =
      path(pathSelection).map(path => RemoveView(path))

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Remove, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))

  }

  type Replace
  object Replace {

    final case class ReplaceView[PathSelection](
      path:  PathSelection,
      value: zio.json.ast.Json
    )

    type ViewSelection[PathSelection] = SelectionBuilder[Replace, ReplaceView[PathSelection]]

    def view[PathSelection](pathSelection: SelectionBuilder[JsonPath, PathSelection]): ViewSelection[PathSelection] =
      (path(pathSelection) ~ value).map { case (path, value) => ReplaceView(path, value) }

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Replace, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Replace, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Test
  object Test {

    final case class TestView[PathSelection](
      path:  PathSelection,
      value: zio.json.ast.Json
    )

    type ViewSelection[PathSelection] = SelectionBuilder[Test, TestView[PathSelection]]

    def view[PathSelection](pathSelection: SelectionBuilder[JsonPath, PathSelection]): ViewSelection[PathSelection] =
      (path(pathSelection) ~ value).map { case (path, value) => TestView(path, value) }

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Test, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Test, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  final case class CampaignHeaderInput(
    id:             Long,
    dmUserId:       Long,
    name:           String,
    gameSystem:     GameSystem,
    campaignStatus: CampaignStatus
  )
  object CampaignHeaderInput {

    implicit val encoder: ArgEncoder[CampaignHeaderInput] = new ArgEncoder[CampaignHeaderInput] {
      override def encode(value: CampaignHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"             -> implicitly[ArgEncoder[Long]].encode(value.id),
            "dmUserId"       -> implicitly[ArgEncoder[Long]].encode(value.dmUserId),
            "name"           -> implicitly[ArgEncoder[String]].encode(value.name),
            "gameSystem"     -> implicitly[ArgEncoder[GameSystem]].encode(value.gameSystem),
            "campaignStatus" -> implicitly[ArgEncoder[CampaignStatus]].encode(value.campaignStatus)
          )
        )
    }

  }
  type Queries = _root_.caliban.client.Operations.RootQuery
  object Queries {

    def campaigns[A](innerSelection: SelectionBuilder[CampaignHeader, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("campaigns", OptionOf(ListOf(Obj(innerSelection))))
    def campaign[A](value: Long)(innerSelection: SelectionBuilder[Campaign, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder
        .Field(
          "campaign",
          OptionOf(Obj(innerSelection)),
          arguments = List(Argument("value", value, "Long!")(encoder0))
        )
    def campaignLogs[A](
      campaignId: Long,
      maxNum:     Int
    )(
      innerSelection: SelectionBuilder[CampaignLogEntry, A]
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Int]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "campaignLogs",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments =
          List(Argument("campaignId", campaignId, "Long!")(encoder0), Argument("maxNum", maxNum, "Int!")(encoder1))
      )

  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {

    def upsertCampaign(
      header:   CampaignHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[CampaignHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertCampaign",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "CampaignHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def campaignLog(
      campaignId: Long,
      message:    String
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "campaignLog",
        OptionOf(Scalar()),
        arguments = List(
          Argument("campaignId", campaignId, "Long!")(encoder0),
          Argument("message", message, "String!")(encoder1)
        )
      )
    def deleteCampaign(
      id:         Long,
      softDelete: Boolean
    )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[Boolean]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "deleteCampaign",
        OptionOf(Scalar()),
        arguments = List(
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

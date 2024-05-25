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

object DND5eClient {

  sealed trait Alignment extends scala.Product with scala.Serializable { def value: String }
  object Alignment {

    case object ChaoticEvil extends Alignment { val value: String = "ChaoticEvil" }
    case object ChaoticGood extends Alignment { val value: String = "ChaoticGood" }
    case object ChaoticNeutral extends Alignment { val value: String = "ChaoticNeutral" }
    case object LawfulEvil extends Alignment { val value: String = "LawfulEvil" }
    case object LawfulGood extends Alignment { val value: String = "LawfulGood" }
    case object LawfulNeutral extends Alignment { val value: String = "LawfulNeutral" }
    case object NeutralEvil extends Alignment { val value: String = "NeutralEvil" }
    case object NeutralGood extends Alignment { val value: String = "NeutralGood" }
    case object TrueNeutral extends Alignment { val value: String = "TrueNeutral" }

    implicit val decoder: ScalarDecoder[Alignment] = {
      case __StringValue("ChaoticEvil")    => Right(Alignment.ChaoticEvil)
      case __StringValue("ChaoticGood")    => Right(Alignment.ChaoticGood)
      case __StringValue("ChaoticNeutral") => Right(Alignment.ChaoticNeutral)
      case __StringValue("LawfulEvil")     => Right(Alignment.LawfulEvil)
      case __StringValue("LawfulGood")     => Right(Alignment.LawfulGood)
      case __StringValue("LawfulNeutral")  => Right(Alignment.LawfulNeutral)
      case __StringValue("NeutralEvil")    => Right(Alignment.NeutralEvil)
      case __StringValue("NeutralGood")    => Right(Alignment.NeutralGood)
      case __StringValue("TrueNeutral")    => Right(Alignment.TrueNeutral)
      case other                           => Left(DecodingError(s"Can't build Alignment from input $other"))
    }
    implicit val encoder: ArgEncoder[Alignment] = {
      case Alignment.ChaoticEvil    => __EnumValue("ChaoticEvil")
      case Alignment.ChaoticGood    => __EnumValue("ChaoticGood")
      case Alignment.ChaoticNeutral => __EnumValue("ChaoticNeutral")
      case Alignment.LawfulEvil     => __EnumValue("LawfulEvil")
      case Alignment.LawfulGood     => __EnumValue("LawfulGood")
      case Alignment.LawfulNeutral  => __EnumValue("LawfulNeutral")
      case Alignment.NeutralEvil    => __EnumValue("NeutralEvil")
      case Alignment.NeutralGood    => __EnumValue("NeutralGood")
      case Alignment.TrueNeutral    => __EnumValue("TrueNeutral")
    }

    val values: scala.collection.immutable.Vector[Alignment] = scala.collection.immutable.Vector(
      ChaoticEvil,
      ChaoticGood,
      ChaoticNeutral,
      LawfulEvil,
      LawfulGood,
      LawfulNeutral,
      NeutralEvil,
      NeutralGood,
      TrueNeutral
    )

  }

  sealed trait Biome extends scala.Product with scala.Serializable { def value: String }
  object Biome {

    case object Arctic extends Biome { val value: String = "Arctic" }
    case object Coastal extends Biome { val value: String = "Coastal" }
    case object Desert extends Biome { val value: String = "Desert" }
    case object Forest extends Biome { val value: String = "Forest" }
    case object Grassland extends Biome { val value: String = "Grassland" }
    case object Hill extends Biome { val value: String = "Hill" }
    case object Mountain extends Biome { val value: String = "Mountain" }
    case object Swamp extends Biome { val value: String = "Swamp" }
    case object Underdark extends Biome { val value: String = "Underdark" }
    case object Underwater extends Biome { val value: String = "Underwater" }
    case object Urban extends Biome { val value: String = "Urban" }

    implicit val decoder: ScalarDecoder[Biome] = {
      case __StringValue("Arctic")     => Right(Biome.Arctic)
      case __StringValue("Coastal")    => Right(Biome.Coastal)
      case __StringValue("Desert")     => Right(Biome.Desert)
      case __StringValue("Forest")     => Right(Biome.Forest)
      case __StringValue("Grassland")  => Right(Biome.Grassland)
      case __StringValue("Hill")       => Right(Biome.Hill)
      case __StringValue("Mountain")   => Right(Biome.Mountain)
      case __StringValue("Swamp")      => Right(Biome.Swamp)
      case __StringValue("Underdark")  => Right(Biome.Underdark)
      case __StringValue("Underwater") => Right(Biome.Underwater)
      case __StringValue("Urban")      => Right(Biome.Urban)
      case other                       => Left(DecodingError(s"Can't build Biome from input $other"))
    }
    implicit val encoder: ArgEncoder[Biome] = {
      case Biome.Arctic     => __EnumValue("Arctic")
      case Biome.Coastal    => __EnumValue("Coastal")
      case Biome.Desert     => __EnumValue("Desert")
      case Biome.Forest     => __EnumValue("Forest")
      case Biome.Grassland  => __EnumValue("Grassland")
      case Biome.Hill       => __EnumValue("Hill")
      case Biome.Mountain   => __EnumValue("Mountain")
      case Biome.Swamp      => __EnumValue("Swamp")
      case Biome.Underdark  => __EnumValue("Underdark")
      case Biome.Underwater => __EnumValue("Underwater")
      case Biome.Urban      => __EnumValue("Urban")
    }

    val values: scala.collection.immutable.Vector[Biome] = scala.collection.immutable
      .Vector(Arctic, Coastal, Desert, Forest, Grassland, Hill, Mountain, Swamp, Underdark, Underwater, Urban)

  }

  sealed trait CreatureSize extends scala.Product with scala.Serializable { def value: String }
  object CreatureSize {

    case object Gargantuan extends CreatureSize { val value: String = "Gargantuan" }
    case object Huge extends CreatureSize { val value: String = "Huge" }
    case object Large extends CreatureSize { val value: String = "Large" }
    case object Medium extends CreatureSize { val value: String = "Medium" }
    case object Small extends CreatureSize { val value: String = "Small" }
    case object Tiny extends CreatureSize { val value: String = "Tiny" }

    implicit val decoder: ScalarDecoder[CreatureSize] = {
      case __StringValue("Gargantuan") => Right(CreatureSize.Gargantuan)
      case __StringValue("Huge")       => Right(CreatureSize.Huge)
      case __StringValue("Large")      => Right(CreatureSize.Large)
      case __StringValue("Medium")     => Right(CreatureSize.Medium)
      case __StringValue("Small")      => Right(CreatureSize.Small)
      case __StringValue("Tiny")       => Right(CreatureSize.Tiny)
      case other                       => Left(DecodingError(s"Can't build CreatureSize from input $other"))
    }
    implicit val encoder: ArgEncoder[CreatureSize] = {
      case CreatureSize.Gargantuan => __EnumValue("Gargantuan")
      case CreatureSize.Huge       => __EnumValue("Huge")
      case CreatureSize.Large      => __EnumValue("Large")
      case CreatureSize.Medium     => __EnumValue("Medium")
      case CreatureSize.Small      => __EnumValue("Small")
      case CreatureSize.Tiny       => __EnumValue("Tiny")
    }

    val values: scala.collection.immutable.Vector[CreatureSize] =
      scala.collection.immutable.Vector(Gargantuan, Huge, Large, Medium, Small, Tiny)

  }

  sealed trait DND5eEntityType extends scala.Product with scala.Serializable { def value: String }
  object DND5eEntityType {

    case object campaign extends DND5eEntityType { val value: String = "campaign" }
    case object encounter extends DND5eEntityType { val value: String = "encounter" }
    case object monster extends DND5eEntityType { val value: String = "monster" }
    case object nonPlayerCharacter extends DND5eEntityType { val value: String = "nonPlayerCharacter" }
    case object playerCharacter extends DND5eEntityType { val value: String = "playerCharacter" }
    case object scene extends DND5eEntityType { val value: String = "scene" }
    case object spell extends DND5eEntityType { val value: String = "spell" }

    implicit val decoder: ScalarDecoder[DND5eEntityType] = {
      case __StringValue("campaign")           => Right(DND5eEntityType.campaign)
      case __StringValue("encounter")          => Right(DND5eEntityType.encounter)
      case __StringValue("monster")            => Right(DND5eEntityType.monster)
      case __StringValue("nonPlayerCharacter") => Right(DND5eEntityType.nonPlayerCharacter)
      case __StringValue("playerCharacter")    => Right(DND5eEntityType.playerCharacter)
      case __StringValue("scene")              => Right(DND5eEntityType.scene)
      case __StringValue("spell")              => Right(DND5eEntityType.spell)
      case other                               => Left(DecodingError(s"Can't build DND5eEntityType from input $other"))
    }
    implicit val encoder: ArgEncoder[DND5eEntityType] = {
      case DND5eEntityType.campaign           => __EnumValue("campaign")
      case DND5eEntityType.encounter          => __EnumValue("encounter")
      case DND5eEntityType.monster            => __EnumValue("monster")
      case DND5eEntityType.nonPlayerCharacter => __EnumValue("nonPlayerCharacter")
      case DND5eEntityType.playerCharacter    => __EnumValue("playerCharacter")
      case DND5eEntityType.scene              => __EnumValue("scene")
      case DND5eEntityType.spell              => __EnumValue("spell")
    }

    val values: scala.collection.immutable.Vector[DND5eEntityType] =
      scala.collection.immutable.Vector(campaign, encounter, monster, nonPlayerCharacter, playerCharacter, scene, spell)

  }

  sealed trait GameSystem extends scala.Product with scala.Serializable { def value: String }
  object GameSystem {

    case object dnd5e extends GameSystem { val value: String = "dnd5e" }
    case object pathfinder2e extends GameSystem { val value: String = "pathfinder2e" }
    case object starTrekAdventures extends GameSystem { val value: String = "starTrekAdventures" }

    implicit val decoder: ScalarDecoder[GameSystem] = {
      case __StringValue("dnd5e")              => Right(GameSystem.dnd5e)
      case __StringValue("pathfinder2e")       => Right(GameSystem.pathfinder2e)
      case __StringValue("starTrekAdventures") => Right(GameSystem.starTrekAdventures)
      case other                               => Left(DecodingError(s"Can't build GameSystem from input $other"))
    }
    implicit val encoder: ArgEncoder[GameSystem] = {
      case GameSystem.dnd5e              => __EnumValue("dnd5e")
      case GameSystem.pathfinder2e       => __EnumValue("pathfinder2e")
      case GameSystem.starTrekAdventures => __EnumValue("starTrekAdventures")
    }

    val values: scala.collection.immutable.Vector[GameSystem] =
      scala.collection.immutable.Vector(dnd5e, pathfinder2e, starTrekAdventures)

  }

  sealed trait MonsterSearchOrder extends scala.Product with scala.Serializable { def value: String }
  object MonsterSearchOrder {

    case object alignment extends MonsterSearchOrder { val value: String = "alignment" }
    case object challengeRating extends MonsterSearchOrder { val value: String = "challengeRating" }
    case object environment extends MonsterSearchOrder { val value: String = "environment" }
    case object monsterType extends MonsterSearchOrder { val value: String = "monsterType" }
    case object name extends MonsterSearchOrder { val value: String = "name" }
    case object size extends MonsterSearchOrder { val value: String = "size" }
    case object source extends MonsterSearchOrder { val value: String = "source" }

    implicit val decoder: ScalarDecoder[MonsterSearchOrder] = {
      case __StringValue("alignment")       => Right(MonsterSearchOrder.alignment)
      case __StringValue("challengeRating") => Right(MonsterSearchOrder.challengeRating)
      case __StringValue("environment")     => Right(MonsterSearchOrder.environment)
      case __StringValue("monsterType")     => Right(MonsterSearchOrder.monsterType)
      case __StringValue("name")            => Right(MonsterSearchOrder.name)
      case __StringValue("size")            => Right(MonsterSearchOrder.size)
      case __StringValue("source")          => Right(MonsterSearchOrder.source)
      case other                            => Left(DecodingError(s"Can't build MonsterSearchOrder from input $other"))
    }
    implicit val encoder: ArgEncoder[MonsterSearchOrder] = {
      case MonsterSearchOrder.alignment       => __EnumValue("alignment")
      case MonsterSearchOrder.challengeRating => __EnumValue("challengeRating")
      case MonsterSearchOrder.environment     => __EnumValue("environment")
      case MonsterSearchOrder.monsterType     => __EnumValue("monsterType")
      case MonsterSearchOrder.name            => __EnumValue("name")
      case MonsterSearchOrder.size            => __EnumValue("size")
      case MonsterSearchOrder.source          => __EnumValue("source")
    }

    val values: scala.collection.immutable.Vector[MonsterSearchOrder] =
      scala.collection.immutable.Vector(alignment, challengeRating, environment, monsterType, name, size, source)

  }

  sealed trait MonsterType extends scala.Product with scala.Serializable { def value: String }
  object MonsterType {

    case object Aberration extends MonsterType { val value: String = "Aberration" }
    case object Beast extends MonsterType { val value: String = "Beast" }
    case object Celestial extends MonsterType { val value: String = "Celestial" }
    case object Construct extends MonsterType { val value: String = "Construct" }
    case object Dragon extends MonsterType { val value: String = "Dragon" }
    case object Elemental extends MonsterType { val value: String = "Elemental" }
    case object Fey extends MonsterType { val value: String = "Fey" }
    case object Fiend extends MonsterType { val value: String = "Fiend" }
    case object Giant extends MonsterType { val value: String = "Giant" }
    case object Humanoid extends MonsterType { val value: String = "Humanoid" }
    case object Monstrosity extends MonsterType { val value: String = "Monstrosity" }
    case object Ooze extends MonsterType { val value: String = "Ooze" }
    case object Plant extends MonsterType { val value: String = "Plant" }
    case object Undead extends MonsterType { val value: String = "Undead" }

    implicit val decoder: ScalarDecoder[MonsterType] = {
      case __StringValue("Aberration")  => Right(MonsterType.Aberration)
      case __StringValue("Beast")       => Right(MonsterType.Beast)
      case __StringValue("Celestial")   => Right(MonsterType.Celestial)
      case __StringValue("Construct")   => Right(MonsterType.Construct)
      case __StringValue("Dragon")      => Right(MonsterType.Dragon)
      case __StringValue("Elemental")   => Right(MonsterType.Elemental)
      case __StringValue("Fey")         => Right(MonsterType.Fey)
      case __StringValue("Fiend")       => Right(MonsterType.Fiend)
      case __StringValue("Giant")       => Right(MonsterType.Giant)
      case __StringValue("Humanoid")    => Right(MonsterType.Humanoid)
      case __StringValue("Monstrosity") => Right(MonsterType.Monstrosity)
      case __StringValue("Ooze")        => Right(MonsterType.Ooze)
      case __StringValue("Plant")       => Right(MonsterType.Plant)
      case __StringValue("Undead")      => Right(MonsterType.Undead)
      case other                        => Left(DecodingError(s"Can't build MonsterType from input $other"))
    }
    implicit val encoder: ArgEncoder[MonsterType] = {
      case MonsterType.Aberration  => __EnumValue("Aberration")
      case MonsterType.Beast       => __EnumValue("Beast")
      case MonsterType.Celestial   => __EnumValue("Celestial")
      case MonsterType.Construct   => __EnumValue("Construct")
      case MonsterType.Dragon      => __EnumValue("Dragon")
      case MonsterType.Elemental   => __EnumValue("Elemental")
      case MonsterType.Fey         => __EnumValue("Fey")
      case MonsterType.Fiend       => __EnumValue("Fiend")
      case MonsterType.Giant       => __EnumValue("Giant")
      case MonsterType.Humanoid    => __EnumValue("Humanoid")
      case MonsterType.Monstrosity => __EnumValue("Monstrosity")
      case MonsterType.Ooze        => __EnumValue("Ooze")
      case MonsterType.Plant       => __EnumValue("Plant")
      case MonsterType.Undead      => __EnumValue("Undead")
    }

    val values: scala.collection.immutable.Vector[MonsterType] = scala.collection.immutable.Vector(
      Aberration,
      Beast,
      Celestial,
      Construct,
      Dragon,
      Elemental,
      Fey,
      Fiend,
      Giant,
      Humanoid,
      Monstrosity,
      Ooze,
      Plant,
      Undead
    )

  }

  sealed trait OrderDirection extends scala.Product with scala.Serializable { def value: String }
  object OrderDirection {

    case object asc extends OrderDirection { val value: String = "asc" }
    case object desc extends OrderDirection { val value: String = "desc" }

    implicit val decoder: ScalarDecoder[OrderDirection] = {
      case __StringValue("asc")  => Right(OrderDirection.asc)
      case __StringValue("desc") => Right(OrderDirection.desc)
      case other                 => Left(DecodingError(s"Can't build OrderDirection from input $other"))
    }
    implicit val encoder: ArgEncoder[OrderDirection] = {
      case OrderDirection.asc  => __EnumValue("asc")
      case OrderDirection.desc => __EnumValue("desc")
    }

    val values: scala.collection.immutable.Vector[OrderDirection] = scala.collection.immutable.Vector(asc, desc)

  }

  type Add
  object Add {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Add, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Add, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Background
  object Background {

    def name: SelectionBuilder[Background, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type CampaignHeader
  object CampaignHeader {

    def id:   SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def dm:   SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("dm", Scalar())
    def name: SelectionBuilder[CampaignHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def gameSystem: SelectionBuilder[CampaignHeader, GameSystem] =
      _root_.caliban.client.SelectionBuilder.Field("gameSystem", Scalar())

  }

  type CharacterClass
  object CharacterClass {

    def id: SelectionBuilder[CharacterClass, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def hitDice: SelectionBuilder[CharacterClass, String] =
      _root_.caliban.client.SelectionBuilder.Field("hitDice", Scalar())

  }

  type Copy
  object Copy {

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type DND5eCampaign
  object DND5eCampaign {

    def header[A](innerSelection: SelectionBuilder[CampaignHeader, A]): SelectionBuilder[DND5eCampaign, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[DND5eCampaign, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())

  }

  type Encounter
  object Encounter {

    def header[A](innerSelection: SelectionBuilder[EncounterHeader, A]): SelectionBuilder[Encounter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Encounter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())

  }

  type EncounterHeader
  object EncounterHeader {

    def id: SelectionBuilder[EncounterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[EncounterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[EncounterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type JsonPath
  object JsonPath {

    def value: SelectionBuilder[JsonPath, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Monster
  object Monster {

    def header[A](innerSelection: SelectionBuilder[MonsterHeader, A]): SelectionBuilder[Monster, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Monster, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())

  }

  type MonsterHeader
  object MonsterHeader {

    def id:   SelectionBuilder[MonsterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def name: SelectionBuilder[MonsterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def monsterType: SelectionBuilder[MonsterHeader, MonsterType] =
      _root_.caliban.client.SelectionBuilder.Field("monsterType", Scalar())
    def biome: SelectionBuilder[MonsterHeader, scala.Option[Biome]] =
      _root_.caliban.client.SelectionBuilder.Field("biome", OptionOf(Scalar()))
    def alignment: SelectionBuilder[MonsterHeader, scala.Option[Alignment]] =
      _root_.caliban.client.SelectionBuilder.Field("alignment", OptionOf(Scalar()))
    def cr: SelectionBuilder[MonsterHeader, Double] = _root_.caliban.client.SelectionBuilder.Field("cr", Scalar())
    def xp: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("xp", Scalar())
    def ac: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("ac", Scalar())
    def hp: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("hp", Scalar())
    def size: SelectionBuilder[MonsterHeader, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())

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

  }

  type NonPlayerCharacterHeader
  object NonPlayerCharacterHeader {

    def id: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[NonPlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type PlayerCharacter
  object PlayerCharacter {

    def header[A](innerSelection: SelectionBuilder[PlayerCharacterHeader, A]): SelectionBuilder[PlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[PlayerCharacter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())

  }

  type PlayerCharacterHeader
  object PlayerCharacterHeader {

    def id: SelectionBuilder[PlayerCharacterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[PlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[PlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def playerName: SelectionBuilder[PlayerCharacterHeader, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("playerName", OptionOf(Scalar()))

  }

  type Race
  object Race {

    def name: SelectionBuilder[Race, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

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

  type Source
  object Source {

    def name: SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def id:   SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def url: SelectionBuilder[Source, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("url", OptionOf(Scalar()))

  }

  type Subclass
  object Subclass {

    def name: SelectionBuilder[Subclass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Test
  object Test {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Test, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Test, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  final case class SourceInput(
    name: String,
    id:   String,
    url:  scala.Option[String] = None
  )
  object SourceInput {

    implicit val encoder: ArgEncoder[SourceInput] = new ArgEncoder[SourceInput] {
      override def encode(value: SourceInput): __Value =
        __ObjectValue(
          List(
            "name" -> implicitly[ArgEncoder[String]].encode(value.name),
            "id"   -> implicitly[ArgEncoder[String]].encode(value.id),
            "url"  -> value.url.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[String]].encode(value))
          )
        )
    }

  }
  type Queries = _root_.caliban.client.Operations.RootQuery
  object Queries {

    def campaigns[A](innerSelection: SelectionBuilder[CampaignHeader, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("campaigns", OptionOf(ListOf(Obj(innerSelection))))
    def campaign[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[DND5eCampaign, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder
        .Field("campaign", OptionOf(Obj(innerSelection)), arguments = List(Argument("value", value, "Long!")(encoder0)))
    def playerCharacters[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[PlayerCharacter, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "playerCharacters",
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
    def encounters[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[EncounterHeader, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "encounters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def encounter[A](value: Long)(innerSelection: SelectionBuilder[Encounter, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "encounter",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def bestiary[A](
      name:            scala.Option[String] = None,
      challengeRating: scala.Option[Double] = None,
      size:            scala.Option[String] = None,
      alignment:       scala.Option[String] = None,
      environment:     scala.Option[String] = None,
      monsterType:     scala.Option[MonsterType] = None,
      source:          scala.Option[SourceInput] = None,
      order:           MonsterSearchOrder,
      orderDir:        OrderDirection,
      page:            Int,
      pageSize:        Int
    )(
      innerSelection: SelectionBuilder[Monster, A]
    )(implicit
      encoder0:  ArgEncoder[scala.Option[String]],
      encoder1:  ArgEncoder[scala.Option[Double]],
      encoder2:  ArgEncoder[scala.Option[String]],
      encoder3:  ArgEncoder[scala.Option[String]],
      encoder4:  ArgEncoder[scala.Option[String]],
      encoder5:  ArgEncoder[scala.Option[MonsterType]],
      encoder6:  ArgEncoder[scala.Option[SourceInput]],
      encoder7:  ArgEncoder[MonsterSearchOrder],
      encoder8:  ArgEncoder[OrderDirection],
      encoder9:  ArgEncoder[Int],
      encoder10: ArgEncoder[Int]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "bestiary",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(
          Argument("name", name, "String")(encoder0),
          Argument("challengeRating", challengeRating, "Float")(encoder1),
          Argument("size", size, "String")(encoder2),
          Argument("alignment", alignment, "String")(encoder3),
          Argument("environment", environment, "String")(encoder4),
          Argument("monsterType", monsterType, "MonsterType")(encoder5),
          Argument("source", source, "SourceInput")(encoder6),
          Argument("order", order, "MonsterSearchOrder!")(encoder7),
          Argument("orderDir", orderDir, "OrderDirection!")(encoder8),
          Argument("page", page, "Int!")(encoder9),
          Argument("pageSize", pageSize, "Int!")(encoder10)
        )
      )
    def sources[A](innerSelection: SelectionBuilder[Source, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("sources", OptionOf(ListOf(Obj(innerSelection))))
    def classes[A](innerSelection: SelectionBuilder[CharacterClass, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("classes", OptionOf(ListOf(Obj(innerSelection))))
    def races[A](innerSelection: SelectionBuilder[Race, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("races", OptionOf(ListOf(Obj(innerSelection))))
    def backgrounds[A](innerSelection: SelectionBuilder[Background, A])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field("backgrounds", OptionOf(ListOf(Obj(innerSelection))))
    def subclasses[A](
      value: String
    )(
      innerSelection:    SelectionBuilder[Subclass, A]
    )(implicit encoder0: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "subclasses",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "String!")(encoder0))
      )

  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {

    def applyOperations(
      entityType: DND5eEntityType,
      id:         Long,
      operations: List[zio.json.ast.Json] = Nil
    )(implicit
      encoder0: ArgEncoder[DND5eEntityType],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[List[zio.json.ast.Json]]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "applyOperations",
        OptionOf(Scalar()),
        arguments = List(
          Argument("entityType", entityType, "DND5eEntityType!")(encoder0),
          Argument("id", id, "Long!")(encoder1),
          Argument("operations", operations, "[Json!]!")(encoder2)
        )
      )

  }

  type Subscriptions = _root_.caliban.client.Operations.RootSubscription
  object Subscriptions {

    def operationStream[A](
      entityType: DND5eEntityType,
      id:         Long,
      operations: List[zio.json.ast.Json] = Nil
    )(
      onAdd:     SelectionBuilder[Add, A],
      onCopy:    SelectionBuilder[Copy, A],
      onMove:    SelectionBuilder[Move, A],
      onRemove:  SelectionBuilder[Remove, A],
      onReplace: SelectionBuilder[Replace, A],
      onTest:    SelectionBuilder[Test, A]
    )(implicit
      encoder0: ArgEncoder[DND5eEntityType],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[List[zio.json.ast.Json]]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootSubscription, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "operationStream",
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
          Argument("entityType", entityType, "DND5eEntityType!")(encoder0),
          Argument("id", id, "Long!")(encoder1),
          Argument("operations", operations, "[Json!]!")(encoder2)
        )
      )

  }

}

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

    case object chaoticEvil extends Alignment { val value: String = "chaoticEvil" }
    case object chaoticGood extends Alignment { val value: String = "chaoticGood" }
    case object chaoticNeutral extends Alignment { val value: String = "chaoticNeutral" }
    case object lawfulEvil extends Alignment { val value: String = "lawfulEvil" }
    case object lawfulGood extends Alignment { val value: String = "lawfulGood" }
    case object lawfulNeutral extends Alignment { val value: String = "lawfulNeutral" }
    case object neutralEvil extends Alignment { val value: String = "neutralEvil" }
    case object neutralGood extends Alignment { val value: String = "neutralGood" }
    case object trueNeutral extends Alignment { val value: String = "trueNeutral" }
    case object unaligned extends Alignment { val value: String = "unaligned" }

    implicit val decoder: ScalarDecoder[Alignment] = {
      case __StringValue("chaoticEvil")    => Right(Alignment.chaoticEvil)
      case __StringValue("chaoticGood")    => Right(Alignment.chaoticGood)
      case __StringValue("chaoticNeutral") => Right(Alignment.chaoticNeutral)
      case __StringValue("lawfulEvil")     => Right(Alignment.lawfulEvil)
      case __StringValue("lawfulGood")     => Right(Alignment.lawfulGood)
      case __StringValue("lawfulNeutral")  => Right(Alignment.lawfulNeutral)
      case __StringValue("neutralEvil")    => Right(Alignment.neutralEvil)
      case __StringValue("neutralGood")    => Right(Alignment.neutralGood)
      case __StringValue("trueNeutral")    => Right(Alignment.trueNeutral)
      case __StringValue("unaligned")      => Right(Alignment.unaligned)
      case other                           => Left(DecodingError(s"Can't build Alignment from input $other"))
    }
    implicit val encoder: ArgEncoder[Alignment] = {
      case Alignment.chaoticEvil    => __EnumValue("chaoticEvil")
      case Alignment.chaoticGood    => __EnumValue("chaoticGood")
      case Alignment.chaoticNeutral => __EnumValue("chaoticNeutral")
      case Alignment.lawfulEvil     => __EnumValue("lawfulEvil")
      case Alignment.lawfulGood     => __EnumValue("lawfulGood")
      case Alignment.lawfulNeutral  => __EnumValue("lawfulNeutral")
      case Alignment.neutralEvil    => __EnumValue("neutralEvil")
      case Alignment.neutralGood    => __EnumValue("neutralGood")
      case Alignment.trueNeutral    => __EnumValue("trueNeutral")
      case Alignment.unaligned      => __EnumValue("unaligned")
    }

    val values: scala.collection.immutable.Vector[Alignment] = scala.collection.immutable.Vector(
      chaoticEvil,
      chaoticGood,
      chaoticNeutral,
      lawfulEvil,
      lawfulGood,
      lawfulNeutral,
      neutralEvil,
      neutralGood,
      trueNeutral,
      unaligned
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

  sealed trait CreatureSize extends scala.Product with scala.Serializable { def value: String }
  object CreatureSize {

    case object unknown extends CreatureSize { val value: String = "unknown" }
    case object gargantuan extends CreatureSize { val value: String = "gargantuan" }
    case object huge extends CreatureSize { val value: String = "huge" }
    case object large extends CreatureSize { val value: String = "large" }
    case object medium extends CreatureSize { val value: String = "medium" }
    case object small extends CreatureSize { val value: String = "small" }
    case object tiny extends CreatureSize { val value: String = "tiny" }

    implicit val decoder: ScalarDecoder[CreatureSize] = {
      case __StringValue("unknown")    => Right(CreatureSize.unknown)
      case __StringValue("gargantuan") => Right(CreatureSize.gargantuan)
      case __StringValue("huge")       => Right(CreatureSize.huge)
      case __StringValue("large")      => Right(CreatureSize.large)
      case __StringValue("medium")     => Right(CreatureSize.medium)
      case __StringValue("small")      => Right(CreatureSize.small)
      case __StringValue("tiny")       => Right(CreatureSize.tiny)
      case other                       => Left(DecodingError(s"Can't build CreatureSize from input $other"))
    }
    implicit val encoder: ArgEncoder[CreatureSize] = {
      case CreatureSize.unknown    => __EnumValue("unknown")
      case CreatureSize.gargantuan => __EnumValue("gargantuan")
      case CreatureSize.huge       => __EnumValue("huge")
      case CreatureSize.large      => __EnumValue("large")
      case CreatureSize.medium     => __EnumValue("medium")
      case CreatureSize.small      => __EnumValue("small")
      case CreatureSize.tiny       => __EnumValue("tiny")
    }

    val values: scala.collection.immutable.Vector[CreatureSize] =
      scala.collection.immutable.Vector(unknown, gargantuan, huge, large, medium, small, tiny)

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

  sealed trait MonsterSearchOrder extends scala.Product with scala.Serializable { def value: String }
  object MonsterSearchOrder {

    case object alignment extends MonsterSearchOrder { val value: String = "alignment" }
    case object biome extends MonsterSearchOrder { val value: String = "biome" }
    case object challengeRating extends MonsterSearchOrder { val value: String = "challengeRating" }
    case object monsterType extends MonsterSearchOrder { val value: String = "monsterType" }
    case object name extends MonsterSearchOrder { val value: String = "name" }
    case object random extends MonsterSearchOrder { val value: String = "random" }
    case object size extends MonsterSearchOrder { val value: String = "size" }

    implicit val decoder: ScalarDecoder[MonsterSearchOrder] = {
      case __StringValue("alignment")       => Right(MonsterSearchOrder.alignment)
      case __StringValue("biome")           => Right(MonsterSearchOrder.biome)
      case __StringValue("challengeRating") => Right(MonsterSearchOrder.challengeRating)
      case __StringValue("monsterType")     => Right(MonsterSearchOrder.monsterType)
      case __StringValue("name")            => Right(MonsterSearchOrder.name)
      case __StringValue("random")          => Right(MonsterSearchOrder.random)
      case __StringValue("size")            => Right(MonsterSearchOrder.size)
      case other                            => Left(DecodingError(s"Can't build MonsterSearchOrder from input $other"))
    }
    implicit val encoder: ArgEncoder[MonsterSearchOrder] = {
      case MonsterSearchOrder.alignment       => __EnumValue("alignment")
      case MonsterSearchOrder.biome           => __EnumValue("biome")
      case MonsterSearchOrder.challengeRating => __EnumValue("challengeRating")
      case MonsterSearchOrder.monsterType     => __EnumValue("monsterType")
      case MonsterSearchOrder.name            => __EnumValue("name")
      case MonsterSearchOrder.random          => __EnumValue("random")
      case MonsterSearchOrder.size            => __EnumValue("size")
    }

    val values: scala.collection.immutable.Vector[MonsterSearchOrder] =
      scala.collection.immutable.Vector(alignment, biome, challengeRating, monsterType, name, random, size)

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
    case object Swarm extends MonsterType { val value: String = "Swarm" }
    case object Undead extends MonsterType { val value: String = "Undead" }
    case object Unknown extends MonsterType { val value: String = "Unknown" }

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
      case __StringValue("Swarm")       => Right(MonsterType.Swarm)
      case __StringValue("Undead")      => Right(MonsterType.Undead)
      case __StringValue("Unknown")     => Right(MonsterType.Unknown)
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
      case MonsterType.Swarm       => __EnumValue("Swarm")
      case MonsterType.Undead      => __EnumValue("Undead")
      case MonsterType.Unknown     => __EnumValue("Unknown")
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
      Swarm,
      Undead,
      Unknown
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

  type Campaign
  object Campaign {

    def header[A](innerSelection: SelectionBuilder[CampaignHeader, A]): SelectionBuilder[Campaign, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Campaign, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Campaign, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type CampaignHeader
  object CampaignHeader {

    def id: SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def dmUserId: SelectionBuilder[CampaignHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("dmUserId", Scalar())
    def name: SelectionBuilder[CampaignHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def gameSystem: SelectionBuilder[CampaignHeader, GameSystem] =
      _root_.caliban.client.SelectionBuilder.Field("gameSystem", Scalar())
    def campaignStatus: SelectionBuilder[CampaignHeader, CampaignStatus] =
      _root_.caliban.client.SelectionBuilder.Field("campaignStatus", Scalar())

  }

  type CharacterClass
  object CharacterClass {

    def id: SelectionBuilder[CharacterClass, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def hitDice[A](innerSelection: SelectionBuilder[DiceRoll, A]): SelectionBuilder[CharacterClass, A] =
      _root_.caliban.client.SelectionBuilder.Field("hitDice", Obj(innerSelection))

  }

  type CombatLog
  object CombatLog {

    def message: SelectionBuilder[CombatLog, String] = _root_.caliban.client.SelectionBuilder.Field("message", Scalar())
    def json: SelectionBuilder[CombatLog, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("json", Scalar())

  }

  type Copy
  object Copy {

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type DiceRoll
  object DiceRoll {

    def roll: SelectionBuilder[DiceRoll, String] = _root_.caliban.client.SelectionBuilder.Field("roll", Scalar())

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
    def status: SelectionBuilder[EncounterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("status", Scalar())
    def sceneId: SelectionBuilder[EncounterHeader, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("sceneId", OptionOf(Scalar()))
    def orderCol: SelectionBuilder[EncounterHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("orderCol", Scalar())

  }

  type GeneralLog
  object GeneralLog {

    def message: SelectionBuilder[GeneralLog, String] =
      _root_.caliban.client.SelectionBuilder.Field("message", Scalar())
    def json: SelectionBuilder[GeneralLog, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("json", Scalar())

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
    def version: SelectionBuilder[Monster, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type MonsterHeader
  object MonsterHeader {

    def id: SelectionBuilder[MonsterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def sourceId: SelectionBuilder[MonsterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("sourceId", Scalar())
    def name: SelectionBuilder[MonsterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def monsterType: SelectionBuilder[MonsterHeader, MonsterType] =
      _root_.caliban.client.SelectionBuilder.Field("monsterType", Scalar())
    def biome: SelectionBuilder[MonsterHeader, scala.Option[Biome]] =
      _root_.caliban.client.SelectionBuilder.Field("biome", OptionOf(Scalar()))
    def alignment: SelectionBuilder[MonsterHeader, scala.Option[Alignment]] =
      _root_.caliban.client.SelectionBuilder.Field("alignment", OptionOf(Scalar()))
    def cr: SelectionBuilder[MonsterHeader, Double] = _root_.caliban.client.SelectionBuilder.Field("cr", Scalar())
    def xp: SelectionBuilder[MonsterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("xp", Scalar())
    def armorClass: SelectionBuilder[MonsterHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("armorClass", Scalar())
    def maximumHitPoints: SelectionBuilder[MonsterHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("maximumHitPoints", Scalar())
    def size: SelectionBuilder[MonsterHeader, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())
    def initiativeBonus: SelectionBuilder[MonsterHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("initiativeBonus", Scalar())

  }

  type MonsterSearchResults
  object MonsterSearchResults {

    def results[A](innerSelection: SelectionBuilder[MonsterHeader, A])
      : SelectionBuilder[MonsterSearchResults, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("results", ListOf(Obj(innerSelection)))
    def total: SelectionBuilder[MonsterSearchResults, Long] =
      _root_.caliban.client.SelectionBuilder.Field("total", Scalar())

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

  }

  type PlayerCharacter
  object PlayerCharacter {

    def header[A](innerSelection: SelectionBuilder[PlayerCharacterHeader, A]): SelectionBuilder[PlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[PlayerCharacter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[PlayerCharacter, String] =
      _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type PlayerCharacterHeader
  object PlayerCharacterHeader {

    def id: SelectionBuilder[PlayerCharacterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[PlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[PlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def source: SelectionBuilder[PlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("source", Scalar())
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

  type Source
  object Source {

    def name: SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def id:   SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def url: SelectionBuilder[Source, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("url", OptionOf(Scalar()))

  }

  type SubClass
  object SubClass {

    def name: SelectionBuilder[SubClass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Test
  object Test {

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
  final case class EncounterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String,
    status:     String,
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
            "status"     -> implicitly[ArgEncoder[String]].encode(value.status),
            "sceneId"  -> value.sceneId.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[Long]].encode(value)),
            "orderCol" -> implicitly[ArgEncoder[Int]].encode(value.orderCol)
          )
        )
    }

  }
  final case class MonsterHeaderInput(
    id:               Long,
    sourceId:         String,
    name:             String,
    monsterType:      MonsterType,
    biome:            scala.Option[Biome] = None,
    alignment:        scala.Option[Alignment] = None,
    cr:               Double,
    xp:               Long,
    armorClass:       Int,
    maximumHitPoints: Int,
    size:             CreatureSize,
    initiativeBonus:  Int
  )
  object MonsterHeaderInput {

    implicit val encoder: ArgEncoder[MonsterHeaderInput] = new ArgEncoder[MonsterHeaderInput] {
      override def encode(value: MonsterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"          -> implicitly[ArgEncoder[Long]].encode(value.id),
            "sourceId"    -> implicitly[ArgEncoder[String]].encode(value.sourceId),
            "name"        -> implicitly[ArgEncoder[String]].encode(value.name),
            "monsterType" -> implicitly[ArgEncoder[MonsterType]].encode(value.monsterType),
            "biome" -> value.biome.fold(__NullValue: __Value)(value => implicitly[ArgEncoder[Biome]].encode(value)),
            "alignment" -> value.alignment.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[Alignment]].encode(value)
            ),
            "cr"               -> implicitly[ArgEncoder[Double]].encode(value.cr),
            "xp"               -> implicitly[ArgEncoder[Long]].encode(value.xp),
            "armorClass"       -> implicitly[ArgEncoder[Int]].encode(value.armorClass),
            "maximumHitPoints" -> implicitly[ArgEncoder[Int]].encode(value.maximumHitPoints),
            "size"             -> implicitly[ArgEncoder[CreatureSize]].encode(value.size),
            "initiativeBonus"  -> implicitly[ArgEncoder[Int]].encode(value.initiativeBonus)
          )
        )
    }

  }
  final case class NonPlayerCharacterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String
  )
  object NonPlayerCharacterHeaderInput {

    implicit val encoder: ArgEncoder[NonPlayerCharacterHeaderInput] = new ArgEncoder[NonPlayerCharacterHeaderInput] {
      override def encode(value: NonPlayerCharacterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name"       -> implicitly[ArgEncoder[String]].encode(value.name)
          )
        )
    }

  }
  final case class PlayerCharacterHeaderInput(
    id:         Long,
    campaignId: Long,
    name:       String,
    source:     String,
    playerName: scala.Option[String] = None
  )
  object PlayerCharacterHeaderInput {

    implicit val encoder: ArgEncoder[PlayerCharacterHeaderInput] = new ArgEncoder[PlayerCharacterHeaderInput] {
      override def encode(value: PlayerCharacterHeaderInput): __Value =
        __ObjectValue(
          List(
            "id"         -> implicitly[ArgEncoder[Long]].encode(value.id),
            "campaignId" -> implicitly[ArgEncoder[Long]].encode(value.campaignId),
            "name"       -> implicitly[ArgEncoder[String]].encode(value.name),
            "source"     -> implicitly[ArgEncoder[String]].encode(value.source),
            "playerName" -> value.playerName.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            )
          )
        )
    }

  }
  final case class PlayerCharacterSearchInput(dndBeyondId: scala.Option[String] = None)
  object PlayerCharacterSearchInput {

    implicit val encoder: ArgEncoder[PlayerCharacterSearchInput] = new ArgEncoder[PlayerCharacterSearchInput] {
      override def encode(value: PlayerCharacterSearchInput): __Value =
        __ObjectValue(
          List(
            "dndBeyondId" -> value.dndBeyondId.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            )
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
    def campaign[A](value: Long)(innerSelection: SelectionBuilder[Campaign, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder
        .Field("campaign", OptionOf(Obj(innerSelection)), arguments = List(Argument("value", value, "Long!")(encoder0)))
    def monster[A](value: Long)(innerSelection: SelectionBuilder[Monster, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder
        .Field("monster", OptionOf(Obj(innerSelection)), arguments = List(Argument("value", value, "Long!")(encoder0)))
    def playerCharacters[A](
      campaignId:            Long,
      playerCharacterSearch: PlayerCharacterSearchInput
    )(
      innerSelection: SelectionBuilder[PlayerCharacter, A]
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[PlayerCharacterSearchInput]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "playerCharacters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(
          Argument("campaignId", campaignId, "Long!")(encoder0),
          Argument("playerCharacterSearch", playerCharacterSearch, "PlayerCharacterSearchInput!")(encoder1)
        )
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
    def bestiary[A](
      name:            scala.Option[String] = None,
      challengeRating: scala.Option[Double] = None,
      size:            scala.Option[CreatureSize] = None,
      alignment:       scala.Option[Alignment] = None,
      biome:           scala.Option[Biome] = None,
      monsterType:     scala.Option[MonsterType] = None,
      source:          scala.Option[SourceInput] = None,
      orderCol:        MonsterSearchOrder,
      orderDir:        OrderDirection,
      page:            Int,
      pageSize:        Int
    )(
      innerSelection: SelectionBuilder[MonsterSearchResults, A]
    )(implicit
      encoder0:  ArgEncoder[scala.Option[String]],
      encoder1:  ArgEncoder[scala.Option[Double]],
      encoder2:  ArgEncoder[scala.Option[CreatureSize]],
      encoder3:  ArgEncoder[scala.Option[Alignment]],
      encoder4:  ArgEncoder[scala.Option[Biome]],
      encoder5:  ArgEncoder[scala.Option[MonsterType]],
      encoder6:  ArgEncoder[scala.Option[SourceInput]],
      encoder7:  ArgEncoder[MonsterSearchOrder],
      encoder8:  ArgEncoder[OrderDirection],
      encoder9:  ArgEncoder[Int],
      encoder10: ArgEncoder[Int]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "bestiary",
        OptionOf(Obj(innerSelection)),
        arguments = List(
          Argument("name", name, "String")(encoder0),
          Argument("challengeRating", challengeRating, "Float")(encoder1),
          Argument("size", size, "CreatureSize")(encoder2),
          Argument("alignment", alignment, "Alignment")(encoder3),
          Argument("biome", biome, "Biome")(encoder4),
          Argument("monsterType", monsterType, "MonsterType")(encoder5),
          Argument("source", source, "SourceInput")(encoder6),
          Argument("orderCol", orderCol, "MonsterSearchOrder!")(encoder7),
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
      innerSelection:    SelectionBuilder[SubClass, A]
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
    def upsertPlayerCharacter(
      header:   PlayerCharacterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[PlayerCharacterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertPlayerCharacter",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "PlayerCharacterHeaderInput!")(encoder0),
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
    def upsertMonster(
      header:   MonsterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[MonsterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "upsertMonster",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "MonsterHeaderInput!")(encoder0),
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
    def importCharacterDNDBeyond[A](
      campaignId:  Long,
      dndBeyondId: String,
      fresh:       Boolean
    )(
      innerSelection: SelectionBuilder[PlayerCharacter, A]
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[String],
      encoder2: ArgEncoder[Boolean]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "importCharacterDNDBeyond",
        OptionOf(Obj(innerSelection)),
        arguments = List(
          Argument("campaignId", campaignId, "Long!")(encoder0),
          Argument("dndBeyondId", dndBeyondId, "String!")(encoder1),
          Argument("fresh", fresh, "Boolean!")(encoder2)
        )
      )
    def applyOperations(
      entityType: String,
      id:         Long,
      events:     List[zio.json.ast.Json] = Nil
    )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Long],
      encoder2: ArgEncoder[List[zio.json.ast.Json]]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "applyOperations",
        OptionOf(Scalar()),
        arguments = List(
          Argument("entityType", entityType, "String!")(encoder0),
          Argument("id", id, "Long!")(encoder1),
          Argument("events", events, "[Json!]!")(encoder2)
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
      onAdd:        SelectionBuilder[Add, A],
      onCombatLog:  SelectionBuilder[CombatLog, A],
      onCopy:       SelectionBuilder[Copy, A],
      onGeneralLog: SelectionBuilder[GeneralLog, A],
      onMove:       SelectionBuilder[Move, A],
      onRemove:     SelectionBuilder[Remove, A],
      onReplace:    SelectionBuilder[Replace, A],
      onTest:       SelectionBuilder[Test, A]
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
              "Add"        -> Obj(onAdd),
              "CombatLog"  -> Obj(onCombatLog),
              "Copy"       -> Obj(onCopy),
              "GeneralLog" -> Obj(onGeneralLog),
              "Move"       -> Obj(onMove),
              "Remove"     -> Obj(onRemove),
              "Replace"    -> Obj(onReplace),
              "Test"       -> Obj(onTest)
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

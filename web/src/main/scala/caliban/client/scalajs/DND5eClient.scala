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

import caliban.client.CalibanClientError.DecodingError
import caliban.client.FieldBuilder._
import caliban.client._
import caliban.client.__Value._

object DND5eClient {

  sealed trait AbilityType extends scala.Product with scala.Serializable { def value: String }
  object AbilityType {

    case object Charisma extends AbilityType { val value: String = "Charisma" }
    case object Constitution extends AbilityType { val value: String = "Constitution" }
    case object Dexterity extends AbilityType { val value: String = "Dexterity" }
    case object Intelligence extends AbilityType { val value: String = "Intelligence" }
    case object Strength extends AbilityType { val value: String = "Strength" }
    case object Wisdom extends AbilityType { val value: String = "Wisdom" }

    implicit val decoder: ScalarDecoder[AbilityType] = {
      case __StringValue("Charisma")     => Right(AbilityType.Charisma)
      case __StringValue("Constitution") => Right(AbilityType.Constitution)
      case __StringValue("Dexterity")    => Right(AbilityType.Dexterity)
      case __StringValue("Intelligence") => Right(AbilityType.Intelligence)
      case __StringValue("Strength")     => Right(AbilityType.Strength)
      case __StringValue("Wisdom")       => Right(AbilityType.Wisdom)
      case other                         => Left(DecodingError(s"Can't build AbilityType from input $other"))
    }
    implicit val encoder: ArgEncoder[AbilityType] = {
      case AbilityType.Charisma     => __EnumValue("Charisma")
      case AbilityType.Constitution => __EnumValue("Constitution")
      case AbilityType.Dexterity    => __EnumValue("Dexterity")
      case AbilityType.Intelligence => __EnumValue("Intelligence")
      case AbilityType.Strength     => __EnumValue("Strength")
      case AbilityType.Wisdom       => __EnumValue("Wisdom")
    }

    val values: scala.collection.immutable.Vector[AbilityType] =
      scala.collection.immutable.Vector(Charisma, Constitution, Dexterity, Intelligence, Strength, Wisdom)

  }

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

  sealed trait Condition extends scala.Product with scala.Serializable { def value: String }
  object Condition {

    case object Blinded extends Condition { val value: String = "Blinded" }
    case object Charmed extends Condition { val value: String = "Charmed" }
    case object Deafened extends Condition { val value: String = "Deafened" }
    case object Frightened extends Condition { val value: String = "Frightened" }
    case object Grappled extends Condition { val value: String = "Grappled" }
    case object Incapacitated extends Condition { val value: String = "Incapacitated" }
    case object Invisible extends Condition { val value: String = "Invisible" }
    case object Paralyzed extends Condition { val value: String = "Paralyzed" }
    case object Petrified extends Condition { val value: String = "Petrified" }
    case object Poisoned extends Condition { val value: String = "Poisoned" }
    case object Prone extends Condition { val value: String = "Prone" }
    case object Restrained extends Condition { val value: String = "Restrained" }
    case object Stunned extends Condition { val value: String = "Stunned" }
    case object Unconscious extends Condition { val value: String = "Unconscious" }

    implicit val decoder: ScalarDecoder[Condition] = {
      case __StringValue("Blinded")       => Right(Condition.Blinded)
      case __StringValue("Charmed")       => Right(Condition.Charmed)
      case __StringValue("Deafened")      => Right(Condition.Deafened)
      case __StringValue("Frightened")    => Right(Condition.Frightened)
      case __StringValue("Grappled")      => Right(Condition.Grappled)
      case __StringValue("Incapacitated") => Right(Condition.Incapacitated)
      case __StringValue("Invisible")     => Right(Condition.Invisible)
      case __StringValue("Paralyzed")     => Right(Condition.Paralyzed)
      case __StringValue("Petrified")     => Right(Condition.Petrified)
      case __StringValue("Poisoned")      => Right(Condition.Poisoned)
      case __StringValue("Prone")         => Right(Condition.Prone)
      case __StringValue("Restrained")    => Right(Condition.Restrained)
      case __StringValue("Stunned")       => Right(Condition.Stunned)
      case __StringValue("Unconscious")   => Right(Condition.Unconscious)
      case other                          => Left(DecodingError(s"Can't build Condition from input $other"))
    }
    implicit val encoder: ArgEncoder[Condition] = {
      case Condition.Blinded       => __EnumValue("Blinded")
      case Condition.Charmed       => __EnumValue("Charmed")
      case Condition.Deafened      => __EnumValue("Deafened")
      case Condition.Frightened    => __EnumValue("Frightened")
      case Condition.Grappled      => __EnumValue("Grappled")
      case Condition.Incapacitated => __EnumValue("Incapacitated")
      case Condition.Invisible     => __EnumValue("Invisible")
      case Condition.Paralyzed     => __EnumValue("Paralyzed")
      case Condition.Petrified     => __EnumValue("Petrified")
      case Condition.Poisoned      => __EnumValue("Poisoned")
      case Condition.Prone         => __EnumValue("Prone")
      case Condition.Restrained    => __EnumValue("Restrained")
      case Condition.Stunned       => __EnumValue("Stunned")
      case Condition.Unconscious   => __EnumValue("Unconscious")
    }

    val values: scala.collection.immutable.Vector[Condition] = scala.collection.immutable.Vector(
      Blinded,
      Charmed,
      Deafened,
      Frightened,
      Grappled,
      Incapacitated,
      Invisible,
      Paralyzed,
      Petrified,
      Poisoned,
      Prone,
      Restrained,
      Stunned,
      Unconscious
    )

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

  sealed trait EncounterDifficulty extends scala.Product with scala.Serializable { def value: String }
  object EncounterDifficulty {

    case object Deadly extends EncounterDifficulty { val value: String = "Deadly" }
    case object Easy extends EncounterDifficulty { val value: String = "Easy" }
    case object Hard extends EncounterDifficulty { val value: String = "Hard" }
    case object Medium extends EncounterDifficulty { val value: String = "Medium" }

    implicit val decoder: ScalarDecoder[EncounterDifficulty] = {
      case __StringValue("Deadly") => Right(EncounterDifficulty.Deadly)
      case __StringValue("Easy")   => Right(EncounterDifficulty.Easy)
      case __StringValue("Hard")   => Right(EncounterDifficulty.Hard)
      case __StringValue("Medium") => Right(EncounterDifficulty.Medium)
      case other                   => Left(DecodingError(s"Can't build EncounterDifficulty from input $other"))
    }
    implicit val encoder: ArgEncoder[EncounterDifficulty] = {
      case EncounterDifficulty.Deadly => __EnumValue("Deadly")
      case EncounterDifficulty.Easy   => __EnumValue("Easy")
      case EncounterDifficulty.Hard   => __EnumValue("Hard")
      case EncounterDifficulty.Medium => __EnumValue("Medium")
    }

    val values: scala.collection.immutable.Vector[EncounterDifficulty] =
      scala.collection.immutable.Vector(Deadly, Easy, Hard, Medium)

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

  sealed trait Sense extends scala.Product with scala.Serializable { def value: String }
  object Sense {

    case object blindsight extends Sense { val value: String = "blindsight" }
    case object darkvision extends Sense { val value: String = "darkvision" }
    case object sight extends Sense { val value: String = "sight" }
    case object tremorsense extends Sense { val value: String = "tremorsense" }
    case object truesight extends Sense { val value: String = "truesight" }

    implicit val decoder: ScalarDecoder[Sense] = {
      case __StringValue("blindsight")  => Right(Sense.blindsight)
      case __StringValue("darkvision")  => Right(Sense.darkvision)
      case __StringValue("sight")       => Right(Sense.sight)
      case __StringValue("tremorsense") => Right(Sense.tremorsense)
      case __StringValue("truesight")   => Right(Sense.truesight)
      case other                        => Left(DecodingError(s"Can't build Sense from input $other"))
    }
    implicit val encoder: ArgEncoder[Sense] = {
      case Sense.blindsight  => __EnumValue("blindsight")
      case Sense.darkvision  => __EnumValue("darkvision")
      case Sense.sight       => __EnumValue("sight")
      case Sense.tremorsense => __EnumValue("tremorsense")
      case Sense.truesight   => __EnumValue("truesight")
    }

    val values: scala.collection.immutable.Vector[Sense] =
      scala.collection.immutable.Vector(blindsight, darkvision, sight, tremorsense, truesight)

  }

  type Ability
  object Ability {

    def abilityType: SelectionBuilder[Ability, AbilityType] =
      _root_.caliban.client.SelectionBuilder.Field("abilityType", Scalar())
    def value: SelectionBuilder[Ability, Int] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())
    def bonus: SelectionBuilder[Ability, Int] = _root_.caliban.client.SelectionBuilder.Field("bonus", Scalar())
    def tempValue: SelectionBuilder[Ability, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("tempValue", OptionOf(Scalar()))
    def tempBonus: SelectionBuilder[Ability, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("tempBonus", OptionOf(Scalar()))

  }

  type Actions
  object Actions {

    def str: SelectionBuilder[Actions, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type Add
  object Add {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Add, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Add, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Background
  object Background {

    def name: SelectionBuilder[Background, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Campaign
  object Campaign {

    def header[A](innerSelection: SelectionBuilder[CampaignHeader, A]): SelectionBuilder[Campaign, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def info[A](innerSelection: SelectionBuilder[CampaignInfo, A]): SelectionBuilder[Campaign, A] =
      _root_.caliban.client.SelectionBuilder.Field("info", Obj(innerSelection))

  }

  type CampaignHeader
  object CampaignHeader {

    def id:   SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def dm:   SelectionBuilder[CampaignHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("dm", Scalar())
    def name: SelectionBuilder[CampaignHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def gameSystem: SelectionBuilder[CampaignHeader, GameSystem] =
      _root_.caliban.client.SelectionBuilder.Field("gameSystem", Scalar())

  }

  type CampaignInfo
  object CampaignInfo {

    def notes: SelectionBuilder[CampaignInfo, String] = _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def scenes[A](innerSelection: SelectionBuilder[Scene, A]): SelectionBuilder[CampaignInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("scenes", ListOf(Obj(innerSelection)))

  }

  type CharacterClass
  object CharacterClass {

    def characterClassId: SelectionBuilder[CharacterClass, Long] =
      _root_.caliban.client.SelectionBuilder.Field("characterClassId", Scalar())
    def name: SelectionBuilder[CharacterClass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def subclass[A](innerSelection: SelectionBuilder[Subclass, A]): SelectionBuilder[CharacterClass, A] =
      _root_.caliban.client.SelectionBuilder.Field("subclass", Obj(innerSelection))
    def level: SelectionBuilder[CharacterClass, Int] = _root_.caliban.client.SelectionBuilder.Field("level", Scalar())

  }

  type Choices
  object Choices {

    def str: SelectionBuilder[Choices, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type Copy
  object Copy {

    def from[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("from", Obj(innerSelection))
    def to[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Copy, A] =
      _root_.caliban.client.SelectionBuilder.Field("to", Obj(innerSelection))

  }

  type Creature
  object Creature {

    def name: SelectionBuilder[Creature, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def creatureType: SelectionBuilder[Creature, Long] =
      _root_.caliban.client.SelectionBuilder.Field("creatureType", Scalar())

  }

  type DNDBeyondImportSource
  object DNDBeyondImportSource {

    def url: SelectionBuilder[DNDBeyondImportSource, String] =
      _root_.caliban.client.SelectionBuilder.Field("url", Scalar())

  }

  type DeathSave
  object DeathSave {

    def fails: SelectionBuilder[DeathSave, Int] = _root_.caliban.client.SelectionBuilder.Field("fails", Scalar())
    def successes: SelectionBuilder[DeathSave, Int] =
      _root_.caliban.client.SelectionBuilder.Field("successes", Scalar())

  }

  type Encounter
  object Encounter {

    def header[A](innerSelection: SelectionBuilder[EncounterHeader, A]): SelectionBuilder[Encounter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def info[A](innerSelection: SelectionBuilder[EncounterInfo, A]): SelectionBuilder[Encounter, A] =
      _root_.caliban.client.SelectionBuilder.Field("info", Obj(innerSelection))

  }

  type EncounterHeader
  object EncounterHeader {

    def id:   SelectionBuilder[EncounterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def name: SelectionBuilder[EncounterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type EncounterInfo
  object EncounterInfo {

    def entities[A](
      onMonsterEncounterEntity:         SelectionBuilder[MonsterEncounterEntity, A],
      onPlayerCharacterEncounterEntity: SelectionBuilder[PlayerCharacterEncounterEntity, A]
    ): SelectionBuilder[EncounterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "entities",
        ListOf(
          ChoiceOf(
            Map(
              "MonsterEncounterEntity"         -> Obj(onMonsterEncounterEntity),
              "PlayerCharacterEncounterEntity" -> Obj(onPlayerCharacterEncounterEntity)
            )
          )
        )
      )
    def difficulty: SelectionBuilder[EncounterInfo, EncounterDifficulty] =
      _root_.caliban.client.SelectionBuilder.Field("difficulty", Scalar())
    def xp: SelectionBuilder[EncounterInfo, Int] = _root_.caliban.client.SelectionBuilder.Field("xp", Scalar())
    def entitiesOption[A](
      onMonsterEncounterEntity:         scala.Option[SelectionBuilder[MonsterEncounterEntity, A]] = None,
      onPlayerCharacterEncounterEntity: scala.Option[SelectionBuilder[PlayerCharacterEncounterEntity, A]] = None
    ): SelectionBuilder[EncounterInfo, List[scala.Option[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "entities",
        ListOf(
          ChoiceOf(
            Map(
              "MonsterEncounterEntity" -> onMonsterEncounterEntity.fold[FieldBuilder[scala.Option[A]]](NullField)(a =>
                OptionOf(Obj(a))
              ),
              "PlayerCharacterEncounterEntity" -> onPlayerCharacterEncounterEntity
                .fold[FieldBuilder[scala.Option[A]]](NullField)(a => OptionOf(Obj(a)))
            )
          )
        )
      )

  }

  type Feat
  object Feat {

    def name: SelectionBuilder[Feat, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type InventoryItem
  object InventoryItem {

    def name: SelectionBuilder[InventoryItem, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type JsonPath
  object JsonPath {

    def value: SelectionBuilder[JsonPath, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Lifestyle
  object Lifestyle {

    def str: SelectionBuilder[Lifestyle, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type Modifiers
  object Modifiers {

    def str: SelectionBuilder[Modifiers, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type Monster
  object Monster {

    def header[A](innerSelection: SelectionBuilder[MonsterHeader, A]): SelectionBuilder[Monster, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def info[A](innerSelection: SelectionBuilder[MonsterInfo, A]): SelectionBuilder[Monster, A] =
      _root_.caliban.client.SelectionBuilder.Field("info", Obj(innerSelection))

  }

  type MonsterEncounterEntity
  object MonsterEncounterEntity {

    def monster[A](innerSelection: SelectionBuilder[Monster, A]): SelectionBuilder[MonsterEncounterEntity, A] =
      _root_.caliban.client.SelectionBuilder.Field("monster", Obj(innerSelection))
    def notes: SelectionBuilder[MonsterEncounterEntity, String] =
      _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def concentration: SelectionBuilder[MonsterEncounterEntity, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("concentration", Scalar())
    def hide: SelectionBuilder[MonsterEncounterEntity, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("hide", Scalar())
    def hp: SelectionBuilder[MonsterEncounterEntity, Int] = _root_.caliban.client.SelectionBuilder.Field("hp", Scalar())
    def ac: SelectionBuilder[MonsterEncounterEntity, Int] = _root_.caliban.client.SelectionBuilder.Field("ac", Scalar())
    def initiative: SelectionBuilder[MonsterEncounterEntity, Int] =
      _root_.caliban.client.SelectionBuilder.Field("initiative", Scalar())
    def conditions: SelectionBuilder[MonsterEncounterEntity, List[Condition]] =
      _root_.caliban.client.SelectionBuilder.Field("conditions", ListOf(Scalar()))

  }

  type MonsterHeader
  object MonsterHeader {

    def id:   SelectionBuilder[MonsterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def name: SelectionBuilder[MonsterHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def monsterType: SelectionBuilder[MonsterHeader, MonsterType] =
      _root_.caliban.client.SelectionBuilder.Field("monsterType", Scalar())
    def biome: SelectionBuilder[MonsterHeader, Biome] = _root_.caliban.client.SelectionBuilder.Field("biome", Scalar())
    def alignment: SelectionBuilder[MonsterHeader, Alignment] =
      _root_.caliban.client.SelectionBuilder.Field("alignment", Scalar())
    def cr: SelectionBuilder[MonsterHeader, Double] = _root_.caliban.client.SelectionBuilder.Field("cr", Scalar())
    def xp: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("xp", Scalar())
    def ac: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("ac", Scalar())
    def hp: SelectionBuilder[MonsterHeader, Int] = _root_.caliban.client.SelectionBuilder.Field("hp", Scalar())
    def size: SelectionBuilder[MonsterHeader, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())

  }

  type MonsterInfo
  object MonsterInfo {

    def walkingSpeed: SelectionBuilder[MonsterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("walkingSpeed", OptionOf(Scalar()))
    def burrowingSpeed: SelectionBuilder[MonsterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("burrowingSpeed", OptionOf(Scalar()))
    def climbingSpeed: SelectionBuilder[MonsterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("climbingSpeed", OptionOf(Scalar()))
    def flyingSpeed: SelectionBuilder[MonsterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("flyingSpeed", OptionOf(Scalar()))
    def swimmingSpeed: SelectionBuilder[MonsterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("swimmingSpeed", OptionOf(Scalar()))
    def abilities[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[MonsterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("abilities", ListOf(Obj(innerSelection)))
    def languages: SelectionBuilder[MonsterInfo, List[String]] =
      _root_.caliban.client.SelectionBuilder.Field("languages", ListOf(Scalar()))
    def challenge: SelectionBuilder[MonsterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("challenge", Scalar())
    def traits: SelectionBuilder[MonsterInfo, String] = _root_.caliban.client.SelectionBuilder.Field("traits", Scalar())
    def actions: SelectionBuilder[MonsterInfo, List[String]] =
      _root_.caliban.client.SelectionBuilder.Field("actions", ListOf(Scalar()))
    def reactions: SelectionBuilder[MonsterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("reactions", Scalar())
    def senses: SelectionBuilder[MonsterInfo, List[Sense]] =
      _root_.caliban.client.SelectionBuilder.Field("senses", ListOf(Scalar()))

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

    def id: SelectionBuilder[NonPlayerCharacter, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def info[A](innerSelection: SelectionBuilder[NonPlayerCharacterInfo, A]): SelectionBuilder[NonPlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("info", Obj(innerSelection))

  }

  type NonPlayerCharacterInfo
  object NonPlayerCharacterInfo {

    def name: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def gender: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("gender", Scalar())
    def race[A](innerSelection: SelectionBuilder[Race, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("race", Obj(innerSelection))
    def characterClass[A](innerSelection: SelectionBuilder[CharacterClass, A])
      : SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("characterClass", Obj(innerSelection))
    def level: SelectionBuilder[NonPlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("level", Scalar())
    def age: SelectionBuilder[NonPlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("age", Scalar())
    def background[A](innerSelection: SelectionBuilder[Background, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("background", Obj(innerSelection))
    def occupation: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("occupation", Scalar())
    def personality: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("personality", Scalar())
    def ideal: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("ideal", Scalar())
    def bond: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("bond", Scalar())
    def flaw: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("flaw", Scalar())
    def characteristic: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("characteristic", Scalar())
    def speech: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("speech", Scalar())
    def hobby: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("hobby", Scalar())
    def fear: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("fear", Scalar())
    def currently: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("currently", Scalar())
    def nickname: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("nickname", Scalar())
    def weapon: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("weapon", Scalar())
    def rumor: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("rumor", Scalar())
    def raisedBy: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("raisedBy", Scalar())
    def parent1: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("parent1", Scalar())
    def parent2: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("parent2", Scalar())
    def siblingCount: SelectionBuilder[NonPlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("siblingCount", Scalar())
    def childhood: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("childhood", Scalar())
    def children: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("children", Scalar())
    def spouse: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("spouse", Scalar())
    def monster[A](innerSelection: SelectionBuilder[Monster, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("monster", Obj(innerSelection))

  }

  type Options
  object Options {

    def str: SelectionBuilder[Options, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type PhysicalCharacteristics
  object PhysicalCharacteristics {

    def gender: SelectionBuilder[PhysicalCharacteristics, String] =
      _root_.caliban.client.SelectionBuilder.Field("gender", Scalar())
    def age: SelectionBuilder[PhysicalCharacteristics, Int] =
      _root_.caliban.client.SelectionBuilder.Field("age", Scalar())
    def hair: SelectionBuilder[PhysicalCharacteristics, String] =
      _root_.caliban.client.SelectionBuilder.Field("hair", Scalar())
    def eyes: SelectionBuilder[PhysicalCharacteristics, String] =
      _root_.caliban.client.SelectionBuilder.Field("eyes", Scalar())
    def skin: SelectionBuilder[PhysicalCharacteristics, String] =
      _root_.caliban.client.SelectionBuilder.Field("skin", Scalar())
    def height: SelectionBuilder[PhysicalCharacteristics, String] =
      _root_.caliban.client.SelectionBuilder.Field("height", Scalar())
    def weight: SelectionBuilder[PhysicalCharacteristics, Int] =
      _root_.caliban.client.SelectionBuilder.Field("weight", Scalar())
    def size: SelectionBuilder[PhysicalCharacteristics, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())

  }

  type PlayerCharacter
  object PlayerCharacter {

    def header[A](innerSelection: SelectionBuilder[PlayerCharacterHeader, A]): SelectionBuilder[PlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def info[A](innerSelection: SelectionBuilder[PlayerCharacterInfo, A]): SelectionBuilder[PlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("info", Obj(innerSelection))

  }

  type PlayerCharacterEncounterEntity
  object PlayerCharacterEncounterEntity {

    def playerCharacter[A](innerSelection: SelectionBuilder[PlayerCharacter, A])
      : SelectionBuilder[PlayerCharacterEncounterEntity, A] =
      _root_.caliban.client.SelectionBuilder.Field("playerCharacter", Obj(innerSelection))
    def notes: SelectionBuilder[PlayerCharacterEncounterEntity, String] =
      _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def concentration: SelectionBuilder[PlayerCharacterEncounterEntity, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("concentration", Scalar())
    def hide: SelectionBuilder[PlayerCharacterEncounterEntity, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("hide", Scalar())
    def hp: SelectionBuilder[PlayerCharacterEncounterEntity, Int] =
      _root_.caliban.client.SelectionBuilder.Field("hp", Scalar())
    def ac: SelectionBuilder[PlayerCharacterEncounterEntity, Int] =
      _root_.caliban.client.SelectionBuilder.Field("ac", Scalar())
    def initiative: SelectionBuilder[PlayerCharacterEncounterEntity, Int] =
      _root_.caliban.client.SelectionBuilder.Field("initiative", Scalar())
    def conditions: SelectionBuilder[PlayerCharacterEncounterEntity, List[Condition]] =
      _root_.caliban.client.SelectionBuilder.Field("conditions", ListOf(Scalar()))

  }

  type PlayerCharacterHeader
  object PlayerCharacterHeader {

    def id: SelectionBuilder[PlayerCharacterHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())

  }

  type PlayerCharacterInfo
  object PlayerCharacterInfo {

    def id: SelectionBuilder[PlayerCharacterInfo, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def source[A](onDNDBeyondImportSource: SelectionBuilder[DNDBeyondImportSource, A])
      : SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder
        .Field("source", ChoiceOf(Map("DNDBeyondImportSource" -> Obj(onDNDBeyondImportSource))))
    def name: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def physicalCharacteristics[A](innerSelection: SelectionBuilder[PhysicalCharacteristics, A])
      : SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("physicalCharacteristics", Obj(innerSelection))
    def faith: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("faith", Scalar())
    def inspiration: SelectionBuilder[PlayerCharacterInfo, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("inspiration", Scalar())
    def baseHitPoints: SelectionBuilder[PlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("baseHitPoints", Scalar())
    def bonusHitPoints: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("bonusHitPoints", Scalar())
    def overrideHitPoints: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("overrideHitPoints", Scalar())
    def removedHitPoints: SelectionBuilder[PlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("removedHitPoints", Scalar())
    def temporaryHitPoints: SelectionBuilder[PlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("temporaryHitPoints", Scalar())
    def currentXp: SelectionBuilder[PlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("currentXp", Scalar())
    def armorClass: SelectionBuilder[PlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("armorClass", Scalar())
    def alignment: SelectionBuilder[PlayerCharacterInfo, Alignment] =
      _root_.caliban.client.SelectionBuilder.Field("alignment", Scalar())
    def lifestyle[A](innerSelection: SelectionBuilder[Lifestyle, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("lifestyle", Obj(innerSelection))
    def abilities[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("abilities", ListOf(Obj(innerSelection)))
    def background[A](innerSelection: SelectionBuilder[Background, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("background", Obj(innerSelection))
    def race[A](innerSelection: SelectionBuilder[Race, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("race", Obj(innerSelection))
    def traits[A](innerSelection: SelectionBuilder[Traits, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("traits", Obj(innerSelection))
    def inventory[A](innerSelection: SelectionBuilder[InventoryItem, A])
      : SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("inventory", ListOf(Obj(innerSelection)))
    def wallet[A](innerSelection: SelectionBuilder[Wallet, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("wallet", Obj(innerSelection))
    def classes[A](innerSelection: SelectionBuilder[CharacterClass, A])
      : SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("classes", ListOf(Obj(innerSelection)))
    def feats[A](innerSelection: SelectionBuilder[Feat, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("feats", ListOf(Obj(innerSelection)))
    def conditions: SelectionBuilder[PlayerCharacterInfo, List[Condition]] =
      _root_.caliban.client.SelectionBuilder.Field("conditions", ListOf(Scalar()))
    def deathSaves[A](innerSelection: SelectionBuilder[DeathSave, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("deathSaves", Obj(innerSelection))
    def adjustmentXp: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("adjustmentXp", Scalar())
    def spellSlots[A](innerSelection: SelectionBuilder[SpellSlot, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("spellSlots", ListOf(Obj(innerSelection)))
    def pactMagic[A](innerSelection: SelectionBuilder[SpellSlot, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("pactMagic", ListOf(Obj(innerSelection)))
    def languages: SelectionBuilder[PlayerCharacterInfo, List[String]] =
      _root_.caliban.client.SelectionBuilder.Field("languages", ListOf(Scalar()))
    def options[A](innerSelection: SelectionBuilder[Options, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("options", Obj(innerSelection))
    def choices[A](innerSelection: SelectionBuilder[Choices, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("choices", Obj(innerSelection))
    def actions[A](innerSelection: SelectionBuilder[Actions, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("actions", Obj(innerSelection))
    def modifiers[A](innerSelection: SelectionBuilder[Modifiers, A]): SelectionBuilder[PlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("modifiers", Obj(innerSelection))
    def classSpells[A](innerSelection: SelectionBuilder[Spell, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("classSpells", ListOf(Obj(innerSelection)))
    def creatures[A](innerSelection: SelectionBuilder[Creature, A]): SelectionBuilder[PlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("creatures", ListOf(Obj(innerSelection)))
    def notes: SelectionBuilder[PlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def sourceOption[A](onDNDBeyondImportSource: scala.Option[SelectionBuilder[DNDBeyondImportSource, A]] = None)
      : SelectionBuilder[PlayerCharacterInfo, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "source",
        ChoiceOf(
          Map(
            "DNDBeyondImportSource" -> onDNDBeyondImportSource.fold[FieldBuilder[scala.Option[A]]](NullField)(a =>
              OptionOf(Obj(a))
            )
          )
        )
      )

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
    def value: SelectionBuilder[Replace, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Scene
  object Scene {

    def name:     SelectionBuilder[Scene, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def isActive: SelectionBuilder[Scene, Boolean] = _root_.caliban.client.SelectionBuilder.Field("isActive", Scalar())
    def notes:    SelectionBuilder[Scene, String] = _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def npcs: SelectionBuilder[Scene, List[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("npcs", ListOf(Scalar()))
    def encounters: SelectionBuilder[Scene, List[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("encounters", ListOf(Scalar()))

  }

  type Source
  object Source {

    def name:     SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def nickName: SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("nickName", Scalar())
    def url:      SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("url", Scalar())

  }

  type Spell
  object Spell {

    def str: SelectionBuilder[Spell, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type SpellSlot
  object SpellSlot {

    def str: SelectionBuilder[SpellSlot, String] = _root_.caliban.client.SelectionBuilder.Field("str", Scalar())

  }

  type Subclass
  object Subclass {

    def name: SelectionBuilder[Subclass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Test
  object Test {

    def path[A](innerSelection: SelectionBuilder[JsonPath, A]): SelectionBuilder[Test, A] =
      _root_.caliban.client.SelectionBuilder.Field("path", Obj(innerSelection))
    def value: SelectionBuilder[Test, String] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type Traits
  object Traits {

    def personalityTraits: SelectionBuilder[Traits, String] =
      _root_.caliban.client.SelectionBuilder.Field("personalityTraits", Scalar())
    def ideals: SelectionBuilder[Traits, String] = _root_.caliban.client.SelectionBuilder.Field("ideals", Scalar())
    def bonds:  SelectionBuilder[Traits, String] = _root_.caliban.client.SelectionBuilder.Field("bonds", Scalar())
    def flaws:  SelectionBuilder[Traits, String] = _root_.caliban.client.SelectionBuilder.Field("flaws", Scalar())
    def appearance: SelectionBuilder[Traits, String] =
      _root_.caliban.client.SelectionBuilder.Field("appearance", Scalar())

  }

  type Wallet
  object Wallet {

    def pp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("pp", Scalar())
    def gp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("gp", Scalar())
    def ep: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("ep", Scalar())
    def sp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("sp", Scalar())
    def cp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("cp", Scalar())

  }

  final case class SourceInput(
    name:     String,
    nickName: String,
    url:      String
  )
  object SourceInput {

    implicit val encoder: ArgEncoder[SourceInput] = new ArgEncoder[SourceInput] {
      override def encode(value: SourceInput): __Value =
        __ObjectValue(
          List(
            "name"     -> implicitly[ArgEncoder[String]].encode(value.name),
            "nickName" -> implicitly[ArgEncoder[String]].encode(value.nickName),
            "url"      -> implicitly[ArgEncoder[String]].encode(value.url)
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
    def subclasses[A](value: Long)(innerSelection: SelectionBuilder[Subclass, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "subclasses",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )

  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {

    def event: SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Boolean]] =
      _root_.caliban.client.SelectionBuilder.Field("event", OptionOf(Scalar()))

  }

  type Subscriptions = _root_.caliban.client.Operations.RootSubscription
  object Subscriptions {

    def operationStream[A](
      onAdd:     SelectionBuilder[Add, A],
      onCopy:    SelectionBuilder[Copy, A],
      onMove:    SelectionBuilder[Move, A],
      onRemove:  SelectionBuilder[Remove, A],
      onReplace: SelectionBuilder[Replace, A],
      onTest:    SelectionBuilder[Test, A]
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
        )
      )

  }

}

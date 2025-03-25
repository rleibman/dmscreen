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

  sealed trait ChallengeRating extends scala.Product with scala.Serializable { def value: String }
  object ChallengeRating {

    case object _0 extends ChallengeRating { val value: String = "_0" }
    case object _eigth extends ChallengeRating { val value: String = "_eigth" }
    case object _quarter extends ChallengeRating { val value: String = "_quarter" }
    case object _half extends ChallengeRating { val value: String = "_half" }
    case object _1 extends ChallengeRating { val value: String = "_1" }
    case object _2 extends ChallengeRating { val value: String = "_2" }
    case object _3 extends ChallengeRating { val value: String = "_3" }
    case object _4 extends ChallengeRating { val value: String = "_4" }
    case object _5 extends ChallengeRating { val value: String = "_5" }
    case object _6 extends ChallengeRating { val value: String = "_6" }
    case object _7 extends ChallengeRating { val value: String = "_7" }
    case object _8 extends ChallengeRating { val value: String = "_8" }
    case object _9 extends ChallengeRating { val value: String = "_9" }
    case object _10 extends ChallengeRating { val value: String = "_10" }
    case object _11 extends ChallengeRating { val value: String = "_11" }
    case object _12 extends ChallengeRating { val value: String = "_12" }
    case object _13 extends ChallengeRating { val value: String = "_13" }
    case object _14 extends ChallengeRating { val value: String = "_14" }
    case object _15 extends ChallengeRating { val value: String = "_15" }
    case object _16 extends ChallengeRating { val value: String = "_16" }
    case object _17 extends ChallengeRating { val value: String = "_17" }
    case object _18 extends ChallengeRating { val value: String = "_18" }
    case object _19 extends ChallengeRating { val value: String = "_19" }
    case object _20 extends ChallengeRating { val value: String = "_20" }
    case object _21 extends ChallengeRating { val value: String = "_21" }
    case object _22 extends ChallengeRating { val value: String = "_22" }
    case object _23 extends ChallengeRating { val value: String = "_23" }
    case object _24 extends ChallengeRating { val value: String = "_24" }
    case object _25 extends ChallengeRating { val value: String = "_25" }
    case object _26 extends ChallengeRating { val value: String = "_26" }
    case object _27 extends ChallengeRating { val value: String = "_27" }
    case object _28 extends ChallengeRating { val value: String = "_28" }
    case object _29 extends ChallengeRating { val value: String = "_29" }
    case object _30 extends ChallengeRating { val value: String = "_30" }

    implicit val decoder: ScalarDecoder[ChallengeRating] = {
      case __StringValue("_0")       => Right(ChallengeRating._0)
      case __StringValue("_eigth")   => Right(ChallengeRating._eigth)
      case __StringValue("_quarter") => Right(ChallengeRating._quarter)
      case __StringValue("_half")    => Right(ChallengeRating._half)
      case __StringValue("_1")       => Right(ChallengeRating._1)
      case __StringValue("_2")       => Right(ChallengeRating._2)
      case __StringValue("_3")       => Right(ChallengeRating._3)
      case __StringValue("_4")       => Right(ChallengeRating._4)
      case __StringValue("_5")       => Right(ChallengeRating._5)
      case __StringValue("_6")       => Right(ChallengeRating._6)
      case __StringValue("_7")       => Right(ChallengeRating._7)
      case __StringValue("_8")       => Right(ChallengeRating._8)
      case __StringValue("_9")       => Right(ChallengeRating._9)
      case __StringValue("_10")      => Right(ChallengeRating._10)
      case __StringValue("_11")      => Right(ChallengeRating._11)
      case __StringValue("_12")      => Right(ChallengeRating._12)
      case __StringValue("_13")      => Right(ChallengeRating._13)
      case __StringValue("_14")      => Right(ChallengeRating._14)
      case __StringValue("_15")      => Right(ChallengeRating._15)
      case __StringValue("_16")      => Right(ChallengeRating._16)
      case __StringValue("_17")      => Right(ChallengeRating._17)
      case __StringValue("_18")      => Right(ChallengeRating._18)
      case __StringValue("_19")      => Right(ChallengeRating._19)
      case __StringValue("_20")      => Right(ChallengeRating._20)
      case __StringValue("_21")      => Right(ChallengeRating._21)
      case __StringValue("_22")      => Right(ChallengeRating._22)
      case __StringValue("_23")      => Right(ChallengeRating._23)
      case __StringValue("_24")      => Right(ChallengeRating._24)
      case __StringValue("_25")      => Right(ChallengeRating._25)
      case __StringValue("_26")      => Right(ChallengeRating._26)
      case __StringValue("_27")      => Right(ChallengeRating._27)
      case __StringValue("_28")      => Right(ChallengeRating._28)
      case __StringValue("_29")      => Right(ChallengeRating._29)
      case __StringValue("_30")      => Right(ChallengeRating._30)
      case other                     => Left(DecodingError(s"Can't build ChallengeRating from input $other"))
    }
    implicit val encoder: ArgEncoder[ChallengeRating] = {
      case ChallengeRating._0       => __EnumValue("_0")
      case ChallengeRating._eigth   => __EnumValue("_eigth")
      case ChallengeRating._quarter => __EnumValue("_quarter")
      case ChallengeRating._half    => __EnumValue("_half")
      case ChallengeRating._1       => __EnumValue("_1")
      case ChallengeRating._2       => __EnumValue("_2")
      case ChallengeRating._3       => __EnumValue("_3")
      case ChallengeRating._4       => __EnumValue("_4")
      case ChallengeRating._5       => __EnumValue("_5")
      case ChallengeRating._6       => __EnumValue("_6")
      case ChallengeRating._7       => __EnumValue("_7")
      case ChallengeRating._8       => __EnumValue("_8")
      case ChallengeRating._9       => __EnumValue("_9")
      case ChallengeRating._10      => __EnumValue("_10")
      case ChallengeRating._11      => __EnumValue("_11")
      case ChallengeRating._12      => __EnumValue("_12")
      case ChallengeRating._13      => __EnumValue("_13")
      case ChallengeRating._14      => __EnumValue("_14")
      case ChallengeRating._15      => __EnumValue("_15")
      case ChallengeRating._16      => __EnumValue("_16")
      case ChallengeRating._17      => __EnumValue("_17")
      case ChallengeRating._18      => __EnumValue("_18")
      case ChallengeRating._19      => __EnumValue("_19")
      case ChallengeRating._20      => __EnumValue("_20")
      case ChallengeRating._21      => __EnumValue("_21")
      case ChallengeRating._22      => __EnumValue("_22")
      case ChallengeRating._23      => __EnumValue("_23")
      case ChallengeRating._24      => __EnumValue("_24")
      case ChallengeRating._25      => __EnumValue("_25")
      case ChallengeRating._26      => __EnumValue("_26")
      case ChallengeRating._27      => __EnumValue("_27")
      case ChallengeRating._28      => __EnumValue("_28")
      case ChallengeRating._29      => __EnumValue("_29")
      case ChallengeRating._30      => __EnumValue("_30")
    }

    val values: scala.collection.immutable.Vector[ChallengeRating] = scala.collection.immutable.Vector(
      _0,
      _eigth,
      _quarter,
      _half,
      _1,
      _2,
      _3,
      _4,
      _5,
      _6,
      _7,
      _8,
      _9,
      _10,
      _11,
      _12,
      _13,
      _14,
      _15,
      _16,
      _17,
      _18,
      _19,
      _20,
      _21,
      _22,
      _23,
      _24,
      _25,
      _26,
      _27,
      _28,
      _29,
      _30
    )

  }

  sealed trait AbilityType extends scala.Product with scala.Serializable { def value: String }
  object AbilityType {

    case object charisma extends AbilityType { val value: String = "charisma" }
    case object constitution extends AbilityType { val value: String = "constitution" }
    case object dexterity extends AbilityType { val value: String = "dexterity" }
    case object intelligence extends AbilityType { val value: String = "intelligence" }
    case object strength extends AbilityType { val value: String = "strength" }
    case object wisdom extends AbilityType { val value: String = "wisdom" }

    implicit val decoder: ScalarDecoder[AbilityType] = {
      case __StringValue("charisma")     => Right(AbilityType.charisma)
      case __StringValue("constitution") => Right(AbilityType.constitution)
      case __StringValue("dexterity")    => Right(AbilityType.dexterity)
      case __StringValue("intelligence") => Right(AbilityType.intelligence)
      case __StringValue("strength")     => Right(AbilityType.strength)
      case __StringValue("wisdom")       => Right(AbilityType.wisdom)
      case other                         => Left(DecodingError(s"Can't build AbilityType from input $other"))
    }
    implicit val encoder: ArgEncoder[AbilityType] = {
      case AbilityType.charisma     => __EnumValue("charisma")
      case AbilityType.constitution => __EnumValue("constitution")
      case AbilityType.dexterity    => __EnumValue("dexterity")
      case AbilityType.intelligence => __EnumValue("intelligence")
      case AbilityType.strength     => __EnumValue("strength")
      case AbilityType.wisdom       => __EnumValue("wisdom")
    }

    val values: scala.collection.immutable.Vector[AbilityType] =
      scala.collection.immutable.Vector(charisma, constitution, dexterity, intelligence, strength, wisdom)

  }

  sealed trait ActionType extends scala.Product with scala.Serializable { def value: String }
  object ActionType {

    case object Ability extends ActionType { val value: String = "Ability" }
    case object Melee extends ActionType { val value: String = "Melee" }
    case object Ranged extends ActionType { val value: String = "Ranged" }
    case object Spell extends ActionType { val value: String = "Spell" }

    implicit val decoder: ScalarDecoder[ActionType] = {
      case __StringValue("Ability") => Right(ActionType.Ability)
      case __StringValue("Melee")   => Right(ActionType.Melee)
      case __StringValue("Ranged")  => Right(ActionType.Ranged)
      case __StringValue("Spell")   => Right(ActionType.Spell)
      case other                    => Left(DecodingError(s"Can't build ActionType from input $other"))
    }
    implicit val encoder: ArgEncoder[ActionType] = {
      case ActionType.Ability => __EnumValue("Ability")
      case ActionType.Melee   => __EnumValue("Melee")
      case ActionType.Ranged  => __EnumValue("Ranged")
      case ActionType.Spell   => __EnumValue("Spell")
    }

    val values: scala.collection.immutable.Vector[ActionType] =
      scala.collection.immutable.Vector(Ability, Melee, Ranged, Spell)

  }

  sealed trait AdvantageDisadvantage extends scala.Product with scala.Serializable { def value: String }
  object AdvantageDisadvantage {

    case object advantage extends AdvantageDisadvantage { val value: String = "advantage" }
    case object disadvantage extends AdvantageDisadvantage { val value: String = "disadvantage" }
    case object neither extends AdvantageDisadvantage { val value: String = "neither" }

    implicit val decoder: ScalarDecoder[AdvantageDisadvantage] = {
      case __StringValue("advantage")    => Right(AdvantageDisadvantage.advantage)
      case __StringValue("disadvantage") => Right(AdvantageDisadvantage.disadvantage)
      case __StringValue("neither")      => Right(AdvantageDisadvantage.neither)
      case other                         => Left(DecodingError(s"Can't build AdvantageDisadvantage from input $other"))
    }
    implicit val encoder: ArgEncoder[AdvantageDisadvantage] = {
      case AdvantageDisadvantage.advantage    => __EnumValue("advantage")
      case AdvantageDisadvantage.disadvantage => __EnumValue("disadvantage")
      case AdvantageDisadvantage.neither      => __EnumValue("neither")
    }

    val values: scala.collection.immutable.Vector[AdvantageDisadvantage] =
      scala.collection.immutable.Vector(advantage, disadvantage, neither)

  }

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
    case object unknown extends Alignment { val value: String = "unknown" }

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
      case __StringValue("unknown")        => Right(Alignment.unknown)
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
      case Alignment.unknown        => __EnumValue("unknown")
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
      unaligned,
      unknown
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
    case object Unimportant extends Biome { val value: String = "Unimportant" }
    case object Unknown extends Biome { val value: String = "Unknown" }
    case object Urban extends Biome { val value: String = "Urban" }

    implicit val decoder: ScalarDecoder[Biome] = {
      case __StringValue("Arctic")      => Right(Biome.Arctic)
      case __StringValue("Coastal")     => Right(Biome.Coastal)
      case __StringValue("Desert")      => Right(Biome.Desert)
      case __StringValue("Forest")      => Right(Biome.Forest)
      case __StringValue("Grassland")   => Right(Biome.Grassland)
      case __StringValue("Hill")        => Right(Biome.Hill)
      case __StringValue("Mountain")    => Right(Biome.Mountain)
      case __StringValue("Swamp")       => Right(Biome.Swamp)
      case __StringValue("Underdark")   => Right(Biome.Underdark)
      case __StringValue("Underwater")  => Right(Biome.Underwater)
      case __StringValue("Unimportant") => Right(Biome.Unimportant)
      case __StringValue("Unknown")     => Right(Biome.Unknown)
      case __StringValue("Urban")       => Right(Biome.Urban)
      case other                        => Left(DecodingError(s"Can't build Biome from input $other"))
    }
    implicit val encoder: ArgEncoder[Biome] = {
      case Biome.Arctic      => __EnumValue("Arctic")
      case Biome.Coastal     => __EnumValue("Coastal")
      case Biome.Desert      => __EnumValue("Desert")
      case Biome.Forest      => __EnumValue("Forest")
      case Biome.Grassland   => __EnumValue("Grassland")
      case Biome.Hill        => __EnumValue("Hill")
      case Biome.Mountain    => __EnumValue("Mountain")
      case Biome.Swamp       => __EnumValue("Swamp")
      case Biome.Underdark   => __EnumValue("Underdark")
      case Biome.Underwater  => __EnumValue("Underwater")
      case Biome.Unimportant => __EnumValue("Unimportant")
      case Biome.Unknown     => __EnumValue("Unknown")
      case Biome.Urban       => __EnumValue("Urban")
    }

    val values: scala.collection.immutable.Vector[Biome] = scala.collection.immutable.Vector(
      Arctic,
      Coastal,
      Desert,
      Forest,
      Grassland,
      Hill,
      Mountain,
      Swamp,
      Underdark,
      Underwater,
      Unimportant,
      Unknown,
      Urban
    )

  }

  sealed trait Condition extends scala.Product with scala.Serializable { def value: String }
  object Condition {

    case object blinded extends Condition { val value: String = "blinded" }
    case object charmed extends Condition { val value: String = "charmed" }
    case object deafened extends Condition { val value: String = "deafened" }
    case object exhaustion extends Condition { val value: String = "exhaustion" }
    case object frightened extends Condition { val value: String = "frightened" }
    case object grappled extends Condition { val value: String = "grappled" }
    case object incapacitated extends Condition { val value: String = "incapacitated" }
    case object invisible extends Condition { val value: String = "invisible" }
    case object paralyzed extends Condition { val value: String = "paralyzed" }
    case object petrified extends Condition { val value: String = "petrified" }
    case object poisoned extends Condition { val value: String = "poisoned" }
    case object prone extends Condition { val value: String = "prone" }
    case object restrained extends Condition { val value: String = "restrained" }
    case object stunned extends Condition { val value: String = "stunned" }
    case object unconscious extends Condition { val value: String = "unconscious" }

    implicit val decoder: ScalarDecoder[Condition] = {
      case __StringValue("blinded")       => Right(Condition.blinded)
      case __StringValue("charmed")       => Right(Condition.charmed)
      case __StringValue("deafened")      => Right(Condition.deafened)
      case __StringValue("exhaustion")    => Right(Condition.exhaustion)
      case __StringValue("frightened")    => Right(Condition.frightened)
      case __StringValue("grappled")      => Right(Condition.grappled)
      case __StringValue("incapacitated") => Right(Condition.incapacitated)
      case __StringValue("invisible")     => Right(Condition.invisible)
      case __StringValue("paralyzed")     => Right(Condition.paralyzed)
      case __StringValue("petrified")     => Right(Condition.petrified)
      case __StringValue("poisoned")      => Right(Condition.poisoned)
      case __StringValue("prone")         => Right(Condition.prone)
      case __StringValue("restrained")    => Right(Condition.restrained)
      case __StringValue("stunned")       => Right(Condition.stunned)
      case __StringValue("unconscious")   => Right(Condition.unconscious)
      case other                          => Left(DecodingError(s"Can't build Condition from input $other"))
    }
    implicit val encoder: ArgEncoder[Condition] = {
      case Condition.blinded       => __EnumValue("blinded")
      case Condition.charmed       => __EnumValue("charmed")
      case Condition.deafened      => __EnumValue("deafened")
      case Condition.exhaustion    => __EnumValue("exhaustion")
      case Condition.frightened    => __EnumValue("frightened")
      case Condition.grappled      => __EnumValue("grappled")
      case Condition.incapacitated => __EnumValue("incapacitated")
      case Condition.invisible     => __EnumValue("invisible")
      case Condition.paralyzed     => __EnumValue("paralyzed")
      case Condition.petrified     => __EnumValue("petrified")
      case Condition.poisoned      => __EnumValue("poisoned")
      case Condition.prone         => __EnumValue("prone")
      case Condition.restrained    => __EnumValue("restrained")
      case Condition.stunned       => __EnumValue("stunned")
      case Condition.unconscious   => __EnumValue("unconscious")
    }

    val values: scala.collection.immutable.Vector[Condition] = scala.collection.immutable.Vector(
      blinded,
      charmed,
      deafened,
      exhaustion,
      frightened,
      grappled,
      incapacitated,
      invisible,
      paralyzed,
      petrified,
      poisoned,
      prone,
      restrained,
      stunned,
      unconscious
    )

  }

  sealed trait CreatureSize extends scala.Product with scala.Serializable { def value: String }
  object CreatureSize {

    case object gargantuan extends CreatureSize { val value: String = "gargantuan" }
    case object huge extends CreatureSize { val value: String = "huge" }
    case object large extends CreatureSize { val value: String = "large" }
    case object medium extends CreatureSize { val value: String = "medium" }
    case object small extends CreatureSize { val value: String = "small" }
    case object tiny extends CreatureSize { val value: String = "tiny" }
    case object unknown extends CreatureSize { val value: String = "unknown" }

    implicit val decoder: ScalarDecoder[CreatureSize] = {
      case __StringValue("gargantuan") => Right(CreatureSize.gargantuan)
      case __StringValue("huge")       => Right(CreatureSize.huge)
      case __StringValue("large")      => Right(CreatureSize.large)
      case __StringValue("medium")     => Right(CreatureSize.medium)
      case __StringValue("small")      => Right(CreatureSize.small)
      case __StringValue("tiny")       => Right(CreatureSize.tiny)
      case __StringValue("unknown")    => Right(CreatureSize.unknown)
      case other                       => Left(DecodingError(s"Can't build CreatureSize from input $other"))
    }
    implicit val encoder: ArgEncoder[CreatureSize] = {
      case CreatureSize.gargantuan => __EnumValue("gargantuan")
      case CreatureSize.huge       => __EnumValue("huge")
      case CreatureSize.large      => __EnumValue("large")
      case CreatureSize.medium     => __EnumValue("medium")
      case CreatureSize.small      => __EnumValue("small")
      case CreatureSize.tiny       => __EnumValue("tiny")
      case CreatureSize.unknown    => __EnumValue("unknown")
    }

    val values: scala.collection.immutable.Vector[CreatureSize] =
      scala.collection.immutable.Vector(gargantuan, huge, large, medium, small, tiny, unknown)

  }

  sealed trait Lifestyle extends scala.Product with scala.Serializable { def value: String }
  object Lifestyle {

    case object aristocratic extends Lifestyle { val value: String = "aristocratic" }
    case object comfortable extends Lifestyle { val value: String = "comfortable" }
    case object modest extends Lifestyle { val value: String = "modest" }
    case object poor extends Lifestyle { val value: String = "poor" }
    case object squalid extends Lifestyle { val value: String = "squalid" }
    case object unknown extends Lifestyle { val value: String = "unknown" }
    case object wealthy extends Lifestyle { val value: String = "wealthy" }
    case object wretched extends Lifestyle { val value: String = "wretched" }

    implicit val decoder: ScalarDecoder[Lifestyle] = {
      case __StringValue("aristocratic") => Right(Lifestyle.aristocratic)
      case __StringValue("comfortable")  => Right(Lifestyle.comfortable)
      case __StringValue("modest")       => Right(Lifestyle.modest)
      case __StringValue("poor")         => Right(Lifestyle.poor)
      case __StringValue("squalid")      => Right(Lifestyle.squalid)
      case __StringValue("unknown")      => Right(Lifestyle.unknown)
      case __StringValue("wealthy")      => Right(Lifestyle.wealthy)
      case __StringValue("wretched")     => Right(Lifestyle.wretched)
      case other                         => Left(DecodingError(s"Can't build Lifestyle from input $other"))
    }
    implicit val encoder: ArgEncoder[Lifestyle] = {
      case Lifestyle.aristocratic => __EnumValue("aristocratic")
      case Lifestyle.comfortable  => __EnumValue("comfortable")
      case Lifestyle.modest       => __EnumValue("modest")
      case Lifestyle.poor         => __EnumValue("poor")
      case Lifestyle.squalid      => __EnumValue("squalid")
      case Lifestyle.unknown      => __EnumValue("unknown")
      case Lifestyle.wealthy      => __EnumValue("wealthy")
      case Lifestyle.wretched     => __EnumValue("wretched")
    }

    val values: scala.collection.immutable.Vector[Lifestyle] =
      scala.collection.immutable.Vector(aristocratic, comfortable, modest, poor, squalid, unknown, wealthy, wretched)

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

  sealed trait ProficiencyLevel extends scala.Product with scala.Serializable { def value: String }
  object ProficiencyLevel {

    case object expert extends ProficiencyLevel { val value: String = "expert" }
    case object half extends ProficiencyLevel { val value: String = "half" }
    case object none extends ProficiencyLevel { val value: String = "none" }
    case object proficient extends ProficiencyLevel { val value: String = "proficient" }

    implicit val decoder: ScalarDecoder[ProficiencyLevel] = {
      case __StringValue("expert")     => Right(ProficiencyLevel.expert)
      case __StringValue("half")       => Right(ProficiencyLevel.half)
      case __StringValue("none")       => Right(ProficiencyLevel.none)
      case __StringValue("proficient") => Right(ProficiencyLevel.proficient)
      case other                       => Left(DecodingError(s"Can't build ProficiencyLevel from input $other"))
    }
    implicit val encoder: ArgEncoder[ProficiencyLevel] = {
      case ProficiencyLevel.expert     => __EnumValue("expert")
      case ProficiencyLevel.half       => __EnumValue("half")
      case ProficiencyLevel.none       => __EnumValue("none")
      case ProficiencyLevel.proficient => __EnumValue("proficient")
    }

    val values: scala.collection.immutable.Vector[ProficiencyLevel] =
      scala.collection.immutable.Vector(expert, half, none, proficient)

  }

  sealed trait RandomTableType extends scala.Product with scala.Serializable { def value: String }
  object RandomTableType {

    case object adventure extends RandomTableType { val value: String = "adventure" }
    case object encounter extends RandomTableType { val value: String = "encounter" }
    case object environment extends RandomTableType { val value: String = "environment" }
    case object npcs extends RandomTableType { val value: String = "npcs" }
    case object other extends RandomTableType { val value: String = "other" }
    case object treasure extends RandomTableType { val value: String = "treasure" }

    implicit val decoder: ScalarDecoder[RandomTableType] = {
      case __StringValue("adventure")   => Right(RandomTableType.adventure)
      case __StringValue("encounter")   => Right(RandomTableType.encounter)
      case __StringValue("environment") => Right(RandomTableType.environment)
      case __StringValue("npcs")        => Right(RandomTableType.npcs)
      case __StringValue("other")       => Right(RandomTableType.other)
      case __StringValue("treasure")    => Right(RandomTableType.treasure)
      case other                        => Left(DecodingError(s"Can't build RandomTableType from input $other"))
    }
    implicit val encoder: ArgEncoder[RandomTableType] = {
      case RandomTableType.adventure   => __EnumValue("adventure")
      case RandomTableType.encounter   => __EnumValue("encounter")
      case RandomTableType.environment => __EnumValue("environment")
      case RandomTableType.npcs        => __EnumValue("npcs")
      case RandomTableType.other       => __EnumValue("other")
      case RandomTableType.treasure    => __EnumValue("treasure")
    }

    val values: scala.collection.immutable.Vector[RandomTableType] =
      scala.collection.immutable.Vector(adventure, encounter, environment, npcs, other, treasure)

  }

  sealed trait RelationToPlayers extends scala.Product with scala.Serializable { def value: String }
  object RelationToPlayers {

    case object ally extends RelationToPlayers { val value: String = "ally" }
    case object enemy extends RelationToPlayers { val value: String = "enemy" }
    case object itsComplicated extends RelationToPlayers { val value: String = "itsComplicated" }
    case object neutral extends RelationToPlayers { val value: String = "neutral" }
    case object unknown extends RelationToPlayers { val value: String = "unknown" }

    implicit val decoder: ScalarDecoder[RelationToPlayers] = {
      case __StringValue("ally")           => Right(RelationToPlayers.ally)
      case __StringValue("enemy")          => Right(RelationToPlayers.enemy)
      case __StringValue("itsComplicated") => Right(RelationToPlayers.itsComplicated)
      case __StringValue("neutral")        => Right(RelationToPlayers.neutral)
      case __StringValue("unknown")        => Right(RelationToPlayers.unknown)
      case other                           => Left(DecodingError(s"Can't build RelationToPlayers from input $other"))
    }
    implicit val encoder: ArgEncoder[RelationToPlayers] = {
      case RelationToPlayers.ally           => __EnumValue("ally")
      case RelationToPlayers.enemy          => __EnumValue("enemy")
      case RelationToPlayers.itsComplicated => __EnumValue("itsComplicated")
      case RelationToPlayers.neutral        => __EnumValue("neutral")
      case RelationToPlayers.unknown        => __EnumValue("unknown")
    }

    val values: scala.collection.immutable.Vector[RelationToPlayers] =
      scala.collection.immutable.Vector(ally, enemy, itsComplicated, neutral, unknown)

  }

  sealed trait Sense extends scala.Product with scala.Serializable { def value: String }
  object Sense {

    case object blindsight extends Sense { val value: String = "blindsight" }
    case object darkvision extends Sense { val value: String = "darkvision" }
    case object other extends Sense { val value: String = "other" }
    case object scent extends Sense { val value: String = "scent" }
    case object sight extends Sense { val value: String = "sight" }
    case object tremorsense extends Sense { val value: String = "tremorsense" }
    case object truesight extends Sense { val value: String = "truesight" }

    implicit val decoder: ScalarDecoder[Sense] = {
      case __StringValue("blindsight")  => Right(Sense.blindsight)
      case __StringValue("darkvision")  => Right(Sense.darkvision)
      case __StringValue("other")       => Right(Sense.other)
      case __StringValue("scent")       => Right(Sense.scent)
      case __StringValue("sight")       => Right(Sense.sight)
      case __StringValue("tremorsense") => Right(Sense.tremorsense)
      case __StringValue("truesight")   => Right(Sense.truesight)
      case other                        => Left(DecodingError(s"Can't build Sense from input $other"))
    }
    implicit val encoder: ArgEncoder[Sense] = {
      case Sense.blindsight  => __EnumValue("blindsight")
      case Sense.darkvision  => __EnumValue("darkvision")
      case Sense.other       => __EnumValue("other")
      case Sense.scent       => __EnumValue("scent")
      case Sense.sight       => __EnumValue("sight")
      case Sense.tremorsense => __EnumValue("tremorsense")
      case Sense.truesight   => __EnumValue("truesight")
    }

    val values: scala.collection.immutable.Vector[Sense] =
      scala.collection.immutable.Vector(blindsight, darkvision, other, scent, sight, tremorsense, truesight)

  }

  sealed trait SkillType extends scala.Product with scala.Serializable { def value: String }
  object SkillType {

    case object acrobatics extends SkillType { val value: String = "acrobatics" }
    case object animalHandling extends SkillType { val value: String = "animalHandling" }
    case object arcana extends SkillType { val value: String = "arcana" }
    case object athletics extends SkillType { val value: String = "athletics" }
    case object deception extends SkillType { val value: String = "deception" }
    case object history extends SkillType { val value: String = "history" }
    case object insight extends SkillType { val value: String = "insight" }
    case object intimidation extends SkillType { val value: String = "intimidation" }
    case object investigation extends SkillType { val value: String = "investigation" }
    case object medicine extends SkillType { val value: String = "medicine" }
    case object nature extends SkillType { val value: String = "nature" }
    case object perception extends SkillType { val value: String = "perception" }
    case object performance extends SkillType { val value: String = "performance" }
    case object persuasion extends SkillType { val value: String = "persuasion" }
    case object religion extends SkillType { val value: String = "religion" }
    case object sleightOfHand extends SkillType { val value: String = "sleightOfHand" }
    case object stealth extends SkillType { val value: String = "stealth" }
    case object survival extends SkillType { val value: String = "survival" }

    implicit val decoder: ScalarDecoder[SkillType] = {
      case __StringValue("acrobatics")     => Right(SkillType.acrobatics)
      case __StringValue("animalHandling") => Right(SkillType.animalHandling)
      case __StringValue("arcana")         => Right(SkillType.arcana)
      case __StringValue("athletics")      => Right(SkillType.athletics)
      case __StringValue("deception")      => Right(SkillType.deception)
      case __StringValue("history")        => Right(SkillType.history)
      case __StringValue("insight")        => Right(SkillType.insight)
      case __StringValue("intimidation")   => Right(SkillType.intimidation)
      case __StringValue("investigation")  => Right(SkillType.investigation)
      case __StringValue("medicine")       => Right(SkillType.medicine)
      case __StringValue("nature")         => Right(SkillType.nature)
      case __StringValue("perception")     => Right(SkillType.perception)
      case __StringValue("performance")    => Right(SkillType.performance)
      case __StringValue("persuasion")     => Right(SkillType.persuasion)
      case __StringValue("religion")       => Right(SkillType.religion)
      case __StringValue("sleightOfHand")  => Right(SkillType.sleightOfHand)
      case __StringValue("stealth")        => Right(SkillType.stealth)
      case __StringValue("survival")       => Right(SkillType.survival)
      case other                           => Left(DecodingError(s"Can't build SkillType from input $other"))
    }
    implicit val encoder: ArgEncoder[SkillType] = {
      case SkillType.acrobatics     => __EnumValue("acrobatics")
      case SkillType.animalHandling => __EnumValue("animalHandling")
      case SkillType.arcana         => __EnumValue("arcana")
      case SkillType.athletics      => __EnumValue("athletics")
      case SkillType.deception      => __EnumValue("deception")
      case SkillType.history        => __EnumValue("history")
      case SkillType.insight        => __EnumValue("insight")
      case SkillType.intimidation   => __EnumValue("intimidation")
      case SkillType.investigation  => __EnumValue("investigation")
      case SkillType.medicine       => __EnumValue("medicine")
      case SkillType.nature         => __EnumValue("nature")
      case SkillType.perception     => __EnumValue("perception")
      case SkillType.performance    => __EnumValue("performance")
      case SkillType.persuasion     => __EnumValue("persuasion")
      case SkillType.religion       => __EnumValue("religion")
      case SkillType.sleightOfHand  => __EnumValue("sleightOfHand")
      case SkillType.stealth        => __EnumValue("stealth")
      case SkillType.survival       => __EnumValue("survival")
    }

    val values: scala.collection.immutable.Vector[SkillType] = scala.collection.immutable.Vector(
      acrobatics,
      animalHandling,
      arcana,
      athletics,
      deception,
      history,
      insight,
      intimidation,
      investigation,
      medicine,
      nature,
      perception,
      performance,
      persuasion,
      religion,
      sleightOfHand,
      stealth,
      survival
    )

  }

  sealed trait SpeedType extends scala.Product with scala.Serializable { def value: String }
  object SpeedType {

    case object burrow extends SpeedType { val value: String = "burrow" }
    case object climb extends SpeedType { val value: String = "climb" }
    case object fly extends SpeedType { val value: String = "fly" }
    case object swim extends SpeedType { val value: String = "swim" }
    case object walk extends SpeedType { val value: String = "walk" }

    implicit val decoder: ScalarDecoder[SpeedType] = {
      case __StringValue("burrow") => Right(SpeedType.burrow)
      case __StringValue("climb")  => Right(SpeedType.climb)
      case __StringValue("fly")    => Right(SpeedType.fly)
      case __StringValue("swim")   => Right(SpeedType.swim)
      case __StringValue("walk")   => Right(SpeedType.walk)
      case other                   => Left(DecodingError(s"Can't build SpeedType from input $other"))
    }
    implicit val encoder: ArgEncoder[SpeedType] = {
      case SpeedType.burrow => __EnumValue("burrow")
      case SpeedType.climb  => __EnumValue("climb")
      case SpeedType.fly    => __EnumValue("fly")
      case SpeedType.swim   => __EnumValue("swim")
      case SpeedType.walk   => __EnumValue("walk")
    }

    val values: scala.collection.immutable.Vector[SpeedType] =
      scala.collection.immutable.Vector(burrow, climb, fly, swim, walk)

  }

  type Abilities
  object Abilities {

    final case class AbilitiesView[
      StrengthSelection,
      DexteritySelection,
      ConstitutionSelection,
      IntelligenceSelection,
      WisdomSelection,
      CharismaSelection
    ](
      strength:     StrengthSelection,
      dexterity:    DexteritySelection,
      constitution: ConstitutionSelection,
      intelligence: IntelligenceSelection,
      wisdom:       WisdomSelection,
      charisma:     CharismaSelection
    )

    type ViewSelection[
      StrengthSelection,
      DexteritySelection,
      ConstitutionSelection,
      IntelligenceSelection,
      WisdomSelection,
      CharismaSelection
    ] = SelectionBuilder[
      Abilities,
      AbilitiesView[
        StrengthSelection,
        DexteritySelection,
        ConstitutionSelection,
        IntelligenceSelection,
        WisdomSelection,
        CharismaSelection
      ]
    ]

    def view[
      StrengthSelection,
      DexteritySelection,
      ConstitutionSelection,
      IntelligenceSelection,
      WisdomSelection,
      CharismaSelection
    ](
      strengthSelection:     SelectionBuilder[Ability, StrengthSelection],
      dexteritySelection:    SelectionBuilder[Ability, DexteritySelection],
      constitutionSelection: SelectionBuilder[Ability, ConstitutionSelection],
      intelligenceSelection: SelectionBuilder[Ability, IntelligenceSelection],
      wisdomSelection:       SelectionBuilder[Ability, WisdomSelection],
      charismaSelection:     SelectionBuilder[Ability, CharismaSelection]
    ): ViewSelection[
      StrengthSelection,
      DexteritySelection,
      ConstitutionSelection,
      IntelligenceSelection,
      WisdomSelection,
      CharismaSelection
    ] =
      (strength(strengthSelection) ~ dexterity(dexteritySelection) ~ constitution(constitutionSelection) ~ intelligence(
        intelligenceSelection
      ) ~ wisdom(wisdomSelection) ~ charisma(charismaSelection)).map {
        case (strength, dexterity, constitution, intelligence, wisdom, charisma) =>
          AbilitiesView(strength, dexterity, constitution, intelligence, wisdom, charisma)
      }

    def strength[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("strength", Obj(innerSelection))
    def dexterity[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("dexterity", Obj(innerSelection))
    def constitution[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("constitution", Obj(innerSelection))
    def intelligence[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("intelligence", Obj(innerSelection))
    def wisdom[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("wisdom", Obj(innerSelection))
    def charisma[A](innerSelection: SelectionBuilder[Ability, A]): SelectionBuilder[Abilities, A] =
      _root_.caliban.client.SelectionBuilder.Field("charisma", Obj(innerSelection))

  }

  type Ability
  object Ability {

    final case class AbilityView(
      abilityType:   AbilityType,
      value:         Int,
      overrideValue: scala.Option[Int],
      isProficient:  Boolean
    )

    type ViewSelection = SelectionBuilder[Ability, AbilityView]

    def view: ViewSelection =
      (abilityType ~ value ~ overrideValue ~ isProficient).map {
        case (abilityType, value, overrideValue, isProficient) =>
          AbilityView(abilityType, value, overrideValue, isProficient)
      }

    def abilityType: SelectionBuilder[Ability, AbilityType] =
      _root_.caliban.client.SelectionBuilder.Field("abilityType", Scalar())
    def value: SelectionBuilder[Ability, Int] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())
    def overrideValue: SelectionBuilder[Ability, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("overrideValue", OptionOf(Scalar()))
    def isProficient: SelectionBuilder[Ability, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("isProficient", Scalar())

  }

  type ActionDC
  object ActionDC {

    final case class ActionDCView(
      dcType:  AbilityType,
      dcValue: Int
    )

    type ViewSelection = SelectionBuilder[ActionDC, ActionDCView]

    def view: ViewSelection = (dcType ~ dcValue).map { case (dcType, dcValue) => ActionDCView(dcType, dcValue) }

    def dcType: SelectionBuilder[ActionDC, AbilityType] =
      _root_.caliban.client.SelectionBuilder.Field("dcType", Scalar())
    def dcValue: SelectionBuilder[ActionDC, Int] = _root_.caliban.client.SelectionBuilder.Field("dcValue", Scalar())

  }

  type Background
  object Background {

    final case class BackgroundView(name: String)

    type ViewSelection = SelectionBuilder[Background, BackgroundView]

    def view: ViewSelection = name.map(name => BackgroundView(name))

    def name: SelectionBuilder[Background, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type CharacterClass
  object CharacterClass {

    final case class CharacterClassView[HitDiceSelection](
      id:      String,
      hitDice: HitDiceSelection
    )

    type ViewSelection[HitDiceSelection] = SelectionBuilder[CharacterClass, CharacterClassView[HitDiceSelection]]

    def view[HitDiceSelection](hitDiceSelection: SelectionBuilder[DiceRoll, HitDiceSelection])
      : ViewSelection[HitDiceSelection] =
      (id ~ hitDice(hitDiceSelection)).map { case (id, hitDice) => CharacterClassView(id, hitDice) }

    def id: SelectionBuilder[CharacterClass, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def hitDice[A](innerSelection: SelectionBuilder[DiceRoll, A]): SelectionBuilder[CharacterClass, A] =
      _root_.caliban.client.SelectionBuilder.Field("hitDice", Obj(innerSelection))

  }

  type Creature
  object Creature {

    final case class CreatureView(
      name:         String,
      creatureType: Long
    )

    type ViewSelection = SelectionBuilder[Creature, CreatureView]

    def view: ViewSelection =
      (name ~ creatureType).map { case (name, creatureType) => CreatureView(name, creatureType) }

    def name: SelectionBuilder[Creature, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def creatureType: SelectionBuilder[Creature, Long] =
      _root_.caliban.client.SelectionBuilder.Field("creatureType", Scalar())

  }

  type DamageType
  object DamageType {

    final case class DamageTypeView(description: String)

    type ViewSelection = SelectionBuilder[DamageType, DamageTypeView]

    def view: ViewSelection = description.map(description => DamageTypeView(description))

    def description: SelectionBuilder[DamageType, String] =
      _root_.caliban.client.SelectionBuilder.Field("description", Scalar())

  }

  type DeathSave
  object DeathSave {

    final case class DeathSaveView(
      fails:        Int,
      successes:    Int,
      isStabilized: Boolean
    )

    type ViewSelection = SelectionBuilder[DeathSave, DeathSaveView]

    def view: ViewSelection =
      (fails ~ successes ~ isStabilized).map { case (fails, successes, isStabilized) =>
        DeathSaveView(fails, successes, isStabilized)
      }

    def fails: SelectionBuilder[DeathSave, Int] = _root_.caliban.client.SelectionBuilder.Field("fails", Scalar())
    def successes: SelectionBuilder[DeathSave, Int] =
      _root_.caliban.client.SelectionBuilder.Field("successes", Scalar())
    def isStabilized: SelectionBuilder[DeathSave, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("isStabilized", Scalar())

  }

  type DiceRoll
  object DiceRoll {

    final case class DiceRollView(roll: String)

    type ViewSelection = SelectionBuilder[DiceRoll, DiceRollView]

    def view: ViewSelection = roll.map(roll => DiceRollView(roll))

    def roll: SelectionBuilder[DiceRoll, String] = _root_.caliban.client.SelectionBuilder.Field("roll", Scalar())

  }

  type Encounter
  object Encounter {

    final case class EncounterView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[Encounter, EncounterView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[EncounterHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        EncounterView(header, jsonInfo, version)
      }

    def header[A](innerSelection: SelectionBuilder[EncounterHeader, A]): SelectionBuilder[Encounter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Encounter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Encounter, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type EncounterHeader
  object EncounterHeader {

    final case class EncounterHeaderView(
      id:         Long,
      campaignId: Long,
      name:       String,
      status:     String,
      sceneId:    scala.Option[Long],
      orderCol:   Int
    )

    type ViewSelection = SelectionBuilder[EncounterHeader, EncounterHeaderView]

    def view: ViewSelection =
      (id ~ campaignId ~ name ~ status ~ sceneId ~ orderCol).map {
        case (id, campaignId, name, status, sceneId, orderCol) =>
          EncounterHeaderView(id, campaignId, name, status, sceneId, orderCol)
      }

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

  type Feat
  object Feat {

    final case class FeatView(name: String)

    type ViewSelection = SelectionBuilder[Feat, FeatView]

    def view: ViewSelection = name.map(name => FeatView(name))

    def name: SelectionBuilder[Feat, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Health
  object Health {

    final case class HealthView[DeathSaveSelection](
      deathSave:            DeathSaveSelection,
      currentHitPoints:     Int,
      maxHitPoints:         Int,
      overrideMaxHitPoints: scala.Option[Int],
      temporaryHitPoints:   Int
    )

    type ViewSelection[DeathSaveSelection] = SelectionBuilder[Health, HealthView[DeathSaveSelection]]

    def view[DeathSaveSelection](deathSaveSelection: SelectionBuilder[DeathSave, DeathSaveSelection])
      : ViewSelection[DeathSaveSelection] =
      (deathSave(deathSaveSelection) ~ currentHitPoints ~ maxHitPoints ~ overrideMaxHitPoints ~ temporaryHitPoints)
        .map { case (deathSave, currentHitPoints, maxHitPoints, overrideMaxHitPoints, temporaryHitPoints) =>
          HealthView(deathSave, currentHitPoints, maxHitPoints, overrideMaxHitPoints, temporaryHitPoints)
        }

    def deathSave[A](innerSelection: SelectionBuilder[DeathSave, A]): SelectionBuilder[Health, A] =
      _root_.caliban.client.SelectionBuilder.Field("deathSave", Obj(innerSelection))
    def currentHitPoints: SelectionBuilder[Health, Int] =
      _root_.caliban.client.SelectionBuilder.Field("currentHitPoints", Scalar())
    def maxHitPoints: SelectionBuilder[Health, Int] =
      _root_.caliban.client.SelectionBuilder.Field("maxHitPoints", Scalar())
    def overrideMaxHitPoints: SelectionBuilder[Health, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("overrideMaxHitPoints", OptionOf(Scalar()))
    def temporaryHitPoints: SelectionBuilder[Health, Int] =
      _root_.caliban.client.SelectionBuilder.Field("temporaryHitPoints", Scalar())

  }

  type InventoryItem
  object InventoryItem {

    final case class InventoryItemView(
      name:     String,
      quantity: Int
    )

    type ViewSelection = SelectionBuilder[InventoryItem, InventoryItemView]

    def view: ViewSelection = (name ~ quantity).map { case (name, quantity) => InventoryItemView(name, quantity) }

    def name: SelectionBuilder[InventoryItem, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def quantity: SelectionBuilder[InventoryItem, Int] =
      _root_.caliban.client.SelectionBuilder.Field("quantity", Scalar())

  }

  type KVLongListOfLong
  object KVLongListOfLong {

    final case class KVLongListOfLongView(
      key:   Long,
      value: List[Long]
    )

    type ViewSelection = SelectionBuilder[KVLongListOfLong, KVLongListOfLongView]

    def view: ViewSelection = (key ~ value).map { case (key, value) => KVLongListOfLongView(key, value) }

    /** Key
      */
    def key: SelectionBuilder[KVLongListOfLong, Long] = _root_.caliban.client.SelectionBuilder.Field("key", Scalar())

    /** Value
      */
    def value: SelectionBuilder[KVLongListOfLong, List[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("value", ListOf(Scalar()))

  }

  type Language
  object Language {

    final case class LanguageView(name: String)

    type ViewSelection = SelectionBuilder[Language, LanguageView]

    def view: ViewSelection = name.map(name => LanguageView(name))

    def name: SelectionBuilder[Language, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Monster
  object Monster {

    final case class MonsterView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[Monster, MonsterView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[MonsterHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        MonsterView(header, jsonInfo, version)
      }

    def header[A](innerSelection: SelectionBuilder[MonsterHeader, A]): SelectionBuilder[Monster, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Monster, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Monster, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type MonsterHeader
  object MonsterHeader {

    final case class MonsterHeaderView(
      id:               Long,
      sourceId:         String,
      name:             String,
      monsterType:      MonsterType,
      biome:            scala.Option[Biome],
      alignment:        scala.Option[Alignment],
      cr:               ChallengeRating,
      xp:               Long,
      armorClass:       Int,
      maximumHitPoints: Int,
      size:             CreatureSize,
      initiativeBonus:  Int
    )

    type ViewSelection = SelectionBuilder[MonsterHeader, MonsterHeaderView]

    def view: ViewSelection =
      (id ~ sourceId ~ name ~ monsterType ~ biome ~ alignment ~ cr ~ xp ~ armorClass ~ maximumHitPoints ~ size ~ initiativeBonus)
        .map {
          case (
                id,
                sourceId,
                name,
                monsterType,
                biome,
                alignment,
                cr,
                xp,
                armorClass,
                maximumHitPoints,
                size,
                initiativeBonus
              ) =>
            MonsterHeaderView(
              id,
              sourceId,
              name,
              monsterType,
              biome,
              alignment,
              cr,
              xp,
              armorClass,
              maximumHitPoints,
              size,
              initiativeBonus
            )
        }

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
    def cr: SelectionBuilder[MonsterHeader, ChallengeRating] =
      _root_.caliban.client.SelectionBuilder.Field("cr", Scalar())
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

    final case class MonsterSearchResultsView[ResultsSelection](
      results: List[ResultsSelection],
      total:   Long
    )

    type ViewSelection[ResultsSelection] =
      SelectionBuilder[MonsterSearchResults, MonsterSearchResultsView[ResultsSelection]]

    def view[ResultsSelection](resultsSelection: SelectionBuilder[MonsterHeader, ResultsSelection])
      : ViewSelection[ResultsSelection] =
      (results(resultsSelection) ~ total).map { case (results, total) => MonsterSearchResultsView(results, total) }

    def results[A](innerSelection: SelectionBuilder[MonsterHeader, A])
      : SelectionBuilder[MonsterSearchResults, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("results", ListOf(Obj(innerSelection)))
    def total: SelectionBuilder[MonsterSearchResults, Long] =
      _root_.caliban.client.SelectionBuilder.Field("total", Scalar())

  }

  type MultiAction
  object MultiAction {

    final case class MultiActionView[ActionsSelection](
      name:        String,
      description: scala.Option[String],
      actions:     List[ActionsSelection]
    )

    type ViewSelection[ActionsSelection] = SelectionBuilder[MultiAction, MultiActionView[ActionsSelection]]

    def view[ActionsSelection](actionsSelection: SelectionBuilder[SingleAction, ActionsSelection])
      : ViewSelection[ActionsSelection] =
      (name ~ description ~ actions(actionsSelection)).map { case (name, description, actions) =>
        MultiActionView(name, description, actions)
      }

    def name: SelectionBuilder[MultiAction, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def description: SelectionBuilder[MultiAction, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("description", OptionOf(Scalar()))
    def actions[A](innerSelection: SelectionBuilder[SingleAction, A]): SelectionBuilder[MultiAction, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("actions", ListOf(Obj(innerSelection)))

  }

  type NonPlayerCharacter
  object NonPlayerCharacter {

    final case class NonPlayerCharacterView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[NonPlayerCharacter, NonPlayerCharacterView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[NonPlayerCharacterHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        NonPlayerCharacterView(header, jsonInfo, version)
      }

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

    final case class NonPlayerCharacterHeaderView(
      id:         Long,
      campaignId: Long,
      name:       String
    )

    type ViewSelection = SelectionBuilder[NonPlayerCharacterHeader, NonPlayerCharacterHeaderView]

    def view: ViewSelection =
      (id ~ campaignId ~ name).map { case (id, campaignId, name) => NonPlayerCharacterHeaderView(id, campaignId, name) }

    def id: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[NonPlayerCharacterHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[NonPlayerCharacterHeader, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type NonPlayerCharacterInfo
  object NonPlayerCharacterInfo {

    final case class NonPlayerCharacterInfoView[
      HealthSelection,
      ClassesSelection,
      PhysicalCharacteristicsSelection,
      AbilitiesSelection,
      SkillsSelection,
      BackgroundSelection,
      RaceSelection,
      InventorySelection,
      WalletSelection,
      FeatsSelection,
      SpellSlotsSelection,
      PactMagicSelection,
      LanguagesSelection,
      ActionsSelection,
      ClassSpellsSelection,
      CreaturesSelection,
      SpeedsSelection,
      SensesSelection,
      DamageVulnerabilitiesSelection,
      DamageResistancesSelection,
      DamageImmunitiesSelection,
      RollplayInfoSelection,
      TraitsSelection
    ](
      health:                  HealthSelection,
      armorClass:              Int,
      classes:                 List[ClassesSelection],
      physicalCharacteristics: PhysicalCharacteristicsSelection,
      faith:                   scala.Option[String],
      overrideInitiative:      scala.Option[Int],
      currentXp:               scala.Option[Long],
      alignment:               Alignment,
      lifestyle:               Lifestyle,
      abilities:               AbilitiesSelection,
      skills:                  SkillsSelection,
      background:              scala.Option[BackgroundSelection],
      race:                    RaceSelection,
      size:                    CreatureSize,
      inventory:               List[InventorySelection],
      wallet:                  WalletSelection,
      feats:                   List[FeatsSelection],
      conditions:              List[Condition],
      spellSlots:              List[SpellSlotsSelection],
      pactMagic:               List[PactMagicSelection],
      languages:               List[LanguagesSelection],
      actions:                 List[ActionsSelection],
      classSpells:             List[ClassSpellsSelection],
      creatures:               List[CreaturesSelection],
      speeds:                  List[SpeedsSelection],
      senses:                  List[SensesSelection],
      hair:                    String,
      skin:                    String,
      eyes:                    String,
      height:                  String,
      weight:                  String,
      age:                     String,
      gender:                  String,
      conditionImmunities:     List[Condition],
      damageVulnerabilities:   List[DamageVulnerabilitiesSelection],
      damageResistances:       List[DamageResistancesSelection],
      damageImmunities:        List[DamageImmunitiesSelection],
      notes:                   String,
      rollplayInfo:            RollplayInfoSelection,
      monster:                 scala.Option[Long],
      challengeRating:         scala.Option[ChallengeRating],
      relationToPlayers:       RelationToPlayers,
      traits:                  TraitsSelection,
      organizations:           String,
      allies:                  String,
      enemies:                 String,
      backstory:               String
    )

    final case class NonPlayerCharacterInfoViewSelectionArgs[
      HealthSelection,
      ClassesSelection,
      PhysicalCharacteristicsSelection,
      AbilitiesSelection,
      SkillsSelection,
      BackgroundSelection,
      RaceSelection,
      InventorySelection,
      WalletSelection,
      FeatsSelection,
      SpellSlotsSelection,
      PactMagicSelection,
      LanguagesSelection,
      ActionsSelection,
      ClassSpellsSelection,
      CreaturesSelection,
      SpeedsSelection,
      SensesSelection,
      DamageVulnerabilitiesSelection,
      DamageResistancesSelection,
      DamageImmunitiesSelection,
      RollplayInfoSelection,
      TraitsSelection
    ](
      healthSelection:                  SelectionBuilder[Health, HealthSelection],
      classesSelection:                 SelectionBuilder[PlayerCharacterClass, ClassesSelection],
      physicalCharacteristicsSelection: SelectionBuilder[PhysicalCharacteristics, PhysicalCharacteristicsSelection],
      abilitiesSelection:               SelectionBuilder[Abilities, AbilitiesSelection],
      skillsSelection:                  SelectionBuilder[Skills, SkillsSelection],
      backgroundSelection:              SelectionBuilder[Background, BackgroundSelection],
      raceSelection:                    SelectionBuilder[Race, RaceSelection],
      inventorySelection:               SelectionBuilder[InventoryItem, InventorySelection],
      walletSelection:                  SelectionBuilder[Wallet, WalletSelection],
      featsSelection:                   SelectionBuilder[Feat, FeatsSelection],
      spellSlotsSelection:              SelectionBuilder[SpellSlots, SpellSlotsSelection],
      pactMagicSelection:               SelectionBuilder[SpellSlots, PactMagicSelection],
      languagesSelection:               SelectionBuilder[Language, LanguagesSelection],
      actionsSelectionOnMultiAction:    SelectionBuilder[MultiAction, ActionsSelection],
      actionsSelectionOnSingleAction:   SelectionBuilder[SingleAction, ActionsSelection],
      classSpellsSelection:             SelectionBuilder[SpellHeader, ClassSpellsSelection],
      creaturesSelection:               SelectionBuilder[Creature, CreaturesSelection],
      speedsSelection:                  SelectionBuilder[Speed, SpeedsSelection],
      sensesSelection:                  SelectionBuilder[SenseRange, SensesSelection],
      damageVulnerabilitiesSelection:   SelectionBuilder[DamageType, DamageVulnerabilitiesSelection],
      damageResistancesSelection:       SelectionBuilder[DamageType, DamageResistancesSelection],
      damageImmunitiesSelection:        SelectionBuilder[DamageType, DamageImmunitiesSelection],
      rollplayInfoSelection:            SelectionBuilder[RollplayInfo, RollplayInfoSelection],
      traitsSelection:                  SelectionBuilder[Traits, TraitsSelection]
    )

    type ViewSelection[
      HealthSelection,
      ClassesSelection,
      PhysicalCharacteristicsSelection,
      AbilitiesSelection,
      SkillsSelection,
      BackgroundSelection,
      RaceSelection,
      InventorySelection,
      WalletSelection,
      FeatsSelection,
      SpellSlotsSelection,
      PactMagicSelection,
      LanguagesSelection,
      ActionsSelection,
      ClassSpellsSelection,
      CreaturesSelection,
      SpeedsSelection,
      SensesSelection,
      DamageVulnerabilitiesSelection,
      DamageResistancesSelection,
      DamageImmunitiesSelection,
      RollplayInfoSelection,
      TraitsSelection
    ] = SelectionBuilder[
      NonPlayerCharacterInfo,
      NonPlayerCharacterInfoView[
        HealthSelection,
        ClassesSelection,
        PhysicalCharacteristicsSelection,
        AbilitiesSelection,
        SkillsSelection,
        BackgroundSelection,
        RaceSelection,
        InventorySelection,
        WalletSelection,
        FeatsSelection,
        SpellSlotsSelection,
        PactMagicSelection,
        LanguagesSelection,
        ActionsSelection,
        ClassSpellsSelection,
        CreaturesSelection,
        SpeedsSelection,
        SensesSelection,
        DamageVulnerabilitiesSelection,
        DamageResistancesSelection,
        DamageImmunitiesSelection,
        RollplayInfoSelection,
        TraitsSelection
      ]
    ]

    def view[
      HealthSelection,
      ClassesSelection,
      PhysicalCharacteristicsSelection,
      AbilitiesSelection,
      SkillsSelection,
      BackgroundSelection,
      RaceSelection,
      InventorySelection,
      WalletSelection,
      FeatsSelection,
      SpellSlotsSelection,
      PactMagicSelection,
      LanguagesSelection,
      ActionsSelection,
      ClassSpellsSelection,
      CreaturesSelection,
      SpeedsSelection,
      SensesSelection,
      DamageVulnerabilitiesSelection,
      DamageResistancesSelection,
      DamageImmunitiesSelection,
      RollplayInfoSelection,
      TraitsSelection
    ](
      selectionArgs: NonPlayerCharacterInfoViewSelectionArgs[
        HealthSelection,
        ClassesSelection,
        PhysicalCharacteristicsSelection,
        AbilitiesSelection,
        SkillsSelection,
        BackgroundSelection,
        RaceSelection,
        InventorySelection,
        WalletSelection,
        FeatsSelection,
        SpellSlotsSelection,
        PactMagicSelection,
        LanguagesSelection,
        ActionsSelection,
        ClassSpellsSelection,
        CreaturesSelection,
        SpeedsSelection,
        SensesSelection,
        DamageVulnerabilitiesSelection,
        DamageResistancesSelection,
        DamageImmunitiesSelection,
        RollplayInfoSelection,
        TraitsSelection
      ]
    ): ViewSelection[
      HealthSelection,
      ClassesSelection,
      PhysicalCharacteristicsSelection,
      AbilitiesSelection,
      SkillsSelection,
      BackgroundSelection,
      RaceSelection,
      InventorySelection,
      WalletSelection,
      FeatsSelection,
      SpellSlotsSelection,
      PactMagicSelection,
      LanguagesSelection,
      ActionsSelection,
      ClassSpellsSelection,
      CreaturesSelection,
      SpeedsSelection,
      SensesSelection,
      DamageVulnerabilitiesSelection,
      DamageResistancesSelection,
      DamageImmunitiesSelection,
      RollplayInfoSelection,
      TraitsSelection
    ] =
      ((health(selectionArgs.healthSelection) ~ armorClass ~ classes(
        selectionArgs.classesSelection
      ) ~ physicalCharacteristics(
        selectionArgs.physicalCharacteristicsSelection
      ) ~ faith ~ overrideInitiative ~ currentXp ~ alignment ~ lifestyle ~ abilities(
        selectionArgs.abilitiesSelection
      ) ~ skills(selectionArgs.skillsSelection) ~ background(selectionArgs.backgroundSelection) ~ race(
        selectionArgs.raceSelection
      ) ~ size ~ inventory(selectionArgs.inventorySelection) ~ wallet(selectionArgs.walletSelection) ~ feats(
        selectionArgs.featsSelection
      ) ~ conditions ~ spellSlots(selectionArgs.spellSlotsSelection) ~ pactMagic(
        selectionArgs.pactMagicSelection
      ) ~ languages(selectionArgs.languagesSelection) ~ actions(
        selectionArgs.actionsSelectionOnMultiAction,
        selectionArgs.actionsSelectionOnSingleAction
      )) ~ (classSpells(selectionArgs.classSpellsSelection) ~ creatures(selectionArgs.creaturesSelection) ~ speeds(
        selectionArgs.speedsSelection
      ) ~ senses(
        selectionArgs.sensesSelection
      ) ~ hair ~ skin ~ eyes ~ height ~ weight ~ age ~ gender ~ conditionImmunities ~ damageVulnerabilities(
        selectionArgs.damageVulnerabilitiesSelection
      ) ~ damageResistances(selectionArgs.damageResistancesSelection) ~ damageImmunities(
        selectionArgs.damageImmunitiesSelection
      ) ~ notes ~ rollplayInfo(
        selectionArgs.rollplayInfoSelection
      ) ~ monster ~ challengeRating ~ relationToPlayers ~ traits(
        selectionArgs.traitsSelection
      ) ~ organizations) ~ (allies ~ enemies ~ backstory)).map {
        case (
              (
                health,
                armorClass,
                classes,
                physicalCharacteristics,
                faith,
                overrideInitiative,
                currentXp,
                alignment,
                lifestyle,
                abilities,
                skills,
                background,
                race,
                size,
                inventory,
                wallet,
                feats,
                conditions,
                spellSlots,
                pactMagic,
                languages,
                actions
              ),
              (
                classSpells,
                creatures,
                speeds,
                senses,
                hair,
                skin,
                eyes,
                height,
                weight,
                age,
                gender,
                conditionImmunities,
                damageVulnerabilities,
                damageResistances,
                damageImmunities,
                notes,
                rollplayInfo,
                monster,
                challengeRating,
                relationToPlayers,
                traits,
                organizations
              ),
              (allies, enemies, backstory)
            ) =>
          NonPlayerCharacterInfoView(
            health,
            armorClass,
            classes,
            physicalCharacteristics,
            faith,
            overrideInitiative,
            currentXp,
            alignment,
            lifestyle,
            abilities,
            skills,
            background,
            race,
            size,
            inventory,
            wallet,
            feats,
            conditions,
            spellSlots,
            pactMagic,
            languages,
            actions,
            classSpells,
            creatures,
            speeds,
            senses,
            hair,
            skin,
            eyes,
            height,
            weight,
            age,
            gender,
            conditionImmunities,
            damageVulnerabilities,
            damageResistances,
            damageImmunities,
            notes,
            rollplayInfo,
            monster,
            challengeRating,
            relationToPlayers,
            traits,
            organizations,
            allies,
            enemies,
            backstory
          )
      }

    def health[A](innerSelection: SelectionBuilder[Health, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("health", Obj(innerSelection))
    def armorClass: SelectionBuilder[NonPlayerCharacterInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("armorClass", Scalar())
    def classes[A](innerSelection: SelectionBuilder[PlayerCharacterClass, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("classes", ListOf(Obj(innerSelection)))
    def physicalCharacteristics[A](innerSelection: SelectionBuilder[PhysicalCharacteristics, A])
      : SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("physicalCharacteristics", Obj(innerSelection))
    def faith: SelectionBuilder[NonPlayerCharacterInfo, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("faith", OptionOf(Scalar()))
    def overrideInitiative: SelectionBuilder[NonPlayerCharacterInfo, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("overrideInitiative", OptionOf(Scalar()))
    def currentXp: SelectionBuilder[NonPlayerCharacterInfo, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("currentXp", OptionOf(Scalar()))
    def alignment: SelectionBuilder[NonPlayerCharacterInfo, Alignment] =
      _root_.caliban.client.SelectionBuilder.Field("alignment", Scalar())
    def lifestyle: SelectionBuilder[NonPlayerCharacterInfo, Lifestyle] =
      _root_.caliban.client.SelectionBuilder.Field("lifestyle", Scalar())
    def abilities[A](innerSelection: SelectionBuilder[Abilities, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("abilities", Obj(innerSelection))
    def skills[A](innerSelection: SelectionBuilder[Skills, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("skills", Obj(innerSelection))
    def background[A](innerSelection: SelectionBuilder[Background, A])
      : SelectionBuilder[NonPlayerCharacterInfo, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("background", OptionOf(Obj(innerSelection)))
    def race[A](innerSelection: SelectionBuilder[Race, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("race", Obj(innerSelection))
    def size: SelectionBuilder[NonPlayerCharacterInfo, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())
    def inventory[A](innerSelection: SelectionBuilder[InventoryItem, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("inventory", ListOf(Obj(innerSelection)))
    def wallet[A](innerSelection: SelectionBuilder[Wallet, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("wallet", Obj(innerSelection))
    def feats[A](innerSelection: SelectionBuilder[Feat, A]): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("feats", ListOf(Obj(innerSelection)))
    def conditions: SelectionBuilder[NonPlayerCharacterInfo, List[Condition]] =
      _root_.caliban.client.SelectionBuilder.Field("conditions", ListOf(Scalar()))
    def spellSlots[A](innerSelection: SelectionBuilder[SpellSlots, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("spellSlots", ListOf(Obj(innerSelection)))
    def pactMagic[A](innerSelection: SelectionBuilder[SpellSlots, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("pactMagic", ListOf(Obj(innerSelection)))
    def languages[A](innerSelection: SelectionBuilder[Language, A]): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("languages", ListOf(Obj(innerSelection)))
    def actions[A](
      onMultiAction:  SelectionBuilder[MultiAction, A],
      onSingleAction: SelectionBuilder[SingleAction, A]
    ): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "actions",
        ListOf(ChoiceOf(Map("MultiAction" -> Obj(onMultiAction), "SingleAction" -> Obj(onSingleAction))))
      )
    def classSpells[A](innerSelection: SelectionBuilder[SpellHeader, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("classSpells", ListOf(Obj(innerSelection)))
    def creatures[A](innerSelection: SelectionBuilder[Creature, A]): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("creatures", ListOf(Obj(innerSelection)))
    def speeds[A](innerSelection: SelectionBuilder[Speed, A]): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("speeds", ListOf(Obj(innerSelection)))
    def senses[A](innerSelection: SelectionBuilder[SenseRange, A]): SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("senses", ListOf(Obj(innerSelection)))
    def hair: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("hair", Scalar())
    def skin: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("skin", Scalar())
    def eyes: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("eyes", Scalar())
    def height: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("height", Scalar())
    def weight: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("weight", Scalar())
    def age: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("age", Scalar())
    def gender: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("gender", Scalar())
    def conditionImmunities: SelectionBuilder[NonPlayerCharacterInfo, List[Condition]] =
      _root_.caliban.client.SelectionBuilder.Field("conditionImmunities", ListOf(Scalar()))
    def damageVulnerabilities[A](innerSelection: SelectionBuilder[DamageType, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("damageVulnerabilities", ListOf(Obj(innerSelection)))
    def damageResistances[A](innerSelection: SelectionBuilder[DamageType, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("damageResistances", ListOf(Obj(innerSelection)))
    def damageImmunities[A](innerSelection: SelectionBuilder[DamageType, A])
      : SelectionBuilder[NonPlayerCharacterInfo, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("damageImmunities", ListOf(Obj(innerSelection)))
    def notes: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("notes", Scalar())
    def rollplayInfo[A](innerSelection: SelectionBuilder[RollplayInfo, A])
      : SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("rollplayInfo", Obj(innerSelection))
    def monster: SelectionBuilder[NonPlayerCharacterInfo, scala.Option[Long]] =
      _root_.caliban.client.SelectionBuilder.Field("monster", OptionOf(Scalar()))
    def challengeRating: SelectionBuilder[NonPlayerCharacterInfo, scala.Option[ChallengeRating]] =
      _root_.caliban.client.SelectionBuilder.Field("challengeRating", OptionOf(Scalar()))
    def relationToPlayers: SelectionBuilder[NonPlayerCharacterInfo, RelationToPlayers] =
      _root_.caliban.client.SelectionBuilder.Field("relationToPlayers", Scalar())
    def traits[A](innerSelection: SelectionBuilder[Traits, A]): SelectionBuilder[NonPlayerCharacterInfo, A] =
      _root_.caliban.client.SelectionBuilder.Field("traits", Obj(innerSelection))
    def organizations: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("organizations", Scalar())
    def allies: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("allies", Scalar())
    def enemies: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("enemies", Scalar())
    def backstory: SelectionBuilder[NonPlayerCharacterInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("backstory", Scalar())
    def actionsOption[A](
      onMultiAction:  scala.Option[SelectionBuilder[MultiAction, A]] = None,
      onSingleAction: scala.Option[SelectionBuilder[SingleAction, A]] = None
    ): SelectionBuilder[NonPlayerCharacterInfo, List[scala.Option[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "actions",
        ListOf(
          ChoiceOf(
            Map(
              "MultiAction"  -> onMultiAction.fold[FieldBuilder[scala.Option[A]]](NullField)(a => OptionOf(Obj(a))),
              "SingleAction" -> onSingleAction.fold[FieldBuilder[scala.Option[A]]](NullField)(a => OptionOf(Obj(a)))
            )
          )
        )
      )

  }

  type PhysicalCharacteristics
  object PhysicalCharacteristics {

    final case class PhysicalCharacteristicsView(
      gender: scala.Option[String],
      age:    scala.Option[Int],
      hair:   scala.Option[String],
      eyes:   scala.Option[String],
      skin:   scala.Option[String],
      height: scala.Option[String],
      weight: scala.Option[Int],
      size:   CreatureSize
    )

    type ViewSelection = SelectionBuilder[PhysicalCharacteristics, PhysicalCharacteristicsView]

    def view: ViewSelection =
      (gender ~ age ~ hair ~ eyes ~ skin ~ height ~ weight ~ size).map {
        case (gender, age, hair, eyes, skin, height, weight, size) =>
          PhysicalCharacteristicsView(gender, age, hair, eyes, skin, height, weight, size)
      }

    def gender: SelectionBuilder[PhysicalCharacteristics, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("gender", OptionOf(Scalar()))
    def age: SelectionBuilder[PhysicalCharacteristics, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("age", OptionOf(Scalar()))
    def hair: SelectionBuilder[PhysicalCharacteristics, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("hair", OptionOf(Scalar()))
    def eyes: SelectionBuilder[PhysicalCharacteristics, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("eyes", OptionOf(Scalar()))
    def skin: SelectionBuilder[PhysicalCharacteristics, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("skin", OptionOf(Scalar()))
    def height: SelectionBuilder[PhysicalCharacteristics, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("height", OptionOf(Scalar()))
    def weight: SelectionBuilder[PhysicalCharacteristics, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("weight", OptionOf(Scalar()))
    def size: SelectionBuilder[PhysicalCharacteristics, CreatureSize] =
      _root_.caliban.client.SelectionBuilder.Field("size", Scalar())

  }

  type PlayerCharacter
  object PlayerCharacter {

    final case class PlayerCharacterView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[PlayerCharacter, PlayerCharacterView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[PlayerCharacterHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        PlayerCharacterView(header, jsonInfo, version)
      }

    def header[A](innerSelection: SelectionBuilder[PlayerCharacterHeader, A]): SelectionBuilder[PlayerCharacter, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[PlayerCharacter, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[PlayerCharacter, String] =
      _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type PlayerCharacterClass
  object PlayerCharacterClass {

    final case class PlayerCharacterClassView[SubclassSelection](
      characterClass: String,
      subclass:       scala.Option[SubclassSelection],
      level:          Int
    )

    type ViewSelection[SubclassSelection] =
      SelectionBuilder[PlayerCharacterClass, PlayerCharacterClassView[SubclassSelection]]

    def view[SubclassSelection](subclassSelection: SelectionBuilder[SubClass, SubclassSelection])
      : ViewSelection[SubclassSelection] =
      (characterClass ~ subclass(subclassSelection) ~ level).map { case (characterClass, subclass, level) =>
        PlayerCharacterClassView(characterClass, subclass, level)
      }

    def characterClass: SelectionBuilder[PlayerCharacterClass, String] =
      _root_.caliban.client.SelectionBuilder.Field("characterClass", Scalar())
    def subclass[A](innerSelection: SelectionBuilder[SubClass, A])
      : SelectionBuilder[PlayerCharacterClass, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("subclass", OptionOf(Obj(innerSelection)))
    def level: SelectionBuilder[PlayerCharacterClass, Int] =
      _root_.caliban.client.SelectionBuilder.Field("level", Scalar())

  }

  type PlayerCharacterHeader
  object PlayerCharacterHeader {

    final case class PlayerCharacterHeaderView(
      id:         Long,
      campaignId: Long,
      name:       String,
      source:     String,
      playerName: scala.Option[String]
    )

    type ViewSelection = SelectionBuilder[PlayerCharacterHeader, PlayerCharacterHeaderView]

    def view: ViewSelection =
      (id ~ campaignId ~ name ~ source ~ playerName).map { case (id, campaignId, name, source, playerName) =>
        PlayerCharacterHeaderView(id, campaignId, name, source, playerName)
      }

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

    final case class RaceView(name: String)

    type ViewSelection = SelectionBuilder[Race, RaceView]

    def view: ViewSelection = name.map(name => RaceView(name))

    def name: SelectionBuilder[Race, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type RandomTable
  object RandomTable {

    final case class RandomTableView[DiceRollSelection, EntriesSelection](
      id:        Long,
      name:      String,
      tableType: RandomTableType,
      subType:   String,
      diceRoll:  DiceRollSelection,
      entries:   List[EntriesSelection]
    )

    type ViewSelection[DiceRollSelection, EntriesSelection] =
      SelectionBuilder[RandomTable, RandomTableView[DiceRollSelection, EntriesSelection]]

    def view[DiceRollSelection, EntriesSelection](
      diceRollSelection: SelectionBuilder[DiceRoll, DiceRollSelection],
      entriesSelection:  SelectionBuilder[RandomTableEntry, EntriesSelection]
    ): ViewSelection[DiceRollSelection, EntriesSelection] =
      (id ~ name ~ tableType ~ subType ~ diceRoll(diceRollSelection) ~ entries(entriesSelection)).map {
        case (id, name, tableType, subType, diceRoll, entries) =>
          RandomTableView(id, name, tableType, subType, diceRoll, entries)
      }

    def id:   SelectionBuilder[RandomTable, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def name: SelectionBuilder[RandomTable, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def tableType: SelectionBuilder[RandomTable, RandomTableType] =
      _root_.caliban.client.SelectionBuilder.Field("tableType", Scalar())
    def subType: SelectionBuilder[RandomTable, String] =
      _root_.caliban.client.SelectionBuilder.Field("subType", Scalar())
    def diceRoll[A](innerSelection: SelectionBuilder[DiceRoll, A]): SelectionBuilder[RandomTable, A] =
      _root_.caliban.client.SelectionBuilder.Field("diceRoll", Obj(innerSelection))
    def entries[A](innerSelection: SelectionBuilder[RandomTableEntry, A]): SelectionBuilder[RandomTable, List[A]] =
      _root_.caliban.client.SelectionBuilder.Field("entries", ListOf(Obj(innerSelection)))

  }

  type RandomTableEntry
  object RandomTableEntry {

    final case class RandomTableEntryView(
      randomTableId: Long,
      rangeLow:      Int,
      rangeHigh:     Int,
      name:          String,
      description:   String
    )

    type ViewSelection = SelectionBuilder[RandomTableEntry, RandomTableEntryView]

    def view: ViewSelection =
      (randomTableId ~ rangeLow ~ rangeHigh ~ name ~ description).map {
        case (randomTableId, rangeLow, rangeHigh, name, description) =>
          RandomTableEntryView(randomTableId, rangeLow, rangeHigh, name, description)
      }

    def randomTableId: SelectionBuilder[RandomTableEntry, Long] =
      _root_.caliban.client.SelectionBuilder.Field("randomTableId", Scalar())
    def rangeLow: SelectionBuilder[RandomTableEntry, Int] =
      _root_.caliban.client.SelectionBuilder.Field("rangeLow", Scalar())
    def rangeHigh: SelectionBuilder[RandomTableEntry, Int] =
      _root_.caliban.client.SelectionBuilder.Field("rangeHigh", Scalar())
    def name: SelectionBuilder[RandomTableEntry, String] =
      _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def description: SelectionBuilder[RandomTableEntry, String] =
      _root_.caliban.client.SelectionBuilder.Field("description", Scalar())

  }

  type RollplayInfo
  object RollplayInfo {

    final case class RollplayInfoView(
      occupation:     String,
      personality:    String,
      ideal:          String,
      bond:           String,
      flaw:           String,
      characteristic: String,
      speech:         String,
      hobby:          String,
      fear:           String,
      currently:      String,
      nickname:       String,
      weapon:         String,
      rumor:          String,
      raisedBy:       String,
      parent1:        String,
      parent2:        String,
      siblingCount:   Int,
      childhood:      String,
      children:       String,
      spouse:         String
    )

    type ViewSelection = SelectionBuilder[RollplayInfo, RollplayInfoView]

    def view: ViewSelection =
      (occupation ~ personality ~ ideal ~ bond ~ flaw ~ characteristic ~ speech ~ hobby ~ fear ~ currently ~ nickname ~ weapon ~ rumor ~ raisedBy ~ parent1 ~ parent2 ~ siblingCount ~ childhood ~ children ~ spouse)
        .map {
          case (
                occupation,
                personality,
                ideal,
                bond,
                flaw,
                characteristic,
                speech,
                hobby,
                fear,
                currently,
                nickname,
                weapon,
                rumor,
                raisedBy,
                parent1,
                parent2,
                siblingCount,
                childhood,
                children,
                spouse
              ) =>
            RollplayInfoView(
              occupation,
              personality,
              ideal,
              bond,
              flaw,
              characteristic,
              speech,
              hobby,
              fear,
              currently,
              nickname,
              weapon,
              rumor,
              raisedBy,
              parent1,
              parent2,
              siblingCount,
              childhood,
              children,
              spouse
            )
        }

    def occupation: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("occupation", Scalar())
    def personality: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("personality", Scalar())
    def ideal: SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("ideal", Scalar())
    def bond:  SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("bond", Scalar())
    def flaw:  SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("flaw", Scalar())
    def characteristic: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("characteristic", Scalar())
    def speech: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("speech", Scalar())
    def hobby: SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("hobby", Scalar())
    def fear:  SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("fear", Scalar())
    def currently: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("currently", Scalar())
    def nickname: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("nickname", Scalar())
    def weapon: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("weapon", Scalar())
    def rumor: SelectionBuilder[RollplayInfo, String] = _root_.caliban.client.SelectionBuilder.Field("rumor", Scalar())
    def raisedBy: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("raisedBy", Scalar())
    def parent1: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("parent1", Scalar())
    def parent2: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("parent2", Scalar())
    def siblingCount: SelectionBuilder[RollplayInfo, Int] =
      _root_.caliban.client.SelectionBuilder.Field("siblingCount", Scalar())
    def childhood: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("childhood", Scalar())
    def children: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("children", Scalar())
    def spouse: SelectionBuilder[RollplayInfo, String] =
      _root_.caliban.client.SelectionBuilder.Field("spouse", Scalar())

  }

  type Scene
  object Scene {

    final case class SceneView[HeaderSelection](
      header:   HeaderSelection,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )

    type ViewSelection[HeaderSelection] = SelectionBuilder[Scene, SceneView[HeaderSelection]]

    def view[HeaderSelection](headerSelection: SelectionBuilder[SceneHeader, HeaderSelection])
      : ViewSelection[HeaderSelection] =
      (header(headerSelection) ~ jsonInfo ~ version).map { case (header, jsonInfo, version) =>
        SceneView(header, jsonInfo, version)
      }

    def header[A](innerSelection: SelectionBuilder[SceneHeader, A]): SelectionBuilder[Scene, A] =
      _root_.caliban.client.SelectionBuilder.Field("header", Obj(innerSelection))
    def jsonInfo: SelectionBuilder[Scene, zio.json.ast.Json] =
      _root_.caliban.client.SelectionBuilder.Field("jsonInfo", Scalar())
    def version: SelectionBuilder[Scene, String] = _root_.caliban.client.SelectionBuilder.Field("version", Scalar())

  }

  type SceneHeader
  object SceneHeader {

    final case class SceneHeaderView(
      id:         Long,
      campaignId: Long,
      name:       String,
      orderCol:   Int,
      isActive:   Boolean
    )

    type ViewSelection = SelectionBuilder[SceneHeader, SceneHeaderView]

    def view: ViewSelection =
      (id ~ campaignId ~ name ~ orderCol ~ isActive).map { case (id, campaignId, name, orderCol, isActive) =>
        SceneHeaderView(id, campaignId, name, orderCol, isActive)
      }

    def id: SelectionBuilder[SceneHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def campaignId: SelectionBuilder[SceneHeader, Long] =
      _root_.caliban.client.SelectionBuilder.Field("campaignId", Scalar())
    def name: SelectionBuilder[SceneHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def orderCol: SelectionBuilder[SceneHeader, Int] =
      _root_.caliban.client.SelectionBuilder.Field("orderCol", Scalar())
    def isActive: SelectionBuilder[SceneHeader, Boolean] =
      _root_.caliban.client.SelectionBuilder.Field("isActive", Scalar())

  }

  type SenseRange
  object SenseRange {

    final case class SenseRangeView(
      sense: Sense,
      range: Int
    )

    type ViewSelection = SelectionBuilder[SenseRange, SenseRangeView]

    def view: ViewSelection = (sense ~ range).map { case (sense, range) => SenseRangeView(sense, range) }

    def sense: SelectionBuilder[SenseRange, Sense] = _root_.caliban.client.SelectionBuilder.Field("sense", Scalar())
    def range: SelectionBuilder[SenseRange, Int] = _root_.caliban.client.SelectionBuilder.Field("range", Scalar())

  }

  type SingleAction
  object SingleAction {

    final case class SingleActionView[DamageSelection, DcSelection](
      actionType:  ActionType,
      name:        String,
      description: scala.Option[String],
      attackBonus: scala.Option[Int],
      damage:      scala.Option[DamageSelection],
      dc:          scala.Option[DcSelection]
    )

    type ViewSelection[DamageSelection, DcSelection] =
      SelectionBuilder[SingleAction, SingleActionView[DamageSelection, DcSelection]]

    def view[DamageSelection, DcSelection](
      damageSelection: SelectionBuilder[DiceRoll, DamageSelection],
      dcSelection:     SelectionBuilder[ActionDC, DcSelection]
    ): ViewSelection[DamageSelection, DcSelection] =
      (actionType ~ name ~ description ~ attackBonus ~ damage(damageSelection) ~ dc(dcSelection)).map {
        case (actionType, name, description, attackBonus, damage, dc) =>
          SingleActionView(actionType, name, description, attackBonus, damage, dc)
      }

    def actionType: SelectionBuilder[SingleAction, ActionType] =
      _root_.caliban.client.SelectionBuilder.Field("actionType", Scalar())
    def name: SelectionBuilder[SingleAction, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def description: SelectionBuilder[SingleAction, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("description", OptionOf(Scalar()))
    def attackBonus: SelectionBuilder[SingleAction, scala.Option[Int]] =
      _root_.caliban.client.SelectionBuilder.Field("attackBonus", OptionOf(Scalar()))
    def damage[A](innerSelection: SelectionBuilder[DiceRoll, A]): SelectionBuilder[SingleAction, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("damage", OptionOf(Obj(innerSelection)))
    def dc[A](innerSelection: SelectionBuilder[ActionDC, A]): SelectionBuilder[SingleAction, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field("dc", OptionOf(Obj(innerSelection)))

  }

  type Skill
  object Skill {

    final case class SkillView(
      skillType:        SkillType,
      proficiencyLevel: ProficiencyLevel,
      advantage:        AdvantageDisadvantage
    )

    type ViewSelection = SelectionBuilder[Skill, SkillView]

    def view: ViewSelection =
      (skillType ~ proficiencyLevel ~ advantage).map { case (skillType, proficiencyLevel, advantage) =>
        SkillView(skillType, proficiencyLevel, advantage)
      }

    def skillType: SelectionBuilder[Skill, SkillType] =
      _root_.caliban.client.SelectionBuilder.Field("skillType", Scalar())
    def proficiencyLevel: SelectionBuilder[Skill, ProficiencyLevel] =
      _root_.caliban.client.SelectionBuilder.Field("proficiencyLevel", Scalar())
    def advantage: SelectionBuilder[Skill, AdvantageDisadvantage] =
      _root_.caliban.client.SelectionBuilder.Field("advantage", Scalar())

  }

  type Skills
  object Skills {

    final case class SkillsView[
      AcrobaticsSelection,
      AnimalHandlingSelection,
      ArcanaSelection,
      AthleticsSelection,
      DeceptionSelection,
      HistorySelection,
      InsightSelection,
      IntimidationSelection,
      InvestigationSelection,
      MedicineSelection,
      NatureSelection,
      PerceptionSelection,
      PerformanceSelection,
      PersuasionSelection,
      ReligionSelection,
      SleightOfHandSelection,
      StealthSelection,
      SurvivalSelection
    ](
      acrobatics:     AcrobaticsSelection,
      animalHandling: AnimalHandlingSelection,
      arcana:         ArcanaSelection,
      athletics:      AthleticsSelection,
      deception:      DeceptionSelection,
      history:        HistorySelection,
      insight:        InsightSelection,
      intimidation:   IntimidationSelection,
      investigation:  InvestigationSelection,
      medicine:       MedicineSelection,
      nature:         NatureSelection,
      perception:     PerceptionSelection,
      performance:    PerformanceSelection,
      persuasion:     PersuasionSelection,
      religion:       ReligionSelection,
      sleightOfHand:  SleightOfHandSelection,
      stealth:        StealthSelection,
      survival:       SurvivalSelection
    )

    type ViewSelection[
      AcrobaticsSelection,
      AnimalHandlingSelection,
      ArcanaSelection,
      AthleticsSelection,
      DeceptionSelection,
      HistorySelection,
      InsightSelection,
      IntimidationSelection,
      InvestigationSelection,
      MedicineSelection,
      NatureSelection,
      PerceptionSelection,
      PerformanceSelection,
      PersuasionSelection,
      ReligionSelection,
      SleightOfHandSelection,
      StealthSelection,
      SurvivalSelection
    ] = SelectionBuilder[
      Skills,
      SkillsView[
        AcrobaticsSelection,
        AnimalHandlingSelection,
        ArcanaSelection,
        AthleticsSelection,
        DeceptionSelection,
        HistorySelection,
        InsightSelection,
        IntimidationSelection,
        InvestigationSelection,
        MedicineSelection,
        NatureSelection,
        PerceptionSelection,
        PerformanceSelection,
        PersuasionSelection,
        ReligionSelection,
        SleightOfHandSelection,
        StealthSelection,
        SurvivalSelection
      ]
    ]

    def view[
      AcrobaticsSelection,
      AnimalHandlingSelection,
      ArcanaSelection,
      AthleticsSelection,
      DeceptionSelection,
      HistorySelection,
      InsightSelection,
      IntimidationSelection,
      InvestigationSelection,
      MedicineSelection,
      NatureSelection,
      PerceptionSelection,
      PerformanceSelection,
      PersuasionSelection,
      ReligionSelection,
      SleightOfHandSelection,
      StealthSelection,
      SurvivalSelection
    ](
      acrobaticsSelection:     SelectionBuilder[Skill, AcrobaticsSelection],
      animalHandlingSelection: SelectionBuilder[Skill, AnimalHandlingSelection],
      arcanaSelection:         SelectionBuilder[Skill, ArcanaSelection],
      athleticsSelection:      SelectionBuilder[Skill, AthleticsSelection],
      deceptionSelection:      SelectionBuilder[Skill, DeceptionSelection],
      historySelection:        SelectionBuilder[Skill, HistorySelection],
      insightSelection:        SelectionBuilder[Skill, InsightSelection],
      intimidationSelection:   SelectionBuilder[Skill, IntimidationSelection],
      investigationSelection:  SelectionBuilder[Skill, InvestigationSelection],
      medicineSelection:       SelectionBuilder[Skill, MedicineSelection],
      natureSelection:         SelectionBuilder[Skill, NatureSelection],
      perceptionSelection:     SelectionBuilder[Skill, PerceptionSelection],
      performanceSelection:    SelectionBuilder[Skill, PerformanceSelection],
      persuasionSelection:     SelectionBuilder[Skill, PersuasionSelection],
      religionSelection:       SelectionBuilder[Skill, ReligionSelection],
      sleightOfHandSelection:  SelectionBuilder[Skill, SleightOfHandSelection],
      stealthSelection:        SelectionBuilder[Skill, StealthSelection],
      survivalSelection:       SelectionBuilder[Skill, SurvivalSelection]
    ): ViewSelection[
      AcrobaticsSelection,
      AnimalHandlingSelection,
      ArcanaSelection,
      AthleticsSelection,
      DeceptionSelection,
      HistorySelection,
      InsightSelection,
      IntimidationSelection,
      InvestigationSelection,
      MedicineSelection,
      NatureSelection,
      PerceptionSelection,
      PerformanceSelection,
      PersuasionSelection,
      ReligionSelection,
      SleightOfHandSelection,
      StealthSelection,
      SurvivalSelection
    ] =
      (acrobatics(acrobaticsSelection) ~ animalHandling(animalHandlingSelection) ~ arcana(arcanaSelection) ~ athletics(
        athleticsSelection
      ) ~ deception(deceptionSelection) ~ history(historySelection) ~ insight(insightSelection) ~ intimidation(
        intimidationSelection
      ) ~ investigation(investigationSelection) ~ medicine(medicineSelection) ~ nature(natureSelection) ~ perception(
        perceptionSelection
      ) ~ performance(performanceSelection) ~ persuasion(persuasionSelection) ~ religion(
        religionSelection
      ) ~ sleightOfHand(sleightOfHandSelection) ~ stealth(stealthSelection) ~ survival(survivalSelection)).map {
        case (
              acrobatics,
              animalHandling,
              arcana,
              athletics,
              deception,
              history,
              insight,
              intimidation,
              investigation,
              medicine,
              nature,
              perception,
              performance,
              persuasion,
              religion,
              sleightOfHand,
              stealth,
              survival
            ) =>
          SkillsView(
            acrobatics,
            animalHandling,
            arcana,
            athletics,
            deception,
            history,
            insight,
            intimidation,
            investigation,
            medicine,
            nature,
            perception,
            performance,
            persuasion,
            religion,
            sleightOfHand,
            stealth,
            survival
          )
      }

    def acrobatics[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("acrobatics", Obj(innerSelection))
    def animalHandling[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("animalHandling", Obj(innerSelection))
    def arcana[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("arcana", Obj(innerSelection))
    def athletics[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("athletics", Obj(innerSelection))
    def deception[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("deception", Obj(innerSelection))
    def history[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("history", Obj(innerSelection))
    def insight[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("insight", Obj(innerSelection))
    def intimidation[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("intimidation", Obj(innerSelection))
    def investigation[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("investigation", Obj(innerSelection))
    def medicine[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("medicine", Obj(innerSelection))
    def nature[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("nature", Obj(innerSelection))
    def perception[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("perception", Obj(innerSelection))
    def performance[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("performance", Obj(innerSelection))
    def persuasion[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("persuasion", Obj(innerSelection))
    def religion[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("religion", Obj(innerSelection))
    def sleightOfHand[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("sleightOfHand", Obj(innerSelection))
    def stealth[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("stealth", Obj(innerSelection))
    def survival[A](innerSelection: SelectionBuilder[Skill, A]): SelectionBuilder[Skills, A] =
      _root_.caliban.client.SelectionBuilder.Field("survival", Obj(innerSelection))

  }

  type Source
  object Source {

    final case class SourceView(
      name: String,
      id:   String,
      url:  scala.Option[String]
    )

    type ViewSelection = SelectionBuilder[Source, SourceView]

    def view: ViewSelection = (name ~ id ~ url).map { case (name, id, url) => SourceView(name, id, url) }

    def name: SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())
    def id:   SelectionBuilder[Source, String] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def url: SelectionBuilder[Source, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("url", OptionOf(Scalar()))

  }

  type Speed
  object Speed {

    final case class SpeedView(
      speedType: SpeedType,
      value:     Int
    )

    type ViewSelection = SelectionBuilder[Speed, SpeedView]

    def view: ViewSelection = (speedType ~ value).map { case (speedType, value) => SpeedView(speedType, value) }

    def speedType: SelectionBuilder[Speed, SpeedType] =
      _root_.caliban.client.SelectionBuilder.Field("speedType", Scalar())
    def value: SelectionBuilder[Speed, Int] = _root_.caliban.client.SelectionBuilder.Field("value", Scalar())

  }

  type SpellHeader
  object SpellHeader {

    final case class SpellHeaderView(
      id:   Long,
      name: String
    )

    type ViewSelection = SelectionBuilder[SpellHeader, SpellHeaderView]

    def view: ViewSelection = (id ~ name).map { case (id, name) => SpellHeaderView(id, name) }

    def id:   SelectionBuilder[SpellHeader, Long] = _root_.caliban.client.SelectionBuilder.Field("id", Scalar())
    def name: SelectionBuilder[SpellHeader, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type SpellSlots
  object SpellSlots {

    final case class SpellSlotsView(
      level: Int,
      used:  Int,
      total: Int
    )

    type ViewSelection = SelectionBuilder[SpellSlots, SpellSlotsView]

    def view: ViewSelection =
      (level ~ used ~ total).map { case (level, used, total) => SpellSlotsView(level, used, total) }

    def level: SelectionBuilder[SpellSlots, Int] = _root_.caliban.client.SelectionBuilder.Field("level", Scalar())
    def used:  SelectionBuilder[SpellSlots, Int] = _root_.caliban.client.SelectionBuilder.Field("used", Scalar())
    def total: SelectionBuilder[SpellSlots, Int] = _root_.caliban.client.SelectionBuilder.Field("total", Scalar())

  }

  type SubClass
  object SubClass {

    final case class SubClassView(name: String)

    type ViewSelection = SelectionBuilder[SubClass, SubClassView]

    def view: ViewSelection = name.map(name => SubClassView(name))

    def name: SelectionBuilder[SubClass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

  }

  type Traits
  object Traits {

    final case class TraitsView(
      personalityTraits: scala.Option[String],
      ideals:            scala.Option[String],
      bonds:             scala.Option[String],
      flaws:             scala.Option[String],
      appearance:        scala.Option[String]
    )

    type ViewSelection = SelectionBuilder[Traits, TraitsView]

    def view: ViewSelection =
      (personalityTraits ~ ideals ~ bonds ~ flaws ~ appearance).map {
        case (personalityTraits, ideals, bonds, flaws, appearance) =>
          TraitsView(personalityTraits, ideals, bonds, flaws, appearance)
      }

    def personalityTraits: SelectionBuilder[Traits, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("personalityTraits", OptionOf(Scalar()))
    def ideals: SelectionBuilder[Traits, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("ideals", OptionOf(Scalar()))
    def bonds: SelectionBuilder[Traits, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("bonds", OptionOf(Scalar()))
    def flaws: SelectionBuilder[Traits, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("flaws", OptionOf(Scalar()))
    def appearance: SelectionBuilder[Traits, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field("appearance", OptionOf(Scalar()))

  }

  type Wallet
  object Wallet {

    final case class WalletView(
      pp: Long,
      gp: Long,
      ep: Long,
      sp: Long,
      cp: Long
    )

    type ViewSelection = SelectionBuilder[Wallet, WalletView]

    def view: ViewSelection =
      (pp ~ gp ~ ep ~ sp ~ cp).map { case (pp, gp, ep, sp, cp) => WalletView(pp, gp, ep, sp, cp) }

    def pp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("pp", Scalar())
    def gp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("gp", Scalar())
    def ep: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("ep", Scalar())
    def sp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("sp", Scalar())
    def cp: SelectionBuilder[Wallet, Long] = _root_.caliban.client.SelectionBuilder.Field("cp", Scalar())

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
    cr:               ChallengeRating,
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
            "cr"               -> implicitly[ArgEncoder[ChallengeRating]].encode(value.cr),
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
    def playerCharacter[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[PlayerCharacter, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "playerCharacter",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def nonPlayerCharacter[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[NonPlayerCharacter, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "nonPlayerCharacter",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def encounters[A](value: Long)(innerSelection: SelectionBuilder[Encounter, A])(implicit encoder0: ArgEncoder[Long])
      : SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "encounters",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def encounter[A](
      campaignId:  Long,
      encounterId: Long
    )(
      innerSelection: SelectionBuilder[Encounter, A]
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "encounter",
        OptionOf(Obj(innerSelection)),
        arguments = List(
          Argument("campaignId", campaignId, "Long!")(encoder0),
          Argument("encounterId", encounterId, "Long!")(encoder1)
        )
      )
    def bestiary[A](
      name:            scala.Option[String] = None,
      challengeRating: scala.Option[ChallengeRating] = None,
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
      encoder1:  ArgEncoder[scala.Option[ChallengeRating]],
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
          Argument("challengeRating", challengeRating, "ChallengeRating")(encoder1),
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
    def randomTables[A](
      randomTableType: scala.Option[RandomTableType] = None
    )(
      innerSelection:    SelectionBuilder[RandomTable, A]
    )(implicit encoder0: ArgEncoder[scala.Option[RandomTableType]]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "randomTables",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("randomTableType", randomTableType, "RandomTableType")(encoder0))
      )
    def randomTable[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[RandomTable, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "randomTable",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )
    def aiGenerateEncounterDescription(
      header:   EncounterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[EncounterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "aiGenerateEncounterDescription",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "EncounterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def aiGenerateNPCDetails[A](
      header:   NonPlayerCharacterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(
      innerSelection: SelectionBuilder[NonPlayerCharacterInfo, A]
    )(implicit
      encoder0: ArgEncoder[NonPlayerCharacterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "aiGenerateNPCDetails",
        OptionOf(Obj(innerSelection)),
        arguments = List(
          Argument("header", header, "NonPlayerCharacterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def aiGenerateNPCDescription(
      header:   NonPlayerCharacterHeaderInput,
      jsonInfo: zio.json.ast.Json,
      version:  String
    )(implicit
      encoder0: ArgEncoder[NonPlayerCharacterHeaderInput],
      encoder1: ArgEncoder[zio.json.ast.Json],
      encoder2: ArgEncoder[String]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[String]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "aiGenerateNPCDescription",
        OptionOf(Scalar()),
        arguments = List(
          Argument("header", header, "NonPlayerCharacterHeaderInput!")(encoder0),
          Argument("jsonInfo", jsonInfo, "Json!")(encoder1),
          Argument("version", version, "String!")(encoder2)
        )
      )
    def npcsForScene[A](
      value: Long
    )(
      innerSelection:    SelectionBuilder[KVLongListOfLong, A]
    )(implicit encoder0: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[List[A]]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "npcsForScene",
        OptionOf(ListOf(Obj(innerSelection))),
        arguments = List(Argument("value", value, "Long!")(encoder0))
      )

  }

  type Mutations = _root_.caliban.client.Operations.RootMutation
  object Mutations {

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
    def addNpcToScene(
      sceneId: Long,
      npcId:   Long
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "addNpcToScene",
        OptionOf(Scalar()),
        arguments = List(Argument("sceneId", sceneId, "Long!")(encoder0), Argument("npcId", npcId, "Long!")(encoder1))
      )
    def removeNpcFromScene(
      sceneId: Long,
      npcId:   Long
    )(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Long]
    ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
      _root_.caliban.client.SelectionBuilder.Field(
        "removeNpcFromScene",
        OptionOf(Scalar()),
        arguments = List(Argument("sceneId", sceneId, "Long!")(encoder0), Argument("npcId", npcId, "Long!")(encoder1))
      )

  }

}

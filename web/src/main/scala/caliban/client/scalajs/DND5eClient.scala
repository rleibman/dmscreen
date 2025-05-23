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

  type SubClass
  object SubClass {

    final case class SubClassView(name: String)

    type ViewSelection = SelectionBuilder[SubClass, SubClassView]

    def view: ViewSelection = name.map(name => SubClassView(name))

    def name: SelectionBuilder[SubClass, String] = _root_.caliban.client.SelectionBuilder.Field("name", Scalar())

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
      innerSelection: SelectionBuilder[NonPlayerCharacter, A]
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

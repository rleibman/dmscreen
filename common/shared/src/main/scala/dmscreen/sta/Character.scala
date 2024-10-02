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

package dmscreen.sta

import dmscreen.*
import just.semver.SemVer
import zio.json.ast.Json

opaque type CharacterId = Long

object CharacterId {

  val empty: CharacterId = CharacterId(0)

  def apply(CharacterId: Long): CharacterId = CharacterId

  extension (CharacterId: CharacterId) {

    def value: Long = CharacterId

  }

}

import scala.compiletime.ops.int.*

type Bounded[MIN <: Int, MAX <: Int] <: Int = MAX match {
  case MIN => MIN
  case _   => MAX | Bounded[MIN, MAX - 1]
}

type AttributeRating = Bounded[7, 12]

object Attributes {

  val default = Attributes(control = 8, daring = 8, fitness = 8, insight = 8, presence = 8, reason = 8)

}

case class Attributes(
  control:  AttributeRating,
  daring:   AttributeRating,
  fitness:  AttributeRating,
  insight:  AttributeRating,
  presence: AttributeRating,
  reason:   AttributeRating
)

type DepartmentRating = Bounded[0, 5]

object Departments {

  val default = Departments(command = 1, conn = 1, engineering = 1, security = 1, science = 1, medicine = 1)

}

case class Departments(
  command:     DepartmentRating,
  conn:        DepartmentRating,
  engineering: DepartmentRating,
  security:    DepartmentRating,
  science:     DepartmentRating,
  medicine:    DepartmentRating
)
case class Trait(name: String)

object LineageType {

  // Maybe get these from a file instead?
  val andorian = new LineageType("Andorian")
  val andorianAenar = new LineageType("Andorian (Aenar)")
  val bajoran = new LineageType("Bajoran")
  val benzite = new LineageType("Benzite")
  val betazoid = new LineageType("Betazoid")
  val bolian = new LineageType("Bolian")
  val borg = new LineageType("Borg")
  val breen = new LineageType("Breen")
  val caitian = new LineageType("Caitian")
  val cardassian = new LineageType("Cardassian")
  val ferengi = new LineageType("Ferengi")
  val gorn = new LineageType("Gorn")
  val human = new LineageType("Human")
  val android = new LineageType("Android")
  val kelpien = new LineageType("Kelpien")
  val klingon = new LineageType("Klingon")
  val lanthanite = new LineageType("Lanthanite")
  val napean = new LineageType("Napean")
  val orion = new LineageType("Orion")
  val progenitor = new LineageType("Progenitor")
  val q = new LineageType("Q")
  val rigellian = new LineageType("Rigellian")
  val romulan = new LineageType("Romulan")
  val tholian = new LineageType("Tholian")
  val tribble = new LineageType("Tribble")
  val trill = new LineageType("Trill")
  val vorta = new LineageType("Vorta")
  val vulcan = new LineageType("Vulcan")
  val xindi = new LineageType("Xindi")
  val yridian = new LineageType("Yridian")
  val zaldan = new LineageType("Zaldan")
  val denobulan = new LineageType("Denobulan")
  val `el-aurian` = new LineageType("El-Aurian")
  val hirogen = new LineageType("Hirogen")
  val `jem'hadar` = new LineageType("Jem'Hadar")
  val ocampa = new LineageType("Ocampa")
  val pakled = new LineageType("Pakled")
  val reman = new LineageType("Reman")
  val `species-8472` = new LineageType("Species 8472")
  val talaxian = new LineageType("Talaxian")
  val `tellarite` = new LineageType("Tellarite")

  def other(name: String) = new LineageType(name)

  val values: Seq[LineageType] = Seq(
    andorian,
    andorianAenar,
    bajoran,
    benzite,
    betazoid,
    bolian,
    borg,
    breen,
    caitian,
    cardassian,
    ferengi,
    gorn,
    human,
    android,
    kelpien,
    klingon,
    lanthanite,
    napean,
    orion,
    progenitor,
    q,
    rigellian,
    romulan,
    tholian,
    tribble,
    trill,
    vorta,
    vulcan,
    xindi,
    yridian,
    zaldan,
    denobulan,
    `el-aurian`,
    hirogen,
    `jem'hadar`,
    ocampa,
    pakled,
    reman,
    `species-8472`,
    talaxian,
    `tellarite`
  )

}

class LineageType(val name: String)

case class Lineage(lineageType: LineageType)

enum Rank {

  // Core
  case Captain,
    Commander,
    LtCommander,
    Lieutenant,
    LieutenantJG,
    Ensign,
    MasterChiefPettyOfficer,
    MasterChiefSpecialist,
    SeniorChiefPettyOfficer,
    SeniorChiefSpecialist,
    ChiefPettyOfficer,
    ChiefSpecialist,
    PettyOfficer1stClass,
    PettyOfficer2ndClass,
    PettyOfficer3rdClass,
    Specialist1stClass,
    Specialist2ndClass,
    Specialist3rdClass,
    Yeoman1stClass,
    Yeoman2ndClass,
    Yeoman3rdClass,
    Crewman1stClass,
    Crewman2ndClass,
    Crewman3rdClass,

    // Command
    RearAdmiral,
    RearAdmiralLower,
    RearAdmiralUpper,
    ViceAdmiral,
    Admiral,
    FleetAdmiral,
    Commodore,
    FleetCaptain,
    Civilian,

    // KlingonCore
    Sergeant,
    Corporal,
    Bekk,

    // Player's Guide
    Colonel,
    Brigadier,
    General,
    MajorGeneral,
    LieutenantGeneral,
    LieutenantColonel,
    Major,
    FirstLieutenant,
    SecondLieutenant,
    MasterSergeant,
    StaffSergeant,
    Private,
    SubCommander,
    SubLieutenant,
    Centurion,
    Uhlan,
    GrandGul,
    Legate,
    Jagul,
    Gul,
    Dal,
    Glinn,
    Gil,
    Garresh,
    Trooper,
    Administrator,
    FleetCommander,
    CadetFourthClass,
    CadetThirdClass,
    CadetSecondClass,
    CadetFirstClass,

    // not specified
    DaiMon,
    Adhar,
    LorC,
    LorBB,
    LorAA,
    Praetor,
    Proconsul,
    ViceProconsul,
    FirstConsul,
    Drone,
    Queen,
    Senator,
    Chairman,
    ViceChairman,
    Prod,

    // other
    None

  def possibleRanks(organization: Organization): Seq[Rank] =
    organization match {
      case Organization.federation =>
        Seq(
          Captain,
          Commander,
          LtCommander,
          Lieutenant,
          LieutenantJG,
          Ensign,
          MasterChiefPettyOfficer,
          MasterChiefSpecialist,
          SeniorChiefPettyOfficer,
          SeniorChiefSpecialist,
          ChiefPettyOfficer,
          ChiefSpecialist,
          PettyOfficer1stClass,
          PettyOfficer2ndClass,
          PettyOfficer3rdClass,
          Specialist1stClass,
          Specialist2ndClass,
          Specialist3rdClass,
          Yeoman1stClass,
          Yeoman2ndClass,
          Yeoman3rdClass,
          Crewman1stClass,
          Crewman2ndClass,
          Crewman3rdClass,
          RearAdmiral,
          RearAdmiralLower,
          RearAdmiralUpper,
          ViceAdmiral,
          Admiral,
          FleetAdmiral,
          Commodore,
          FleetCaptain,
          Civilian,
          Praetor,
          Proconsul,
          ViceProconsul,
          FirstConsul,
          SubLieutenant,
          Uhlan,
          Drone,
          Queen
        )
      case Organization.klingon =>
        Seq(
          General,
          Brigadier,
          Colonel,
          Captain,
          Commander,
          Lieutenant,
          Sergeant,
          Bekk,
          Civilian
        )
      case Organization.romulan =>
        Seq(
          General,
          Admiral,
          Commander,
          SubCommander,
          Colonel,
          Centurion,
          Major,
          SubLieutenant,
          Uhlan,
          Captain,
          Praetor,
          Proconsul,
          ViceProconsul,
          FirstConsul,
          Senator,
          Chairman,
          ViceChairman,
          Prod,
          Civilian
        )
      case Organization.cardassian =>
        Seq(
          Legate,
          Gul,
          Dal,
          Glinn,
          Gil,
          Garresh,
          Trooper,
          Administrator,
          Civilian
        )
      case Organization.ferengi =>
        Seq(
          DaiMon,
          Adhar,
          LorC,
          LorBB,
          LorAA,
          Civilian
        )
      case Organization.borg =>
        Seq(
          Drone,
          Queen
        )
      case Organization.orion =>
        ???
    }

}

enum Role {

  case
    // Core
    CommandingOfficer,
    ExecutiveOfficer,
    OperationsManager,
    ChiefEngineer,
    ChiefOfSecurity,
    ShipsCounselor,
    ChiefMedicalOfficer,
    ScienceOfficer,
    FlightController,
    CommunicationsOfficer,

    // Command Division
    Admiral,
    Adjutant,
    StrategicOperations,
    IntelligenceOfficer,
    FleetLiaisonOfficer,
    DiplomaticAttache,

    // Sciences Division
    ChiefSurgeon,
    HeadNurse,
    Anesthesiologist,
    PhysiciansAssistant,

    // Klingon Core
    SecondOrThirdOfficer,
    WeaponsOfficer,
    ShipsCookOrChef,

    // Player's Guide
    Administrator,
    Ambassador,
    ArmoryOfficer,
    Bartender,
    Bodyguard,
    Child,
    CivilianBureaucrat,
    Constable,
    Expert,
    IntelligenceAgent,
    Merchant,
    PoliticalLiaison,
    ShipsDoctor,
    SpiritualLeader,
    Translator,
    Navigator,
    Helmsman,

    // Federation-Klingon War
    CombatEngineer,
    FieldMedic,
    HeavyWeaponsSpecialist,
    OrdnanceExpert,
    Reconaissance,
    SquadLeader,
    TacticalOfficer,

    // not specified
    Other

}

case class Focus(name: String)

enum Division {

  case Command, Operations, Sciences

}

case class EthicalValue(name: String)

case class Organization(name: String)

object Organization {

  val none:       Organization = Organization("none")
  val cardassian: Organization = Organization("cardassian")
  val federation: Organization = Organization("federation")
  val ferengi:    Organization = Organization("ferengi")
  val klingon:    Organization = Organization("klingon")
  val orion:      Organization = Organization("orion")
  val romulan:    Organization = Organization("romulan")
  val borg:       Organization = Organization("borg")
  def other(name: String): Organization = Organization(name)

  def knownOrganizations: Seq[Organization] =
    Seq(
      cardassian,
      federation,
      ferengi,
      klingon,
      orion,
      romulan,
      borg
    )

}

case class CareerEvent(name: String)

case class Determination(
  value: Int,
  max:   Int
)

case class Stress(
  value: Int,
  max:   Int
)

case class Talent(
  name:        String,
  description: String
)

final case class InventoryItem(
  name:        String,
  quantity:    Int = 1,
  description: String = ""
)

case class CharacterInfo(
  determination:    Determination = Determination(10, 10),
  stress:           Stress = Stress(10, 10), // maximum stress is typically your fitness attribute
  organization:     Organization = Organization.federation,
  pronouns:         Option[String] = None,
  rank:             Option[Rank] = None,
  jobAssignment:    Option[String] = None,
  roles:            Seq[Role] = Seq.empty,
  reputation:       Int = 10,
  lineage:          Seq[Lineage] = Seq.empty,
  environment:      Option[String] = None,
  upbringing:       Option[String] = None,
  careerPath:       Option[String] = None,
  experience:       Option[String] = None,
  careerEvents:     Seq[CareerEvent] = Seq.empty,
  attributes:       Attributes = Attributes.default,
  departments:      Departments = Departments.default,
  values:           Seq[EthicalValue] = Seq.empty,
  focuses:          Seq[Focus] = Seq.empty, // Main characters have 6 focuses
  pastimes:         Seq[String] = Seq.empty,
  attacks:          Seq[String] = Seq.empty,
  speciesAbilities: Seq[String] = Seq.empty,
  talents:          Seq[Talent] = Seq.empty,
  specialRules:     Seq[String] = Seq.empty,
  inventoryItems:   Seq[InventoryItem] = Seq.empty,
  weapons:          Seq[Weapon] = Seq.empty,
  traits:           Seq[Trait] = Seq.empty,
  reprimands:       Int = 0,
  assignedShip:     Option[String] = None,
  age: Option[Int] = None, // Might want to add more to age, like "adolescent", etc, things that would be different by species
  notes: String = ""
)

final case class CharacterHeader(
  id:         CharacterId,
  campaignId: CampaignId,
  name:       Option[String],
  playerName: Option[String] = None
) extends HasId[CharacterId]

case class Character(
  header:               CharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[CharacterId, CharacterHeader, CharacterInfo] {

  override def entityType: EntityType[CharacterId] = STAEntityType.character

}

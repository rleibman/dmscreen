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

final case class CharacterHeader(
  id:         CharacterId,
  campaignId: CampaignId,
  name:       String, // TODO make it optional
  playerName: Option[String] = None
) extends HasId[CharacterId]

case class Character(
  header:               CharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[CharacterId, CharacterHeader, CharacterInfo] {

  override def entityType: EntityType = STAEntityType.character

}

object Attributes {

  val default = Attributes(7, 7, 7, 7, 7, 7)

}

case class Attributes(
  control:  Int,
  daring:   Int,
  fitness:  Int,
  insight:  Int,
  presence: Int,
  reason:   Int
)

object Skills {

  val default = Skills(1, 1, 1, 1, 1, 1)

}

case class Skills(
  command:     Int,
  conn:        Int,
  engineering: Int,
  security:    Int,
  science:     Int,
  medicine:    Int
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

}

class LineageType(val name: String)

case class Lineage(name: String)

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
    None

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

}

case class Focus(name: String)

enum Division {

  case Command, Operations, Sciences

}

case class EthicalValue(name: String)

enum CharacterType {

  case Starfleet,
    KlingonWarrior,
    AlliedMilitary,
    AmbassadorDiplomat,
    Civilian,
    Cadet,
    Child,
    Tribble,
    Other

}
case class CharacterInfo(
  attributes:    Attributes = Attributes.default,
  skills:        Skills = Skills.default,
  lineage:       Lineage,
  characterType: CharacterType = CharacterType.Starfleet,
  reputation:    Int = 10,
  reprimands:    Int = 0,
  age: Option[Int], // Might want to add more to age, like "adolescent", etc, things that would be different by species
  house:         Option[String],
  values:        Seq[EthicalValue],
  rank:          Option[Rank],
  roles:         Seq[Role],
  jobAssignment: Option[String],
  assignedShip:  Option[String],
  focuses:       Seq[Focus],
  pronouns:      Option[String],
  notes:         String
)

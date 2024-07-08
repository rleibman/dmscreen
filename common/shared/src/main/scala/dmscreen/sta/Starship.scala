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

opaque type StarshipId = Long

object StarshipId {

  val empty: StarshipId = StarshipId(0)

  def apply(StarshipId: Long): StarshipId = StarshipId

  extension (StarshipId: StarshipId) {

    def value: Long = StarshipId

  }

}

final case class StarshipHeader(
  id:         StarshipId,
  campaignId: CampaignId,
  name:       Option[String],
  playerName: Option[String] = None
) extends HasId[StarshipId]

case class Starship(
  header:               StarshipHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[StarshipId, StarshipHeader, StarshipInfo] {

  override def entityType: EntityType[StarshipId] = STAEntityType.starship

}

enum ShipBuildType {

  case Pod, Shuttlecraft, Runabout, Starship

}

case class StarshipTrait(
  name: String
)

enum MissionPod {

  case CommandAndControl,
    Sensors,
    Weapons,

    // Utopia Planitia
    AstrometricsAndNavigation,
    DefensiveShieldEnhancement,
    EmergencyRecovery,
    FieldHospital,
    FleetCarrier,
    FleetCommandSupport,
    MobileDrydock,
    WarpPropulsionPod

}

enum MissionProfile {

  case StrategicAndDiplomatic,
    PathfinderAndReconaissance,
    TechnicalTestBed,
    Tactical,
    ScientificAndSurvey,
    CrisisAndEmergencyResponse,
    MultiroleExplorer,
    HouseGuard,
    ProjectEscalante,
    Battlecruiser,
    ReserveFleet,
    CivilianMerchantMarine,
    ColonySupport,
    EntertainmentPleasureShip,
    EspionageIntelligence,
    Flagship,
    LogisticalQuartermaster,
    Patrol,
    Warship

}

enum ShipClass {

  case Akira,
    Akira_UP,
    Ambassador,
    Ambassador_UP,
    Archer,
    Centaur,
    Centaur_UP,
    Constellation,
    Constellation_UP,
    Constitution,
    Constitution_UP,
    Daedalus,
    Daedalus_UP,
    Defiant,
    Defiant_UP,
    Excelsior,
    Excelsior_UP,
    Galaxy,
    Hermes,
    Intrepid,
    Intrepid_UP,
    Luna,
    Luna_UP,
    Miranda,
    Miranda_UP,
    Nebula,
    Nebula_UP,
    NewOrleans,
    Norway,
    Norway_UP,
    Nova,
    NX,
    NX_UP,
    Oberth,
    Oberth_UP,
    Olympic,
    Olympic_UP,
    Saber,
    Sovereign,
    Sovereign_UP,
    Steamrunner,
    Steamrunner_UP,
    Sydney,

    // Discovery
    Walker,
    Shepard,
    Magee,
    Cardenas,
    Hoover,
    Malachowski,
    Engle,
    Nimitz,
    Crossfield,
    Hiawatha,

    // Discovery: Section 31
    StealthShip,
    Nimrod,
    Shiva,

    // Utopia Planitia
    JClassYClass,
    Delta,
    IntrepidType,
    Antares,
    Soyuz,
    Cheyenne,
    Springfield,
    RavenType,
    Niagara,
    Challenger,
    Freedom,
    Prometheus,
    Vesta,
    Ross,
    Inquiry,
    Reliant,
    Sutherland,
    Gagarin,
    Odyssey,
    Pathfinder,
    D5,
    Raptor,
    VonTalk,
    KToch,
    TuYuQ,
    D7,
    Brel,
    PachNom,
    QoToch,
    IwChaPar,
    D12,
    KlingonCivilianTransport,
    KVort,
    ParTok,
    Toron,
    VorCha,
    NeghVar,

    // Lower Decks
    California,
    Osler,
    Obena,
    Parliament,
    TKalat,
    Ganashia,

    // Discovery
    DiscoBirdOfPrey,
    Qugh,
    Daspu,
    Qoj,
    Batlh,
    Chargh,
    NaQjej,
    Elth,
    BortasBir,
    Sech,
    SarcophagusShip,

    // Captain's Log
    ScoutType,
    Angelou,
    Eisenberg,
    Friendship,
    Janeway,
    Kirk,
}

object ShipStats {

  val default = ShipStats(
    departments = List("Command", "Conn", "Security", "Engineering", "Science", "Medicine"),
    systems = List("Comms", "Computers", "Engines", "Sensors", "Structure", "Weapons"),
    className = "Unknown",
    scale = 4,
    weapons = List.empty
  )

}

case class ShipStats(
  departments: List[String],
  systems:     List[String],
  className:   String,
  scale:       Int,
  weapons:     List[Weapon]
)

case class StarshipInfo(
  registry:       String,
  traits:         List[StarshipTrait],
  serviceYear:    Option[Int] = None,
  model:          Option[String] = None,
  missionPod:     Option[MissionPod] = None,
  missionProfile: Option[MissionProfile] = None,
  shipStats:      ShipStats = ShipStats.default,
  weapons:        List[Weapon] = List.empty,
  spaceFrame:     Option[ShipClass] = None
)

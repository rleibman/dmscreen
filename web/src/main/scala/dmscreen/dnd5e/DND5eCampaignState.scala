package dmscreen.dnd5e

import dmscreen.CampaignState

case class DND5eCampaignState(
  campaign:   Campaign,
  pcs:        Seq[PlayerCharacter] = Seq.empty,
  npcs:       Seq[NonPlayerCharacter] = Seq.empty,
  encounters: Seq[Encounter] = Seq.empty
) extends CampaignState

package dmscreen.dnd5e

import dmscreen.CampaignState

case class DND5eCampaignState(
  campaign:   Campaign,
  pcs:        Seq[CharacterPlayer] = Seq.empty,
  npcs:       Seq[NonCharacterPlayer] = Seq.empty,
  encounters: Seq[Encounter] = Seq.empty
) extends CampaignState

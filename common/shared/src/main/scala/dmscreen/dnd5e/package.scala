package dmscreen.dnd5e

import zio.json.*

given JsonCodec[EncounterId] = JsonCodec.long.transform(EncounterId.apply, _.value)
given JsonCodec[NonPlayerCharacterId] = JsonCodec.long.transform(NonPlayerCharacterId.apply, _.value)
given JsonCodec[Scene] = JsonCodec.derived[Scene] // Move this to a more global scope
given JsonCodec[CampaignInfo] = JsonCodec.derived[CampaignInfo] // Move this to a more global scope

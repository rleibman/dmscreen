
- General
DONE  - Work on GraphQL API for Campaign
DONE  - Work on first app page, retrieve a campaign
DONE  - Dice: https://www.npmjs.com/package/@3d-dice/dice-box
DONE  - Modal component CSS
DONE  - Move the campaign stuff out of the dnd5e module and into a general module
- StorageSpec 
  - Campaign
  - Player Character
  - Monster
  - NPC
  - Scene
  - Encounter
DONE- Dashboard Page
DONE- EncounterPlanner Page
DONE - Encounter Page
DONE - Home Page
DONE - NPC Page
DONE - Player Page
DONE - Feats Editor
DONE - Hitpoints Editor
DONE - Notes Editor
DONE - Player Character Class Editor
DONE - Skills Editor
DONE - New Player Character
DONE - Delete Player Character
DONE - Sync Player Character
DONE - DNDBeyond Import
DONE - Long Rest (reset all stats)
DONE- Scene Page
DONE - Campaign Management:
DONE  - Manage multiple campaigns, each with its own set of characters, NPCs, and notes.
DONE  - Automatically log session events, dice rolls, and decisions for future reference.
DONE - Dice roller


BUGS
# General
There's no error logging!
## CSS
WONTDO - Make menu thinner:
WONTDO menu column
WONTDO ```
WONTDO min-width: 200px;
WONTDO    width: 10%;
WONTDO  ```
WONTDO content column
WONTDO ```
WONTDO width: 90%;
WONTDO ```
TEMPDONE - Modal headers are black on black, change color! 
## Functionality
DONE - Why is it changing pages on refresh and ignoring the # in the URL?
# Dashboard
## Functionality
- Might want to see if we can fix the campaign notes now without a button.
# Home
## Functionality
DONE - Hide "current button" if the campaign is already current
Super ugly if there's no campaigns.
# PlayerCharacterPage/Component
DONE Allow other subclasses
DONE - Hit point up/down isn't working
- When increasing HP past the max, increase the max?
- Import from DND Beyond text
# NPC
DONE - Delete NPC is not working
DONE - Add NPC from Bestiary
DONE - Scenes filter
## CSS
- Align pc to top
DONE - Conditions bgcolor should be white
DONE - Inspiration bgcolor should be white
## Functionality
DONE - Character name is not sticking
DONE - Player name is not sticking
DONE - Inspiration is not sticking
DONE - Languages dialog broken
DONE - Feats Dialog broken
# Encounter Editor
DONE - Add NPC doesn't work (note, turns out it was working, it was just not obvious.)
WONTDO - Need to be able to edit encounter name (Not necessary, just go to edit title on left) 
# Combat Runner
DONE - Initiative is totally broken
DONE - Filter out dead NPCs/Monsters
DONE - Healing of PCs needs to get add full healing, not just the difference
DONE Don't pass initiative to dead creatures
DONE HP color is messed up
DONE Same characters same initiative
DONE  Hunter's mark is not working for NPCs
## CSS
DONE - Heal/Damage buttons look ugly, maybe switch the whole table to white background
# StatBlock
## CSS
DONE Notes should be black on white
DONE Edit monster, alignment is broken
DONE Initiatives for pcs not sorted
DONE Edit player name in pc is not working
Next in encounter is broken
DONE No edit for monsters, need to fix that

Features:
DONE - Add snapshot to campaign
DONE - Add NPCs to scenes outside of encounters
DONE - Start combat is not showing the PCs.
DONE - Next is still not working
DONE - Delete campaign
DONE Auth stuff
DONE - Edit user profile
DONE - Change password
DONE - Whoami
DONE - Logout
DONE - Test Login
DONE - Test Register
DONE - Test Forgot password
DONE - Test Reset password
DONE - Bearer token
DONE - Error in Encounter.scala, line 200 (I put a try in there to catch it temporarily)
DONE - CR on encounters is showing value instead of label
DONE - Monster pager is not working
DONE - NPC is not showing up after saving
CNR - Monster search is not working after entering a monster name, selecting it  and then removing the name (I'm guessing something is "sticiking" in the search). Could not reproduce
DONE Campaign name editor
DONE - Bestiary Page
DONE  - Allow name editing in combat editor
- Reference (basically, access all of the SRD)
- About page
- Killing the current combatant sets the "currentCombatant" back to the top of the round
- Combat: next round after rolling initiative
- Name Generator
- Add text generator to encounter editor
- Monsters need to be added to embedding database dynamically.
- Toast is showing up behind the menu
- Error handling, errors are not being displayed, logged, or anything!
- Encounter balancing
- Fifth Edition Character Sheet Import
- Short Rest
- Timeline feature to track campaign events chronologically.
- Session Notes and Log:
- Digital notepad for session planning and real-time note-taking.
- Maps (others do it much better)
- Audio/video/image library
- Messaging (others do it much better)
- Integration with discord
- NPC Automatic Generator
- Mobile mode
- DND Beyond plugin
- Integrate with AI graphic generator
- Connect everything to it's source (rules, monsters, skills, classes, races, subclasses, spells, etc)
- 
DONE - Make LangchainConfiguration part of the general configuration, add it to the debian template.
DONE - Use existing qdrant server, figure out how to query it to see what's in there and not repeat the monsters

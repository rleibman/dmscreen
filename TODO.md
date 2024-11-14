
- General
  - Work on GraphQL API for Campaign
DONE  - Work on first app page, retrieve a campaign
DONE  - Dice: https://www.npmjs.com/package/@3d-dice/dice-box
  - Modal component CSS
  - Error handling, errors are not being displayed, logged, or anything!
  - Move the campaign stuff out of the dnd5e module and into a general module
- StorageSpec 
  - Campaign
  - Player Character
  - Monster
  - NPC
  - Scene
  - Encounter
- About page
- Bestiary Page
DONE- Dashboard Page
DONE- EncounterPlanner Page
- Encounter balancing
DONE - Encounter Page
DONE - Home Page
DONE - NPC Page
DONE - Player Page
 -   conditionImmunities:   Seq[Condition] = Seq.empty,
     damageVulnerabilities: Seq[DamageType] = Seq.empty,
     damageResistances:     Seq[DamageType] = Seq.empty,
     damageImmunities:      Seq[DamageType] = Seq.empty,

DONE - Feats Editor
DONE - Hitpoints Editor
DONE - Notes Editor
DONE - Player Character Class Editor
DONE - Skills Editor
DONE - New Player Character
DONE - Delete Player Character
DONE - Sync Player Character
DONE - DNDBeyond Import
- Fifth Edition Character Sheet Import
- Short Rest
DONE - Long Rest (reset all stats)
- Scene Page

Future (some ideas from Chatgpt)
- Campaign Management:
  - Manage multiple campaigns, each with its own set of characters, NPCs, and notes.
  - Timeline feature to track campaign events chronologically.
- Session Notes and Log:
  - Digital notepad for session planning and real-time note-taking.
DONE  - Automatically log session events, dice rolls, and decisions for future reference.
- Maps (others do it much better)
- Audio/video/image library
- Messaging (others do it much better)
- Integration with discord
DONE - Dice roller
- NPC Automatic Generator
- Mobile mode
- DND Beyond plugin
- Integrate with AI graphic generator
- Connect everything to it's source (rules, monsters, skills, classes, races, subclasses, spells, etc)


BUGS
# General
There's no error logging!
## CSS
- Make menu thinner:
menu column
```
   min-width: 200px;
   width: 10%;
 ```
content column
```
width: 90%;
```
TEMPDONE - Modal headers are black on black, change color! 
## Functionality
- Why is it changing pages on refresh and ignoring the # in the URL?
# Dashboard
## Functionality
- Might want to see if we can fix the campaign notes now without a button.
# Home
## Functionality
DONE - Hide "current button" if the campaign is already current
Super ugly if there's no campaigns.
# PlayerCharacterPage/Component
DONE: Allow other subclasses
DONE - Hit point up/down isn't working
- When increasing HP past the max, increase the max?
- Import from DND Beyond text
# NPC
DONE - Delete NPC is not working
DONE - Add NPC from Bestiary
DONE - Scenes filter
## CSS
- Align pc to top
TEMPDONE - Conditions bgcolor should be white
TEMPDONE - Inspiration bgcolor should be white
## Functionality
DONE - Character name is not sticking
DONE - Player name is not sticking
DONE - Inspiration is not sticking
DONE - Languages dialog broken
DONE - Feats Dialog broken
# Encounter Editor
DONE - Add NPC doesn't work (note, turns out it was working, it was just not obvious.)
WON'T DO - Need to be able to edit encounter name (Not necessary, just go to edit title on left) 
# Combat Runner
DONE - Initiative is totally broken
DONE - Filter out dead NPCs/Monsters
DONE - Healing of PCs needs to get add full healing, not just the difference
DONE: Don't pass initiative to dead creatures
DONE: HP color is messed up
DONE: Same characters same initiative
DONE:  Hunter's mark is not working for NPCs
## CSS
- Heal/Damage buttons look ugly, maybe switch the whole table to white background
# StatBlock
## CSS
TEMPDONE: Notes should be black on white


------------------
Edit monster, alignment is broken
DONE Initiatives for pcs not sorted
DONE Edit player name in pc is not working
Next in encounter is broken


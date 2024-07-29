
- General
  - Work on GraphQL API for Campaign
  - Work on first app page, retrieve a campaign
  - Dice: https://www.npmjs.com/package/@3d-dice/dice-box
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
- Dashboard Page
- EncounterPlanner Page
  - Encounter balancing
- Encounter Page
- Home Page
- NPC Page
- Player Page
  - Feats Editor
  - Hitpoints Editor
  - Notes Editor
  - Player Character Class Editor
  - Skills Editor
  - New Player Character
  - Delete Player Character
  - Sync Player Character
  - DNDBeyond Import
  - Fifth Edition Character Sheet Import
  - Short Rest
  - Long Rest (reset all stats)
- Scene Page

Future (some ideas from Chatgpt)
- Campaign Management:
  - Manage multiple campaigns, each with its own set of characters, NPCs, and notes.
  - Timeline feature to track campaign events chronologically.
- Session Notes and Log:
  - Digital notepad for session planning and real-time note-taking.
  - Automatically log session events, dice rolls, and decisions for future reference.
- Maps (others do it much better)
- Audio/video/image library
- Messaging (others do it much better)
- Integration with discord
- Dice roller
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
- Modal headers are black on black, change color! 
## Functionality
- Why is it changing pages on refresh and ignoring the # in the URL?
# Dashboard
## Functionality
- Might want to see if we can fix the campaign notes now without a button.
# Home
## Functionality
DONE - Hide "current button" if the campaign is already current
# PlayerCharacterPage/Component
## CSS
- Align pc to top
- Conditions bgcolor should be white
- Inspiration bgcolor should be white
## Functionality
DONE - Character name is not sticking
DONE - Player name is not sticking
DONE - Inspiration is not sticking
- Languages dialog broken
- Feats Dialog broken
# Combat Runner
## CSS
Heal/Damage buttons look ugly, maybe switch the whole table to white background
# StatBlock
## CSS
Notes should be black on white, furthermore, notes should be html

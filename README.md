# dmscreen

| Project Stage | CI | Release | Snapshot | Discord |
| --- | --- | --- | --- | --- |
| [![Project stage][Badge-Stage]][Link-Stage-Page] | [![Build Status][Badge-Circle]][Link-Circle] | [![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] | [![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots] | [![Badge-Discord]][Link-Discord] |

# Summary
This project, dmscreen, is a tool designed to assist Dungeon Masters (DMs) in managing and running Dungeons & Dragons 5th Edition (D&D 5e) encounters. It provides a user-friendly interface for organizing and editing encounters, managing monsters, and tracking various aspects of gameplay.  
Key Features:
- Encounter Management: Create, edit, and run encounters with ease.
- PC Dashboard: Track player character (PC) stats and initiative order.
- Monster Database: Search and filter a database of D&D 5e monsters.
- Random Table Generator: Create custom random tables for use in your campaign.
- Dice Roller: Roll dice and calculate results with a built-in dice roller.
- AI Generated Encounter descriptions

# Technologies used
- Scala
- ZIO
  - zio-http
  - zio-json
  - zio-logging
  - zio-prelude
  - zio-quill
  - zio-config
  - zio-cache
- Caliban (GraphQL)
- React (through scalajs-react)
- Scala.js
- Semantic UI
- ScalablyTyped

# Documentation
[dmscreen Microsite](https://zio.github.io/dmscreen/)

# Contributing
[Documentation for contributors](https://zio.github.io/dmscreen/docs/about/about_contributing)

## Code of Conduct

See the [Code of Conduct](https://zio.github.io/dmscreen/docs/about/about_coc)

## Support

Come chat with us on [![Badge-Discord]][Link-Discord].


# License
[License](LICENSE)

[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/dmscreen_2.12.svg "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/dev.zio/dmscreen_2.12.svg "Sonatype Snapshots"
[Badge-Discord]: https://img.shields.io/discord/629491597070827530?logo=discord "chat on discord"
[Badge-Circle]: https://circleci.com/gh/zio/dmscreen.svg?style=svg "circleci"
[Link-Circle]: https://circleci.com/gh/zio/dmscreen "circleci"
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/dev/zio/dmscreen_2.12/ "Sonatype Releases"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/dev/zio/dmscreen_2.12/ "Sonatype Snapshots"
[Link-Discord]: https://discord.gg/2ccFBr4 "Discord"
[Badge-Stage]: https://img.shields.io/badge/Project%20Stage-Concept-red.svg
[Link-Stage-Page]: https://github.com/zio/zio/wiki/Project-Stages

# stat block css
We got it from https://codepen.io/retractedhack/pen/gPLpWe

# Dice stuff
Attempting to use this: https://fantasticdice.games/docs/0.6/intro.

Example here: https://codesandbox.io/s/react-roller-advanced-notation-v1-0-5-rz0nmr?file=/src/App.js

# graphql schema generation
http://localhost:8079/api/dnd5e/schema

# Client code generation
```sbtshell
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/dmscreen.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/DMScreenClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/dnd5e.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/DND5eClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/sta.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/STAClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
```
//Remember to ALWAYS put keys on lists in react!

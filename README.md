# dmscreen

| Project Stage | CI | Release | Snapshot | Discord |
| --- | --- | --- | --- | --- |
| [![Project stage][Badge-Stage]][Link-Stage-Page] | [![Build Status][Badge-Circle]][Link-Circle] | [![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] | [![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots] | [![Badge-Discord]][Link-Discord] |

# Summary
TODO: Tagline

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

# graphql schema generation
http://localhost:8079/api/dnd5e/schema

# Client code generation
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/schema.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/DND5eClient.scala --genview true --scalarMappings Json:zio.json.ast.Json --packageName caliban.client.scalajs

//Remember to ALWAYS put keys on lists in react!

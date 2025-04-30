# dmscreen

![](logo.png "Logo")

# Summary
This project, dmscreen, is a tool designed to assist Dungeon Masters (DMs) in managing and running Dungeons & Dragons 5th Edition (D&D 5e) encounters. It provides a user-friendly interface for organizing and editing encounters, managing monsters, and tracking various aspects of gameplay.  
Key Features:
- Encounter Management: Create, edit, and run encounters with ease.
- PC Dashboard: Track player character (PC) stats and initiative order.
- Monster Database: Search and filter a database of D&D 5e monsters.
- Random Table Generator: Create custom random tables for use in your campaign.
- Dice Roller: Roll dice and calculate results with a built-in dice roller.
- AI Generated Encounter descriptions

# Running Locally 
## Prerequisites
- Java JDK 11+
- Node.js 16+
- Scala
- Rust

## Installation
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/dmscreen.git
   cd dmscreen
   sbt 
   debugDist
   project server
   ~reStart

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

## Support

# License
[License](LICENSE)

# stat block css
We got it from https://codepen.io/retractedhack/pen/gPLpWe

# Dice stuff
Attempting to use this: https://fantasticdice.games/docs/0.6/intro.

Example here: https://codesandbox.io/s/react-roller-advanced-notation-v1-0-5-rz0nmr?file=/src/App.js

# graphql schema generation
http://localhost:8078/api/dnd5e/schema

# Client code generation
```sbtshell
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/dmscreen.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/DMScreenClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/dnd5e.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/DND5eClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
calibanGenClient /home/rleibman/projects/dmscreen/server/src/main/graphql/sta.gql /home/rleibman/projects/dmscreen/web/src/main/scala/caliban/client/scalajs/STAClient.scala --genView true --scalarMappings Json:zio.json.ast.Json,LocalDateTime:java.time.LocalDateTime --packageName caliban.client.scalajs
```
//Remember to ALWAYS put keys on lists in react!
********

# Building and installing a ubuntu package
sbt debian:packageBin
# uninstall the old package
sudo apt-get purge dmscreen-server
# install the new package
sudo dpkg -i ./server/target/dmscreen_1.0.4-SNAPSHOT_all.deb
# start the new service 
sudo service dmscreen-server start
# Monitor it:
sudo journalctl -u dmscreen-server -f --since "10 minutes ago"

# To run ollama locally
https://ollama.com/download


# To run qdrant locally
docker pull qdrant/qdrant
docker run -d -p 6333:6333 --volume /opt/databases/qdrant/storage:/qdrant/storage qdrant/qdrant
You can set up qdrant to run with systemd, it's a bit of a pain, but not that hard.
- Create the qdrant user and group
- Create the qdrant directory, assign ownership to the qdrant user and group
- Create the qdrant.service file in /etc/systemd/system
- Enable and start the qdrant service
- Use the journalctl command to monitor the qdrant service and make sure it ran
- Test it with your browser: http://localhost:6333



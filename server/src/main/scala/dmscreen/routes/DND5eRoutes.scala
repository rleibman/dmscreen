package dmscreen.routes

import caliban.*
import caliban.schema.GenericSchema
import dmscreen.DMScreenEnvironment
import dmscreen.dnd5e.{DND5eAPI, DND5eGameService}
import zio.IO
import zio.http.*

object DND5eRoutes {

  lazy val interpreter: IO[Throwable, GraphQLInterpreter[DMScreenEnvironment, CalibanError]] = DND5eAPI.api.interpreter

  lazy val route =
    for {
      interpreter <- interpreter
    } yield {
      val graphiql = GraphiQLHandler.handler(apiPath = "/api/dnd5e", graphiqlPath = "/graphiql")

      Routes(
        Method.GET / "api" / "dnd5e" / "graphiql" -> graphiql,
        Method.GET / "api" / "dnd5e"              -> QuickAdapter(interpreter).handlers.api,
        Method.POST / "api" / "dnd5e"             -> QuickAdapter(interpreter).handlers.upload
      )
    }

}

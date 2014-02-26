import akka.actor.{ActorSystem, ActorRefFactory}
import api.Api
import core.{Core, BootedCore, CoreActors}
import web.{StaticResources, Web}

object Rest extends App with BootedCore with Core with CoreActors with Api  with StaticResources  {


}


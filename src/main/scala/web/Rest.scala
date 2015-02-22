package web

import api.{Api, Core, BootedCore, CoreActors}


object Rest extends App with BootedCore with Core with CoreActors with Api with StaticResources
package core

import akka.actor.Actor
import core.FruitActor.FruitPojo

object FruitActor {

  case class FruitPojo(name:String, price:Double) {}

}

class FruitActor extends Actor {


  def receive: Receive = {
    case _  =>
      sender ! new FruitPojo("banana", 5.0)
  }
}





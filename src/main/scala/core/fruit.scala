package core

import akka.actor.{Props, Actor}
import java.util.UUID
import core.FruitActor.FruitPojo

object FruitActor {

  case class FruitPojo(name:String, price:Double) {}

}

class FruitActor extends Actor {
  import MessengerActor._
  import EmailActor._
  import SMSActor._


  def receive: Receive = {
    case _  =>
      sender ! new FruitPojo("banana", 5.0)
  }
}





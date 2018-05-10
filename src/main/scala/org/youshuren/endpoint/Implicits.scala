package org.youshuren.endpoint

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Implicits {

  implicit val system = ActorSystem("youshuren-server")
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher
}

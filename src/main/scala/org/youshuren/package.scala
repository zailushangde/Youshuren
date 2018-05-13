package org

import akka.actor.ActorSystem
import akka.event.Logging
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import cats.effect.IO

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

package object youshuren {

  type Result[T] = IO[Either[Throwable, T]]

  implicit val system: ActorSystem = ActorSystem("youshuren-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  implicit val circuitBreaker: CircuitBreaker = new CircuitBreaker(
    system.scheduler,
    maxFailures = 3,
    callTimeout = 15 seconds,
    resetTimeout = 1 seconds
  )
  def logger[T](clazz: Class[T]) = Logging(system, clazz)
}

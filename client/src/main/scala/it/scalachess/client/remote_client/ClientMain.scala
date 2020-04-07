package it.scalachess.client.remote_client

import java.net.InetAddress

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.io.Source

object ClientMain extends App {

  val privateAddress = InetAddress.getLocalHost.getHostAddress
  val customConf = ConfigFactory
    .parseString(s"""akka.remote.artery.canonicalHostname =  $privateAddress""")
    .withFallback(ConfigFactory.load())
  val serverAddress = if (args.length >= 1) args(0) else "127.0.0.1:25555"
  val client        = ActorSystem(Client(serverAddress), "Client", customConf)
  val inputParser   = client.systemActorOf[String](InputParser(client), "InputReader")

  //Stdin redirect to an actor, needs to be outside of the actor system because it is a blocking call
  Source.stdin
    .getLines()
    .takeWhile(_ != InputParser.quit)
    .foreach { inputParser ! _ }

  client.terminate()
}

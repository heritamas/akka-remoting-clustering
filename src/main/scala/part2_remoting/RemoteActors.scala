package part2_remoting

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, ActorSystem, Identify, Props, Stash}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object RemoteActors extends App {

  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("part2_remoting/remoteActors.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")
  //localSimpleActor ! "hello, local actor!"

  // send a message to the REMOTE simple actor

  // Method 1: actor selection
  //val remoteActorSelection = localSystem.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
  //remoteActorSelection ! "hello from the \"local\" JVM"

  // Method 2: resolve the actor selection to an actor ref
//  import localSystem.dispatcher
//  implicit val timeout = Timeout(3 seconds)
//  val remoteActorRefFuture = remoteActorSelection.resolveOne()
//  remoteActorRefFuture.onComplete {
//    case Success(actorRef) => actorRef ! "I've resolved you in a future!"
//    case Failure(ex) => println(s"I failed to resolve the remote actor because: $ex")
//  }

  // Method 3: actor identification via messages
  /*
    - actor resolver will ask for an actor selection from the local actor system
    - actor resolver will send a Identify(42) to the actor selection
    - the remote actor will AUTOMATICALLY respond with ActorIdentity(42, actorRef)
    - the actor resolver is free to use the remote actorRef
   */
  class ActorResolver extends Actor with ActorLogging with Stash {
    override def preStart(): Unit = {
      val selection = context.actorSelection("akka://RemoteSystem@localhost:2552/user/remoteSimpleActor")
      selection ! Identify(42)
    }

    override def receive: Receive = {
      case ActorIdentity(42, Some(actorRef)) =>
        actorRef ! "Thank you for identifying yourself!"
        unstashAll()
        context.become(forwarder(actorRef))
      case _ =>
        // stash all other messages until we receive the ActorIdentity message
        log.warning("Received an unexpected message before identifying the remote actor. Stashing it.")
        stash()
    }

    private def forwarder(actorRef: ActorRef) : Receive = {
      case message =>
        log.info(s"Forwarding message '$message' to $actorRef")
        actorRef forward message
    }
  }

  val stub = localSystem.actorOf(Props[ActorResolver], "localActorResolver")
  stub ! "I reach you via the local actor resolver!"
  stub ! "Another message for the remote actor!"
}

object RemoteActors_Remote extends App {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("part2_remoting/remoteActors.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  remoteSimpleActor ! "hello, remote actor!"
}

// two actor systems, one local and one remote, both running in the same JVM
object RemoteActors_Both extends App {
  val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("part2_remoting/remoteActors.conf"))
  val localSimpleActor = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")
  localSimpleActor ! "hello, local actor!"

  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("part2_remoting/remoteActors.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  remoteSimpleActor ! "hello, remote actor!"
}
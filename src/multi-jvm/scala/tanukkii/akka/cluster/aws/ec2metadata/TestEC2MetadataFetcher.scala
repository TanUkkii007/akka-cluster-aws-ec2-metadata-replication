package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.{ActorLogging, Props, ActorRef, Actor}

class TestEC2MetadataFetcher(sendTo: ActorRef, info: EC2InstanceInfo) extends Actor with ActorLogging {
  import EC2MetadataFetcher._

  def receive: Receive = {
    case FetchEC2Metadata =>
      log.info("mock EC2 metadata {} will be sent", info)
      sendTo ! EC2MetadataFetched(info)
    case GetEC2Metadata(originalSender) =>
      sendTo ! MyEC2Metadata(Some(info), originalSender)
  }
}

object TestEC2MetadataFetcher {
  def props(sendTo: ActorRef, info: EC2InstanceInfo): Props = Props(new TestEC2MetadataFetcher(sendTo, info))
}
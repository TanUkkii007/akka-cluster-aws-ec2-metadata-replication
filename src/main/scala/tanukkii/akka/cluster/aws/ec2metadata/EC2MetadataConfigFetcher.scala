package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.{Props, ActorRef, Actor}

class EC2MetadataConfigFetcher(sendTo: ActorRef) extends Actor {
  import EC2MetadataFetcher._

  var cache: Option[EC2InstanceInfo] = None

  def receive: Receive = {
    case FetchEC2Metadata =>
      val config = context.system.settings.config.getConfig("akka-cluster-aws-ec2-metadata-replication.instance-info")
      val info = EC2InstanceInfo.fromConfig(config)
      cache = Some(info)
      sendTo ! EC2MetadataFetched(info)
    case GetEC2Metadata(originalSender) =>
      sendTo ! MyEC2Metadata(cache, originalSender)
  }
}

object EC2MetadataConfigFetcher {
  def props(sendTo: ActorRef): Props = Props(new EC2MetadataConfigFetcher(sendTo))
}
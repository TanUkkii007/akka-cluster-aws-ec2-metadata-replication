package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.{Props, Actor}

private[ec2metadata] class EC2MetadataReplicator(settings: EC2MetadataReplicationSettings) extends Actor {
  import EC2MetadataReplicator._

  def fetcherProps = EC2MetadataFetcher.props(self, settings.ec2MetadataFetchRetryInterval)

  val ec2MetadataFetcher = context.actorOf(fetcherProps, "ec2-metadata-fetcher")

  val ec2MetadataReplicator = context.actorOf(UnderlyingEC2MetadataReplicator.props(self, settings.replicationTimeout), "ec2-metadata-replicator")

  ec2MetadataFetcher ! EC2MetadataFetcher.FetchEC2Metadata

  def receive: Receive = {
    case EC2MetadataFetcher.EC2MetadataFetched(info) =>
      ec2MetadataReplicator ! UnderlyingEC2MetadataReplicator.ReplicateEC2Metadata(info)
    case GetClusterEC2Metadata =>
      ec2MetadataReplicator ! UnderlyingEC2MetadataReplicator.GetEC2Metadata(sender())
    case UnderlyingEC2MetadataReplicator.CurrentEC2Metadata(elements, originalSender) =>
      originalSender ! CurrentClusterEC2Metadata(elements)
    case GetMyEC2Metadata =>
      ec2MetadataFetcher ! EC2MetadataFetcher.GetEC2Metadata(sender())
    case EC2MetadataFetcher.MyEC2Metadata(info, originalSender) =>
      originalSender ! MyEC2Metadata(info)
  }
}

object EC2MetadataReplicator {
  case object GetClusterEC2Metadata
  case class CurrentClusterEC2Metadata(elements: Set[EC2ClusterMember])
  case object GetMyEC2Metadata
  case class MyEC2Metadata(info: Option[EC2InstanceInfo])

  private[ec2metadata] def props(settings: EC2MetadataReplicationSettings): Props = Props(new EC2MetadataReplicator(settings))
}
package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{ORSet, ORSetKey, DistributedData}

import scala.concurrent.duration.FiniteDuration

case class EC2ClusterMember(address: Address, info: EC2InstanceInfo)

private class UnderlyingEC2MetadataReplicator(sendTo: ActorRef, replicationTimeout: FiniteDuration) extends Actor
  with ActorLogging {
  import UnderlyingEC2MetadataReplicator._

  val EC2MetadataSetKey = ORSetKey[EC2ClusterMember]("ec2-cluster-member")

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator

  def receive: Receive = waiting

  def waiting: Receive = ({
    case ReplicateEC2Metadata(info) =>
      val data = EC2ClusterMember(cluster.selfAddress, info)
      replicate(data)
      context.become(replicating(data))
  }: Receive) orElse getData

  def replicating(data: EC2ClusterMember): Receive = ({
    case UpdateSuccess(EC2MetadataSetKey, _) =>
      log.debug("Successfully replicate EC2 instance metadata: {}.", data.info)
    case UpdateTimeout(EC2MetadataSetKey, _) =>
      log.warning("Failed to replicate EC2 instance metadata. Retrying.")
      replicate(data)
      context.become(waiting)
  }: Receive) orElse getData

  def getData: Receive = {
    case msg: GetEC2Metadata =>
      replicator ! Get(EC2MetadataSetKey, ReadLocal, request = Some(msg.originalSender))
    case g @ GetSuccess(EC2MetadataSetKey, Some(originalSender: ActorRef)) =>
      val elements = g.get(EC2MetadataSetKey).elements
      sendTo ! CurrentEC2Metadata(elements, originalSender)
  }

  def replicate(data: EC2ClusterMember) = {
    val write = WriteMajority(replicationTimeout)
    replicator ! Update(EC2MetadataSetKey, ORSet.empty[EC2ClusterMember], write)(_ + data)
  }
}

private[ec2metadata] object UnderlyingEC2MetadataReplicator {
  case class ReplicateEC2Metadata(info: EC2InstanceInfo)
  case class GetEC2Metadata(originalSender: ActorRef)
  case class CurrentEC2Metadata(elements: Set[EC2ClusterMember], originalSender: ActorRef)

  private[ec2metadata] def props(sendTo: ActorRef, replicationTimeout: FiniteDuration): Props = Props(new UnderlyingEC2MetadataReplicator(sendTo, replicationTimeout))
}
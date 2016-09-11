package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData

object EC2MetadataReplication extends ExtensionId[EC2MetadataReplication] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): EC2MetadataReplication = new EC2MetadataReplication(system)

  override def lookup = EC2MetadataReplication

  override def get(system: ActorSystem): EC2MetadataReplication = super.get(system)
}

class EC2MetadataReplication(system: ExtendedActorSystem) extends Extension {

  private val config = system.settings.config.getConfig("akka-cluster-aws-ec2-metadata-replication")
  private val settings = EC2MetadataReplicationSettings(system)

  def isTerminated: Boolean = DistributedData(system).isTerminated

  val replicator: ActorRef =
    if (isTerminated) {
      system.log.warning("EC2MetadataReplicator points to dead letters: Make sure the cluster node is not terminated and has the proper role!")
      system.deadLetters
    } else {
      val name = config.getString("name")
      system.systemActorOf(EC2MetadataReplicator.props(settings), name)
    }

}

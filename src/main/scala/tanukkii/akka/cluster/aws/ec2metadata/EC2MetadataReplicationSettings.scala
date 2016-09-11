package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.ActorSystem
import scala.concurrent.duration.{FiniteDuration, _}

case class EC2MetadataReplicationSettings(replicationTimeout: FiniteDuration, ec2MetadataFetchRetryInterval: FiniteDuration)

object EC2MetadataReplicationSettings {
  def apply(system: ActorSystem): EC2MetadataReplicationSettings = {
    val c = system.settings.config
    EC2MetadataReplicationSettings(
      c.getDuration("akka-cluster-aws-ec2-metadata-replication.replication-timeout").toMillis millis,
      c.getDuration("akka-cluster-aws-ec2-metadata-replication.ec2-metadata-fetch-retry-interval").toMillis millis
    )
  }
}
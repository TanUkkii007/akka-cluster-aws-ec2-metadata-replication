package tanukkii.akka.cluster.aws.ec2metadata

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object MultiNodeEC2MetadataReplicationTestConfig extends MultiNodeConfig {

  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")
  val node4 = role("node4")
  val node5 = role("node5")

  commonConfig(ConfigFactory.parseString(
    """
      |akka.cluster.metrics.enabled=off
      |akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
      |akka.extensions += "akka.cluster.ddata.DistributedData"
      |akka.loglevel = INFO
    """.stripMargin))
}

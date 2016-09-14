package tanukkii.akka.cluster.aws.ec2metadata

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object MultiNodeEC2MetadataReplicationTestConfig2 extends MultiNodeConfig {

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
      |akka.extensions += "tanukkii.akka.cluster.aws.ec2metadata.EC2MetadataReplication"
      |akka.loglevel = INFO
      |akka-cluster-aws-ec2-metadata-replication {
      |  provide-instance-info-from-config = true
      |
      |  instance-info {
      |    instance-id = "i-12345"
      |    instance-type = "t2.micro"
      |    image-id = "ami-5fb8c835"
      |    architecture = "x86_64"
      |    kernel-id = "aki-919dcaf8"
      |    region = "us-east-1"
      |    availability-zone = "us-east-1d"
      |    private-ip = "10.158.112.5"
      |  }
      |}
    """.stripMargin))
}

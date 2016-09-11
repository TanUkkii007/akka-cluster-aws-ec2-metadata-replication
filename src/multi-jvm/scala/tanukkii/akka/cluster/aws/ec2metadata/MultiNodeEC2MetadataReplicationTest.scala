package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.{ActorRef, Props}
import akka.cluster.Cluster
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import scala.concurrent.duration._

class MultiNodeEC2MetadataReplicationTestMultiJvmNode1 extends MultiNodeEC2MetadataReplicationTest
class MultiNodeEC2MetadataReplicationTestMultiJvmNode2 extends MultiNodeEC2MetadataReplicationTest
class MultiNodeEC2MetadataReplicationTestMultiJvmNode3 extends MultiNodeEC2MetadataReplicationTest
class MultiNodeEC2MetadataReplicationTestMultiJvmNode4 extends MultiNodeEC2MetadataReplicationTest
class MultiNodeEC2MetadataReplicationTestMultiJvmNode5 extends MultiNodeEC2MetadataReplicationTest

class TestEC2Replicator(info: EC2InstanceInfo, settings: EC2MetadataReplicationSettings) extends EC2MetadataReplicator(settings) {
  override def fetcherProps = TestEC2MetadataFetcher.props(self, info)
}

class MultiNodeEC2MetadataReplicationTest extends MultiNodeSpec(MultiNodeEC2MetadataReplicationTestConfig)
  with STMultiNodeSpec with ImplicitSender {
  import MultiNodeEC2MetadataReplicationTestConfig._

  override def initialParticipants: Int = roles.size

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()

  var replicator: ActorRef = _

  def join(from: RoleName, to: RoleName, number: Int): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
      startReplicator(createEC2Metadata(from, number))
    }
    enterBarrier(from.name + "-joined")
  }

  def startReplicator(info: EC2InstanceInfo) = {
    val props = Props(new TestEC2Replicator(info, EC2MetadataReplicationSettings(system)))
    replicator = system.actorOf(props)
  }

  def createEC2Metadata(roleName: RoleName, number: Int) = {
    val name = roleName.name
    EC2InstanceInfo(
      instanceId = s"i-$name",
      instanceType = "t1.micro",
      imageId = "ami-5fb8c835",
      architecture = "x86_64",
      kernelId = "aki-919dcaf8",
      region = "us-east-1",
      availabilityZone = "us-east-1d",
      privateIp = s"10.158.112.$number"
    )
  }

  "EC2 cluster" must {

    "join" in {
      join(node1, node1, 1)
      join(node2, node1, 2)
      join(node3, node1, 3)
      join(node4, node1, 4)
      join(node5, node1, 5)
    }

    "fetch EC2 metadata" in {
      runOn(node1, node2, node3, node4, node5) {
        awaitAssert({
          replicator ! EC2MetadataReplicator.GetMyEC2Metadata
          val result = expectMsgType[EC2MetadataReplicator.MyEC2Metadata](1 second)
          result.info.isDefined should be(true)
        }, 10 seconds, 1 second)
      }

      enterBarrier("fetch-metadata-completed")
    }

    "replicate EC2 metadata" in {

      runOn(node1, node2, node3, node4, node5) {

        awaitAssert({
          replicator ! EC2MetadataReplicator.GetClusterEC2Metadata
          val result = expectMsgType[EC2MetadataReplicator.CurrentClusterEC2Metadata](1 second)
          result.elements.size should be(initialParticipants)
        }, 10 seconds, 1 second)
      }

      enterBarrier("replication-completed")
    }
  }
}

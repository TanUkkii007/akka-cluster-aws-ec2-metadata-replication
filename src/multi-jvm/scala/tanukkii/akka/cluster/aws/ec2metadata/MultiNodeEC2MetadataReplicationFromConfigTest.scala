package tanukkii.akka.cluster.aws.ec2metadata

import akka.actor.{ActorRef, Props}
import akka.cluster.Cluster
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import scala.concurrent.duration._

class MultiNodeEC2MetadataReplicationFromConfigTestMultiJvmNode1 extends MultiNodeEC2MetadataReplicationFromConfigTest
class MultiNodeEC2MetadataReplicationFromConfigTestMultiJvmNode2 extends MultiNodeEC2MetadataReplicationFromConfigTest
class MultiNodeEC2MetadataReplicationFromConfigTestMultiJvmNode3 extends MultiNodeEC2MetadataReplicationFromConfigTest
class MultiNodeEC2MetadataReplicationFromConfigTestMultiJvmNode4 extends MultiNodeEC2MetadataReplicationFromConfigTest
class MultiNodeEC2MetadataReplicationFromConfigTestMultiJvmNode5 extends MultiNodeEC2MetadataReplicationFromConfigTest

class MultiNodeEC2MetadataReplicationFromConfigTest extends MultiNodeSpec(MultiNodeEC2MetadataReplicationTestConfig2)
with STMultiNodeSpec with ImplicitSender {
  import MultiNodeEC2MetadataReplicationTestConfig._

  override def initialParticipants: Int = roles.size

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()

  val replicator: ActorRef = EC2MetadataReplication(system).replicator

  def join(from: RoleName, to: RoleName, number: Int): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
    }
    enterBarrier(from.name + "-joined")
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

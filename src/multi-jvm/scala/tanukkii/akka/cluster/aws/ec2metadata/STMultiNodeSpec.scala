package tanukkii.akka.cluster.aws.ec2metadata

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{Matchers, WordSpecLike, BeforeAndAfterAll}

trait STMultiNodeSpec extends MultiNodeSpecCallbacks
with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}

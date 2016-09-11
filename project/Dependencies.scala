import sbt._

object Dependencies {

  object Akka {
    val version = "2.4.10"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val testKit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
    val multiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % version % "test"
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val distrubutedData = "com.typesafe.akka" %% "akka-distributed-data-experimental" % version
  }

  object ScalaTest {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  }

  object AkkaHttpAws {
    val version = "0.0.1"
    val core = "github.com/TanUkkii007" %% "akka-http-aws-core" % version
  }
}

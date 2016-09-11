import Dependencies._

name := "akka-cluster-aws-ec2-metadata-replication"

organization := "github.com/TanUkkii007"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8",
  "-language:implicitConversions",
  "-language:postfixOps"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

resolvers += Resolver.bintrayRepo("tanukkii007", "maven")

libraryDependencies ++= Seq(
  Akka.actor,
  Akka.cluster,
  Akka.distrubutedData,
  Akka.testKit,
  Akka.multiNodeTestKit,
  AkkaHttpAws.core,
  ScalaTest.scalaTest
)

compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test)

parallelExecution in Test := false

executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
  case (testResults, multiNodeResults) =>
    val overall =
      if (testResults.overall.id < multiNodeResults.overall.id)
        multiNodeResults.overall
      else
        testResults.overall
    Tests.Output(overall,
      testResults.events ++ multiNodeResults.events,
      testResults.summaries ++ multiNodeResults.summaries)
}

configs(MultiJvm)
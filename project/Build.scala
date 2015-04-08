import sbt._
import Keys._

object ProjectqBuild extends Build {
  import BuildSettings._
  import Dependencies._
  import com.typesafe.sbt.SbtMultiJvm
  import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
 
  lazy val scalaTest =  "org.scalatest" %% "scalatest" % "2.2.1" % "test"

  lazy val akka = "com.typesafe.akka" %% "akka-actor" % "2.3.6"

  lazy val akkaRemote = "com.typesafe.akka" %% "akka-remote" % "2.3.6"

  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % "2.3.6"

  lazy val akkaContrib = "com.typesafe.akka" %% "akka-contrib" % "2.3.6"

  lazy val akkaPersit = "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.6"		

  lazy val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % "2.3.6"

  lazy val scala = "org.scala-lang" % "scala-library" % "2.10.2"

  lazy val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.5"

  lazy val slf4japi = "org.slf4j" % "slf4j-api" % "1.7.5"

  lazy val spray_can = "io.spray" %% "spray-can" % "1.3.1"

  lazy val spray_routing = "io.spray" %% "spray-routing" % "1.3.1"

  lazy val spray_json = "io.spray"           %% "spray-json"       % "1.2.6"

  lazy val spray_test = "io.spray" %% "spray-testkit" % "1.3.1" % "test"

  lazy val spray_client = "io.spray" %%  "spray-client" % "1.3.1"

  lazy val akkaMultiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.3.6"

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test), // make sure that MultiJvm test are compiled by the default test compilation
    parallelExecution in Test := false,                                          // disable parallel tests
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((testResults), (multiJvmResults)) =>
          val overall =
            if (testResults.overall.id < multiJvmResults.overall.id) multiJvmResults.overall
            else testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiJvmResults.events,
            testResults.summaries ++ multiJvmResults.summaries)
      }
    )
 
  lazy val parent = Project(id = "projectq",
    base = file("."))
    .aggregate (example)
    .settings(basicSettings: _*)

  lazy val example = Project(id = "example", 
    base = file("example"),
    settings = Defaults.defaultSettings ++ multiJvmSettings,
    configurations = Configurations.default :+ MultiJvm)
    .settings(basicSettings: _*)
    .settings(unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "multi-jvm" / "scala")
    .settings(unmanagedResourceDirectories in Test += baseDirectory.value / "src" / "multi-jvm" / "resources")
    .settings( libraryDependencies ++= Seq(scalaTest, akka, akkaRemote, akkaCluster, akkaContrib, scala, akkaMultiNodeTestKit, spray_can, spray_routing, spray_json, spray_test, spray_client))
  

    
}

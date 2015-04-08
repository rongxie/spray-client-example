import sbt.Resolver.mavenLocal

scalaVersion := "2.10.3"

unmanagedBase := baseDirectory.value / "lib"

resolvers ++= Seq(
  mavenLocal,
  "Restlet Repository" at "http://maven.restlet.org/",
  "JBoss Repository" at "https://repository.jboss.org/nexus/content/repositories/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
)


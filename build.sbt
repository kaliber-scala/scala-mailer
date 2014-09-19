name := "play-mailer"

version := "2.2.0"

organization := "nl.rhinofly"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.11.2", "2.10.4")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  rhinoflyRepo("RELEASE").get
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.0" % "provided",
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.play" %% "play-test" % "2.3.4" % "test",
  "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test")

publishTo := rhinoflyRepo(version.value)

def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}

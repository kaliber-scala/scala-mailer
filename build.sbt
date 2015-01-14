name := "play-mailer"

version := "3.0.0"

organization := "nl.rhinofly"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  rhinoflyRepo("RELEASE").get
)

val playVersion = "2.3.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided" /* match all versions that are equal or higher */,
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.play" %% "play-test" % playVersion % "test",
  "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test")

publishTo := rhinoflyRepo(version.value)

def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}

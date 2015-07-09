name := "play-mailer"

organization := "nl.rhinofly"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases", /* specs2-core depends on scalaz-stream */
  rhinoflyRepo("RELEASE").get
)

val playVersion = "2.4.2"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided" /* match all versions that are equal or higher */,
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.play" %% "play-test" % playVersion % "test",
  "com.typesafe.play" %% "play-specs2" % playVersion % "test",
  "org.specs2" %% "specs2-core" % "3.6.2" % "test",
  "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test")

publishTo := rhinoflyRepo(version.value)

def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
}

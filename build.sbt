name := "play-mailer"

organization := "net.kaliber"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases" /* specs2-core depends on scalaz-stream */
)

val playVersion = "2.5.4"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided" /* match all versions that are equal or higher */,
  "javax.mail" % "mail" % "1.4.7",
  "com.typesafe.play" %% "play-test" % playVersion % "test",
  "com.typesafe.play" %% "play-specs2" % playVersion % "test",
  "org.specs2" %% "specs2-core" % "3.8.4" % "test",
  "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test",
  "com.typesafe.play" % "play-logback_2.11" % playVersion)

publishTo := {
  val repo = if (version.value endsWith "SNAPSHOT") "snapshot" else "release"
  Some("Kaliber Internal " + repo.capitalize + " Repository" at "https://jars.kaliber.io/artifactory/libs-" + repo + "-local")
}

fork in Test := true

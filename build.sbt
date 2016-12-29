
resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases" /* specs2-core depends on scalaz-stream */
)

lazy val commonSettings = Seq(
  organization := "net.kaliber",
  scalaVersion := "2.11.8"
)

lazy val core = (project in file("core"))
  .settings(
    name := "scala-mailer-core"
  )
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe"      % "config"       % "1.3.1"  % "provided",
      "javax.mail" % "mail" % "1.4.7"
    )
  )
  .settings(publishSettings: _*)
  .settings(testSettings: _*)

lazy val play = (project in file("play"))
  .settings(
    name := "scala-mailer-play"
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(testSettings: _*)
  .settings(playSettings: _*)
  .dependsOn(core % "compile->compile;test->test")

lazy val root = (project in file("."))
  .settings(
    name := "scala-mailer",
    publishArtifact := false
  )
  .settings(publishSettings: _*)
  .aggregate(core, play)


lazy val playSettings = {
  val playVersion = "2.5.0"

  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "com.typesafe.play" %% "play-test" % playVersion % "test",
    "com.typesafe.play" %% "play-specs2" % playVersion % "test"
  )
}

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "3.6.2" % "test",
    "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test"
  )
)

lazy val publishSettings = Seq(
  publishTo := {
    val repo = if (version.value endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Kaliber Internal " + repo.capitalize + " Repository" at "https://jars.kaliber.io/artifactory/libs-" + repo + "-local")
  }
)

fork in Test := true

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
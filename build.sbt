
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
  .settings(bintraySettings: _*)
  .settings(testSettings: _*)

lazy val play = (project in file("play"))
  .settings(
    name := "scala-mailer-play"
  )
  .settings(commonSettings: _*)
  .settings(bintraySettings: _*)
  .settings(testSettings: _*)
  .settings(playSettings: _*)
  .dependsOn(core % "compile->compile;test->test")

lazy val root = (project in file("."))
  .settings(
    name := "scala-mailer",
    publishArtifact := false
  )
  .settings(bintraySettings: _*)
  .settings(
    bintrayRelease := (),
    bintrayReleaseOnPublish := false,
    bintrayUnpublish := ()
  )
  .aggregate(core, play)


lazy val playSettings = {
  val playVersion = "2.5.11"

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

lazy val bintraySettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/kaliber-scala/scala-mailer")),
  bintrayOrganization := Some("kaliber-scala"),
  bintrayReleaseOnPublish := false,
  publishMavenStyle := true,
  
  pomExtra := (
    <scm>
        <connection>scm:git@github.com:kaliber-scala/scala-mailer.git</connection>
        <developerConnection>scm:git@github.com:kaliber-scala/scala-mailer.git</developerConnection>
        <url>https://github.com/kaliber-scala/scala-mailer</url>
    </scm>
    <developers>
        <developer>
        <id>Kaliber</id>
        <name>Kaliber Interactive</name>
        <url>https://kaliber.net/</url>
        </developer>
    </developers>
    )
)

fork in Test := true
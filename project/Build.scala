import sbt._
import Tests._
import Keys._
import play._

object ApplicationBuild extends Build {

  val appName = "play-mailer"
  val appVersion = "2.1.3"

  val compileDependencies = Seq("javax.mail" % "mail" % "1.4.7")
  val testDependencies = Seq("org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test")
  val appDependencies = testDependencies ++ compileDependencies

  def rhinoflyRepo(version: String) = {
    val repo = if (version endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }

  def singleTests(groups: Seq[Group]) = {
    println("asdadsads")
    groups.flatMap {
      _.tests.map { test =>
        println("soep")
        println(test)
        new Group(
          name = test.name,
          tests = Seq(test),
          runPolicy = SubProcess(javaOptions = Seq.empty[String]))
      }
    }
  }

  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "play.modules.mailer",
    resolvers += rhinoflyRepo("RELEASE").get,
    publishTo <<= version(rhinoflyRepo),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalacOptions += "-feature",
    // Group marked tests to make sure they run in isolation
    testGrouping in Test := {
      val original: Seq[Tests.Group] = (testGrouping in Test).value

      original.flatMap { group =>
        val (isolated, normal) =
          group.tests.partition(_.name endsWith "InIsolation")

        Seq(
          group.copy(tests = normal.sortBy(_.name)),
          group.copy(tests = isolated))
      }
    })

}

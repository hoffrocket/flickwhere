import sbt._

class UdorseLift(info: ProjectInfo) extends DefaultWebProject(info) {

  val liftSnapshotsRepo = "scala snapshots repository" at "http://scala-tools.org/repo-snapshots"
  
  val liftMapper = "net.liftweb" % "lift-mapper" % "1.1-M6" % "compile->default"
  val liftWebKit = "net.liftweb" % "lift-webkit" % "1.1-M6" % "compile->default"
  val liftJson = "net.liftweb" % "lift-json" % "1.1-M6" % "compile->default"
  val liftUtil = "net.liftweb" % "lift-util" % "1.1-M6" % "compile->default"
  val liftTextile = "net.liftweb" % "lift-textile" % "1.1-M6" % "compile->default"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.16" % "test->default"
  val jodaTime = "joda-time" % "joda-time" % "1.6"
  val jwebtest = "net.sourceforge.jwebunit" % "jwebunit-htmlunit-plugin" % "1.4.1" % "test->default"
  val st = "org.scala-tools.testing" % "scalatest" % "0.9.5" % "test->default"
  val commonsIo = "commons-io" % "commons-io" % "1.3.2"

  override def scanDirectories = Nil
  
  override def jettyPort = 8080 //systemOptional[Int]("jetty.port", 8080).value

}

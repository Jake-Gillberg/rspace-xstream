name := "rspace-xstream"

version := "0.1"

scalaVersion := "2.12.5"

libraryDependencies ++= Seq(
  "coop.rchain" %% "rspace" % "0.1.1",
  "com.thoughtworks.xstream" % "xstream" % "1.4.10"
)
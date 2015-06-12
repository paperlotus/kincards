name := """KinCards"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.neo4j" % "neo4j-rest-graphdb" % "2.0.1",
  "com.sun.jersey" % "jersey-core" % "1.9.1",
  "com.google.code.gson" % "gson" % "2.3.1",
  "com.googlecode.json-simple" % "json-simple" % "1.1",
  "javax.mail" % "mail" % "1.4.1",
  "com.googlecode.libphonenumber" % "libphonenumber" % "7.0.6",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.googlecode.ez-vcard" % "ez-vcard" % "0.9.6",
  "mysql" % "mysql-connector-java" % "5.1.18"
)

resolvers += "neo4j" at "http://m2.neo4j.org/content/groups/everything"

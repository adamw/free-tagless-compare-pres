import sbt._
import Keys._

name := "free-tagless-compare-pres"
organization := "com.softwaremill"
scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.0-MF",
  "org.typelevel" %% "cats-free" % "1.0.0-MF"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")


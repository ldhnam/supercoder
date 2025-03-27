val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "SuperCoder",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "com.openai" % "openai-java" % "0.33.0",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "org.jline" % "jline" % "3.21.0"
    )
  )

enablePlugins(JavaAppPackaging)
packageName := "supercoder"
mainClass := Some("com.supercoder.run")
maintainer := "hey@huy.rocks"

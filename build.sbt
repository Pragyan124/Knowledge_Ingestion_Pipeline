ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "KnowledgeIngestionPipeline",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.rometools" % "rome" % "2.1.0",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6"
    )
  )

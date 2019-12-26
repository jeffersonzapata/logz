lazy val commonSettings = Seq(
  name := "logz",
  scalaVersion := "2.13.1"
)

lazy val core = project
  .settings(
    commonSettings,
    name += "-core"
  )

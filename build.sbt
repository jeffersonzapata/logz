lazy val commonSettings = Seq(
  name := "logz",
  organization := "com.github.jeffersonzapata",
  scalaVersion := "2.13.1",
  addCompilerPlugin(Deps.betterMonadicFor),
  wartremoverErrors ++= Warts.allBut(Wart.Overloading),
  testFrameworks += new TestFramework("minitest.runner.Framework")
)

lazy val core = project
  .settings(
    commonSettings,
    name += "-core",
    libraryDependencies ++= Seq(
      Deps.miniTest % Test
    )
  )

lazy val `test-instances` = project
  .settings(
    commonSettings,
    name += "-test-instances",
    libraryDependencies ++= Seq(
      Deps.catsEffects,
      Deps.miniTest % Test
    )
  )
  .dependsOn(core)

lazy val `context-provider` = project
  .settings(
    commonSettings,
    name += "-context-provider",
    libraryDependencies ++= Seq(
      Deps.catsEffects,
      Deps.miniTest % Test
    )
  )
  .dependsOn(core, `test-instances` % Test)

lazy val slf4j = project
  .settings(
    commonSettings,
    name += "-slf4j",
    libraryDependencies ++= Seq(
      Deps.catsEffects,
      Deps.logstashEncoder,
      Deps.slf4jApi,
      Deps.miniTest  % Test,
      Deps.slf4jTest % Test
    )
  )
  .dependsOn(core)
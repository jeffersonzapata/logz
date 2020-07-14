lazy val commonSettings = Seq(
  name         := "logz",
  organization := "com.github.jeffersonzapata",
  scalaVersion := "2.13.1",
  addCompilerPlugin(Deps.betterMonadicFor),
  wartremoverErrors ++= Warts.allBut(Wart.Overloading),
  testFrameworks += new TestFramework("minitest.runner.Framework")
)

lazy val core = project
  .settings(
    homepage := Some(url("https://github.com/$ORG/$PROJECT")),
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
      Deps.scalaCompact,
      Deps.slf4jApi,
      Deps.miniTest  % Test,
      Deps.slf4jTest % Test
    )
  )
  .dependsOn(core)

inThisBuild(
  List(
    organization := "com.github.jeffersonzapata",
    licenses     := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage     := Some(url("https://github.com/jeffersonzapata/logz")),
    developers := List(
      Developer(
        "jeffersonzapata",
        "Jefferson Zapata",
        "jefferson.zapata.sierra@gmail.com",
        url("https://github.com/jeffersonzapata")
      )
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/jeffersonzapata/logz"), "scm:git:git@github.com:jeffersonzapata/logz.git")),
    pgpPublicRing      := file("/tmp/pubring.asc"),
    pgpSecretRing      := file("/tmp/secring.asc"),
    releaseEarlyWith   := SonatypePublisher,
    crossScalaVersions := Seq("2.11.12", "2.12.10", scalaVersion.value)
  ))

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt test:scalafmt")

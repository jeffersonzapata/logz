import sbt._

object Deps {

  object Versions {

    lazy val betterMonadicFor = "0.3.1"
    lazy val catsEffects = "2.0.0"
    lazy val miniTest = "2.7.0"
  }

  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
  lazy val catsEffects = "org.typelevel" %% "cats-effect"          % Versions.catsEffects withSources () withJavadoc ()
  lazy val miniTest = "io.monix" %% "minitest" % Versions.miniTest
}

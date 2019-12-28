import sbt._

object Deps {
  object Versions {
    lazy val betterMonadicFor = "0.3.1"
    lazy val catsEffects      = "2.0.0"
    lazy val logstashEncoder  = "6.2"
    lazy val miniTest         = "2.7.0"
    lazy val slf4jApi         = "2.0.0-alpha0"
    lazy val slf4jTest        = "2.1.1"
  }
  lazy val betterMonadicFor = "com.olegpy"             %% "better-monadic-for"      % Versions.betterMonadicFor
  lazy val catsEffects      = "org.typelevel"          %% "cats-effect"             % Versions.catsEffects withSources () withJavadoc ()
  lazy val logstashEncoder  = "net.logstash.logback"   % "logstash-logback-encoder" % Versions.logstashEncoder
  lazy val miniTest         = "io.monix"               %% "minitest"                % Versions.miniTest
  lazy val slf4jTest        = "com.github.valfirst"    % "slf4j-test"               % Versions.slf4jTest
  lazy val slf4jApi         = "org.slf4j"              % "slf4j-api"                % Versions.slf4jApi
}

# LogZ

Simple lightweight logging library open to extension, save some time and money with Log Buffer.

## Getting Started

Imports the modules you need
```sbt
"logz" %%  "logz-core"              % "0.0.1"  // core lightweight abstractions
"logz" %%  "logz-test-instances"    % "0.0.1"  // test instances
"logz" %%  "logz-slf4j"             % "0.0.1"  // slf4j instances
"logz" %%  "logz-context-provider"  % "0.0.1"
```

Allow your functions/libraries to be part of the app's history without committing to a specific implementation
(no needs external dependencies).
```scala
import logz.Logger

def myGreatLibrary[F[_]](implicit logger: Logger[F]): F[Unit] = {
    Logger.debug[F]("debug")
    //...
}
// or just
def myGreatLibrary[F[_]: Logger]: F[Unit] = {
  Logger.debug[F]("debug")
  //...
}
```

Choose how the history should be written.

```scala
// Improve your tests with an in memory logger
import logz.instances.TestLogger
import logz.instances.TestLoggerContext

// SLF4J instances
import logz.instances.slf4j.SLF4JLogger

// SLF4J instances with Context (if you want to understand your app's history you'll need some context).
import logz.instances.slf4j.SLF4JLoggerMarkerContext    // recommended
import logz.instances.slf4j.SLF4JLoggerMDCContext       // not recommend for multi-thread applications

// a LoggerContext instance with Context inside
import logz.context.providers.LoggerCtxtProvided
import logz.context.providers.LoggerCtxtProvidedBuffer  // (include the Log Buffer feature)

// Example
val slf4j: org.slf4j.Logger = TestLoggerFactory.getTestLogger("TestSLF4JLoggerMarkerContextSpec").pure[IO]
implicit val logger: LoggerContext[IO] = SLF4JLoggerMarkerContext[IO](slf4j)
myGreatLibrary[IO]()
```
[TestLogger Example]()
[TestLoggerContext Example]()
[SLF4JLogger Example]()
[SLF4JLoggerMarkerContext Example]()
[SLF4JLoggerMDCContext Example]()
[LoggerContextProvided Example]()

Do you want more choices? send yours!.

## Log Buffer

Log Buffer will help you to save disk space by writing the whole log's history only when you log a warning or an error

```scala
for {
 _ <- LoggerContextProvided.debug[IO]("debug")  // Writes `debug` in the log.
 _ <- LoggerContextProvided.debugB[IO]("debugB") // Doesn't write in the log, yet.
 _ <- LoggerContextProvided.infoB[IO]("infoB")   // Doesn't write in the log, yet.
 _ <- LoggerContextProvided.warnB[IO]("warnB")   // Writes `debugB`, `infoB` and `warnB` in log.
 _ <- LoggerContextProvided.errorB[IO]("errorB") // Writes 'errorB' in the log.
} yield ()
```








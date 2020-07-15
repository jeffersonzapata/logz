# LogZ

[![CircleCI](https://circleci.com/gh/jeffersonzapata/logz/tree/develop.svg?style=shield)](https://circleci.com/gh/jeffersonzapata/logz/tree/develop)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.jeffersonzapata/logz-core_2.13.svg)](https://repo.maven.apache.org/maven2/com/github/jeffersonzapata/logz-core_2.13/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Simple lightweight logging library
 - Composable
 - Modular
 - Open to extension
 - And it can help you to save some money with [Log Buffer](https://github.com/jeffersonzapata/logz#log-buffer) .

## Getting Started

Add the following dependencies to you `build.sbt`
```sbt
"com.github.jeffersonzapata" %%  "logz-core"              % "0.0.1"  // core lightweight abstractions
"com.github.jeffersonzapata" %%  "logz-test-instances"    % "0.0.1"  // test instances
"com.github.jeffersonzapata" %%  "logz-slf4j"             % "0.0.1"  // slf4j instances
"com.github.jeffersonzapata" %%  "logz-context-provider"  % "0.0.1"
```

### Core Module
let your functions/libraries be part of the application history without committing to a specific implementation
(it doesn't depends on any external dependencies).
```scala
import logz.Logger

def addTwoNumbers(a: Int, b: Int)(implicit logger: Logger[IO]): IO[Int] = {
  for {
    _ <- Logger.debug[IO](s"I'm adding $a plus $b")
    c <- IO(a + b)
    _ <- Logger.info[IO](s"The result is $c!")
  } yield c
}
```

### Logz Instances
Choose how the history should be written.
`logz-slf4j`, `logz-test-instances` and `logz-context-provider` provide useful implementations

#### `logz-slf4j`
`SLF4JLogger` provides a SLF4J basic implementation
```scala
import logz.Logger
import logz.instances.slf4j.SLF4JLogger

val slf4jLogger = org.slf4j.LoggerFactory.getLogger("LogzExample")

implicit val logger: Logger[IO] = SLF4JLogger[IO](slf4jLogger)
addTwoNumbers(4, 6)

//[main] DEBUG LogzExample  - I'm adding 4 plus 6
//[main] INFO  LogzExample  - the result is 10!
```

Add context to your logs with `LoggerContext`
```scala
// Context will marks you logs with a identifier
import logz.instances.slf4j.SLF4JLoggerMarkerContext    // recommended
import logz.instances.slf4j.SLF4JLoggerMDCContext       // not recommend for multi-thread applications

val slf4jLogger = org.slf4j.LoggerFactory.getLogger("LogzExample")

implicit val loggerContext: LoggerContext[IO] = SLF4JLoggerMarkerContext[IO](slf4jLogger)
val ctx: Context = Context()

for {
  result <- addTwoNumbers(4, 6)
  _      <- LoggerContext.info[IO](ctx)(s"I got the result $result!")
} yield result

//[main] DEBUG LogzExample  - I'm adding 4 plus 6      // you can add a `context_id` to these logs without modifying
//[main] INFO  LogzExample  - the result is 10!        // the `addTwoNumbers` function by using `LoggerCtxProvided`
//[main] INFO  LogzExample {context_id=e7ebce44-b7b0-4993-acdc-530d9e3d2bcc} - I got the result 10!
```


#### `logz-context-provider`
`LoggerCtxProvided` makes your life easier

```scala
import logz.instances.slf4j.SLF4JLoggerMarkerContext
import logz.syntax.context._

val slf4jLogger = org.slf4j.LoggerFactory.getLogger("LogzExample")
val loggerContext: LoggerContext[IO] = SLF4JLoggerMarkerContext[IO](slf4jLogger)
implicit val loggerCtxProvided: LoggerCtxProvided[IO] = Context().toLoggerCtxProvided(loggerContext)

for {
  result <- addTwoNumbers(4, 6)
  _      <- LoggerCtxProvided.info[IO](s"I got the result $result!") // yep, no need to pass `context1`
  _      <- LoggerCtxProvided.debug[IO](s"We got a context_id for free!")
} yield result

//[main] DEBUG LogzExample {context_id=a5a741a8-a0b6-4406-82f4-4cef0d883a8e} - I'm adding 4 plus 6
//[main] INFO  LogzExample {context_id=a5a741a8-a0b6-4406-82f4-4cef0d883a8e} - the result is 10!
//[main] INFO  LogzExample {context_id=a5a741a8-a0b6-4406-82f4-4cef0d883a8e} - I got the result 10!
//[main] DEBUG  LogzExample {context_id=a5a741a8-a0b6-4406-82f4-4cef0d883a8e} - We got a context_id for free
```

`LoggerCtxProvidedBuffer` takes care of your disk space and your time looking for errors
by writing the complete log's history only when a Warning or an Error is being logged

```scala
import logz.instances.slf4j.SLF4JLoggerMarkerContext
import logz.syntax.context._

val slf4jLogger = org.slf4j.LoggerFactory.getLogger("LogzExample")
val loggerContext: LoggerContext[IO] = SLF4JLoggerMarkerContext[IO](slf4jLogger)

Context().toLoggerCtxProvidedBuffer[IO, Int](loggerContext)(implicit logger =>
  for {
    result <- addTwoNumbers(4, 6)
    _ <- Logger.info[IO](s"I got the result $result!")
    _ <- LoggerCtxProvidedBuffer.debugB[IO](s"This will only be written if there is a Warning or an Error log")
    _ <- LoggerCtxProvidedBuffer.warnB[IO](s"I will write the previous logs")
    _ <- LoggerCtxProvidedBuffer.infoB[IO](s"I won't show up, you don't need me")
  } yield result
)

//[main] DEBUG LogzExample {context_id=95000a64-2d7b-4152-b1b6-869cef907eda} - I'm adding 4 plus 6
//[main] INFO  LogzExample {context_id=95000a64-2d7b-4152-b1b6-869cef907eda} - the result is 10!
//[main] INFO  LogzExample {context_id=95000a64-2d7b-4152-b1b6-869cef907eda} - I got the result 10!
//[main] DEBUG LogzExample {context_id=95000a64-2d7b-4152-b1b6-869cef907eda} - This will only be written if there is a Warning or an Error log
//[main] WARN  LogzExample {context_id=95000a64-2d7b-4152-b1b6-869cef907eda} - I will write the previous log
```

#### `logz-test-instances`
Provides your tests with an in memory logger

```scala
import logz.instances.TestLogger
import logz.instances.TestLoggerContext

// Example
val slf4j: org.slf4j.Logger = TestLoggerFactory.getTestLogger("TestSLF4JLoggerMarkerContextSpec").pure[IO]
implicit val logger: LoggerContext[IO] = SLF4JLoggerMarkerContext[IO](slf4j)
myGreatLibrary[IO]()
```

- [TestLogger Example]()
- [TestLoggerContext Example]()
- [SLF4JLogger Example]()
- [SLF4JLoggerMarkerContext Example]()
- [SLF4JLoggerMDCContext Example]()
- [LoggerContextProvided Example]()








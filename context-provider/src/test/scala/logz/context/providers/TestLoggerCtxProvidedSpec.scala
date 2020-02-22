package logz.context.providers

import cats.effect.IO
import cats.effect.concurrent.Ref
import logz.instances.TestLoggerContext
import logz.{Context, LoggerContext}
import minitest.SimpleTestSuite

object TestLoggerCtxProvidedSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    val context: Context = Context(Map("correlationId" -> "corId1"))
    val ex: Exception = new Exception("Exception")
    val result = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      implicit0(loggerContext: LoggerContext[IO]) = TestLoggerContext[IO](testLogger)
      implicit0(logger: LoggerCtxProvided[IO]) = LoggerCtxProvided[IO](context)
      _ <- LoggerCtxProvided.debug[IO]("debug")
      _ <- LoggerCtxProvided.info[IO]("info")
      _ <- LoggerCtxProvided.warn[IO]("warn")
      _ <- LoggerCtxProvided.error[IO]("error")
      _ <- LoggerCtxProvided.error[IO, Exception](ex)("error")
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId1), exception: Exception")
        assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlationId -> corId1)")
      }
    } yield ()
    result.unsafeToFuture()
  }

}

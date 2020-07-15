package logz.context.providers

import cats.effect.IO
import cats.effect.concurrent.Ref
import logz.instances.TestLoggerContext
import logz.{Context, Logger, LoggerContext}
import minitest.SimpleTestSuite

object TestLoggerCtxProvidedSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    import logz.syntax.context._
    val context: Context = Context(Map("correlationId" -> "corId1"))
    val ex: Exception    = new Exception("Exception")
    val result = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      loggerContext: LoggerContext[IO]         = TestLoggerContext[IO](testLogger)
      implicit0(logger: LoggerCtxProvided[IO]) = context.toLoggerCtxProvided[IO](loggerContext)
      _ <- Logger.debug[IO]("debug")
      _ <- Logger.info[IO]("info")
      _ <- Logger.warn[IO]("warn")
      _ <- Logger.error[IO]("error")
      _ <- Logger.error[IO, Exception](ex)("error")
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId1), exception: Exception")
      }
    } yield ()
    result.unsafeToFuture()
  }

}

package logz.instances

import cats.effect.IO
import cats.effect.concurrent.Ref
import logz.{Context, LoggerContext}
import minitest.SimpleTestSuite

object TestLoggerContextSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    val context1: Context = Context(Map("correlationId" -> "corId1"))
    val context2: Context = Context(Map("correlationId" -> "corId2"))
    val ex: Exception = new Exception("Exception")
    val result = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      implicit0(logger: LoggerContext[IO]) = TestLoggerContext[IO](testLogger)
      _ <- LoggerContext.debug[IO](context1)("debug")
      _ <- LoggerContext.info[IO](context2)("info")
      _ <- LoggerContext.warn[IO](context2)("warn")
      _ <- LoggerContext.error[IO](context1)("error")
      _ <- LoggerContext.error[IO, Exception](context2)(ex)("error")
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId2), exception: Exception")
        assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlationId -> corId1)")
        assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlationId -> corId2)")
        assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlationId -> corId2)")
        assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlationId -> corId1)")
      }
    } yield ()
    result.unsafeToFuture()
  }

}

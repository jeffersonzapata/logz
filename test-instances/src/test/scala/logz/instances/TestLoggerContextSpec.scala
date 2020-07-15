package logz.instances

import cats.effect.IO
import cats.effect.concurrent.Ref
import logz.{Context, LoggerContext}
import minitest.SimpleTestSuite

object TestLoggerContextSpec extends SimpleTestSuite {

  testAsync("should collect the logs in a list") {
    val context1: Context = Context(Map("correlationId" -> "0001"))
    val context2: Context = Context(Map("correlationId" -> "0002"))
    val ex: Exception     = new Exception("Exception")

    val result = for {
      ref <- Ref.of[IO, List[String]](List.empty[String])
      implicit0(logger: LoggerContext[IO]) = TestLoggerContext[IO](ref)
      _ <- LoggerContext.debug[IO](context1)("debug")
      _ <- LoggerContext.info[IO](context2)("info")
      _ <- LoggerContext.warn[IO](context2)("warn")
      _ <- LoggerContext.error[IO](context1)("error")
      _ <- LoggerContext.error[IO, Exception](context2)(ex)("error")
      _ <- ref.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlationId -> 0001)")
        assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlationId -> 0002)")
        assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlationId -> 0002)")
        assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlationId -> 0001)")
        assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlationId -> 0002), exception: Exception")
      }
    } yield ()

    result.unsafeToFuture()
  }

}

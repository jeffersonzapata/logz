package logz.context.providers

import cats.effect.concurrent.Ref
import cats.effect.{IO, Sync}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import logz.instances.TestLoggerContext
import logz.{Context, LoggerContext}
import minitest.SimpleTestSuite
import logz.syntax.context._

object TestLoggerCtxProvidedBufferSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    def app[F[_]: Sync: LoggerCtxProvidedBuffer]: F[String] =
      for {
        _ <- LoggerCtxProvidedBuffer.debug[F]("debug")
        _ <- LoggerCtxProvidedBuffer.info[F]("info")
        _ <- LoggerCtxProvidedBuffer.warn[F]("warn")
        _ <- LoggerCtxProvidedBuffer.error[F]("error")
        _ <- LoggerCtxProvidedBuffer.error[F, Exception](new Exception("Exception"))("error")
      } yield "result!"

    val ctx: Context = Context(Map("correlation_id" -> "corId1"))
    val result: IO[Unit] = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      loggerCtx: LoggerContext[IO] = TestLoggerContext[IO](testLogger)
      result <- ctx.toLoggerCtxProvidedBuffer[IO, String](loggerCtx)(implicit loggerWithContext => app[IO])
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlation_id -> corId1), exception: Exception")
      }
    } yield assertEquals(result, "result!")
    result.unsafeToFuture()
  }

  testAsync("should log to/from buffer correctly") {
    def app[F[_]: Sync: LoggerCtxProvidedBuffer](testLogger: Ref[F, scala.List[String]]): F[Unit] =
      for {
        _ <- LoggerCtxProvidedBuffer.debug[F]("debug")
        _ <- LoggerCtxProvidedBuffer.debugB[F]("debug")
        _ <- LoggerCtxProvidedBuffer.info[F]("info")
        _ <- LoggerCtxProvidedBuffer.infoB[F]("info")
        _ <- testLogger.get.map(logs => assertEquals(logs.size, 2))
        _ <- LoggerCtxProvidedBuffer.warn[F]("warn")
        _ <- LoggerCtxProvidedBuffer.warnB[F]("warn")
        _ <- testLogger.get.map(logs => assertEquals(logs.size, 6))
        _ <- LoggerCtxProvidedBuffer.error[F]("error")
        _ <- LoggerCtxProvidedBuffer.errorB[F, Exception](new Exception("Exception"))("error")
        _ <- testLogger.get.map { logs =>
          assertEquals(logs.size, 8)
          assertEquals(logs(7), "Level: DEBUG, msg: debug, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(6), "Level: INFO, msg: info, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(5), "Level: WARN, msg: warn, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(4), "Level: DEBUG, msg: debug, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(3), "Level: INFO, msg: info, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(2), "Level: WARN, msg: warn, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(1), "Level: ERROR, msg: error, ctx: Map(correlation_id -> corId1)")
          assertEquals(logs(0), "Level: ERROR, msg: error, ctx: Map(correlation_id -> corId1), exception: Exception")
        }
      } yield ()

    val ctx: Context = Context(Map("correlation_id" -> "corId1"))
    val result: IO[Unit] = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      loggerCtx: LoggerContext[IO] = TestLoggerContext[IO](testLogger)
      result <- ctx.toLoggerCtxProvidedBuffer[IO, Unit](loggerCtx) { implicit loggerWithContext: LoggerCtxProvidedBuffer[IO] =>
        app[IO](testLogger)
      }
    } yield result
    result.unsafeToFuture()
  }

  testAsync("should log from the buffer correctly if an exception happens") {
    val unhandledError: Throwable = new Throwable("Really bad error")
    def app[F[_]: Sync: LoggerCtxProvidedBuffer]: F[Unit] =
      for {
        _ <- LoggerCtxProvidedBuffer.debugB[F]("debug")
        _ <- LoggerCtxProvidedBuffer.infoB[F]("info")
        _ <- Sync[F].raiseError[Unit](unhandledError)
        _ <- LoggerCtxProvidedBuffer.warnB[F]("info")
      } yield ()

    val ctx: Context = Context(Map("correlation_id" -> "corId1"))
    val result: IO[Unit] = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      loggerCtx: LoggerContext[IO] = TestLoggerContext[IO](testLogger)
      result <- ctx.toLoggerCtxProvidedBuffer[IO, Unit](loggerCtx) { implicit loggerWithContext: LoggerCtxProvidedBuffer[IO] =>
        app[IO]
      }.handleError(_ => ())
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 3)
        assertEquals(logs(2), "Level: DEBUG, msg: debug, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(1), "Level: INFO, msg: info, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(0),s"Level: ERROR, msg: Unhandled Error, ctx: Map(correlation_id -> corId1), exception: ${unhandledError.getMessage}")
      }
    } yield result
    result.unsafeToFuture()
  }

  testAsync("should log from the buffer correctly if an exception happens (using *> or <*)") {
    import cats.syntax.apply._
    val unhandledError: Throwable = new Throwable("Really bad error")
    def app[F[_]: Sync: LoggerCtxProvidedBuffer]: F[Unit] =
      for {
        _ <- LoggerCtxProvidedBuffer.debugB[F]("debug")
        _ <- LoggerCtxProvidedBuffer.infoB[F]("info")
        _ <- Sync[F].raiseError[Unit](unhandledError) *> LoggerCtxProvidedBuffer.warnB[F]("info")
      } yield ()

    val ctx: Context = Context(Map("correlation_id" -> "corId1"))
    val result: IO[Unit] = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      loggerCtx: LoggerContext[IO] = TestLoggerContext[IO](testLogger)
      result <- ctx.toLoggerCtxProvidedBuffer[IO, Unit](loggerCtx) { implicit loggerWithContext: LoggerCtxProvidedBuffer[IO] =>
        app[IO]
      }.handleError(_ => ())
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 3)
        assertEquals(logs(2), "Level: DEBUG, msg: debug, ctx: Map(correlation_id -> corId1)")
        assertEquals(logs(1), "Level: INFO, msg: info, ctx: Map(correlation_id -> corId1)")
        assertEquals(
          logs(0),
          s"Level: ERROR, msg: Unhandled Error, ctx: Map(correlation_id -> corId1), exception: ${unhandledError.getMessage}")
      }
    } yield result
    result.unsafeToFuture()
  }

}

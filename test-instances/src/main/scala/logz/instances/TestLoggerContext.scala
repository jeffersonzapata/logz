package logz.instances

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.instances.map._
import cats.instances.string._
import cats.syntax.functor._
import cats.syntax.show._
import logz._

object TestLoggerContext {

  def apply[F[_]: Applicative](logger: Ref[F, List[String]]): LoggerContext[F] = new LoggerContext[F] {
    override def log(ctx: Context)(level: Level)(msg: => String): F[Unit] = level match {
      case Debug =>
        logger
          .modify(list => (s"Level: DEBUG, msg: $msg, ctx: ${ctx.entries.show}" :: list, list))
          .map(_ => ())
      case Info =>
        logger
          .modify(list => (s"Level: INFO, msg: $msg, ctx: ${ctx.entries.show}" :: list, list))
          .map(_ => ())
      case Warn =>
        logger
          .modify(list => (s"Level: WARN, msg: $msg, ctx: ${ctx.entries.show}" :: list, list))
          .map(_ => ())
      case Error(None) =>
        logger
          .modify(list => (s"Level: ERROR, msg: $msg, ctx: ${ctx.entries.show}" :: list, list))
          .map(_ => ())
      case Error(Some(th: Throwable)) =>
        logger
          .modify(list =>
            (s"Level: ERROR, msg: $msg, ctx: ${ctx.entries.show}, exception: ${th.getMessage}" :: list, list))
          .map(_ => ())
    }

    override def log(level: Level)(msg: => String): F[Unit] = TestLogger[F](logger).log(level)(msg)
  }
}

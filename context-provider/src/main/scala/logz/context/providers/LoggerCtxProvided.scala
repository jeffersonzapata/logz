package logz.context.providers

import logz._

sealed abstract class LoggerCtxProvided[F[_]] private extends Logger[F] {
  def log(level: Level)(msg: => String): F[Unit]
}

object LoggerCtxProvided {

  def apply[F[_]: LoggerContext](context: Context): LoggerCtxProvided[F] =
    new LoggerCtxProvided[F] {
      override def log(level: Level)(msg: => String): F[Unit] = LoggerContext[F].log(context)(level)(msg)
    }

  def apply[F[_]: LoggerCtxProvided]: LoggerCtxProvided[F] = implicitly

  def debug[F[_]: LoggerCtxProvided](msg: => String): F[Unit] = LoggerCtxProvided[F].log(Debug)(msg)

  def info[F[_]: LoggerCtxProvided](msg: => String): F[Unit] = LoggerCtxProvided[F].log(Info)(msg)

  def warn[F[_]: LoggerCtxProvided](msg: => String): F[Unit] = LoggerCtxProvided[F].log(Warn)(msg)

  def error[F[_]: LoggerCtxProvided](msg: => String): F[Unit] = LoggerCtxProvided[F].log(Error())(msg)

  def error[F[_]: LoggerCtxProvided, E <: Throwable](exception: E)(msg: => String): F[Unit] =
    LoggerCtxProvided[F].log(Error(exception))(msg)
}

package logz.context.providers

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import logz._

sealed abstract class LoggerCtxProvidedBuffer[F[_]] private extends Logger[F] {
  def log(level: Level)(msg: => String): F[Unit]
  def logToBuffer(level: Level)(msg: => String): F[Unit]
}

object LoggerCtxProvidedBuffer {

  def apply[F[A]: Sync: LoggerContext, A](context: Context)(f: LoggerCtxProvidedBuffer[F] => F[A]): F[A] = {
    for {
      buffer <- Ref[F].of(Sync[F].unit)
      implicit0(logB: LoggerCtxProvidedBuffer[F]) = LoggerCtxProvidedBuffer.create[F](context, buffer)
      result <- f(logB).handleErrorWith { error =>
        LoggerCtxProvidedBuffer.errorB[F, Throwable](error)("Unhandled Error").flatMap(_ => error.raiseError[F, A])
      }
    } yield result
  }

  private def create[F[_]: Sync: LoggerContext](context: Context, buffer: Ref[F, F[Unit]]): LoggerCtxProvidedBuffer[F] =
    new LoggerCtxProvidedBuffer[F] {
      override def log(level: Level)(msg: => String): F[Unit] = LoggerContext[F].log(context)(level)(msg)

      override def logToBuffer(level: Level)(msg: => String): F[Unit] = level match {
        case _ @(Warn | Error(_)) =>
          for {
            logs <- buffer.get
            _    <- logs
            _    <- LoggerContext[F].log(context)(level)(msg)
            _    <- buffer.modify(_ => (Sync[F].unit, ()))
          } yield ()
        case _ =>
          buffer.modify(f => (f.flatMap(_ => LoggerContext[F].log(context)(level)(msg)), ()))
      }
    }

  def apply[F[_]: LoggerCtxProvidedBuffer]: LoggerCtxProvidedBuffer[F] = implicitly

  def debug[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] = LoggerCtxProvidedBuffer[F].log(Debug)(msg)
  def debugB[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].logToBuffer(Debug)(msg)

  def info[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] = LoggerCtxProvidedBuffer[F].log(Info)(msg)
  def infoB[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].logToBuffer(Info)(msg)

  def warn[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] = LoggerCtxProvidedBuffer[F].log(Warn)(msg)
  def warnB[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].logToBuffer(Warn)(msg)

  def error[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].log(Error())(msg)
  def errorB[F[_]: LoggerCtxProvidedBuffer](msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].logToBuffer(Error())(msg)

  def error[F[_]: LoggerCtxProvidedBuffer, E <: Throwable](exception: E)(msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].log(Error(exception))(msg)
  def errorB[F[_]: LoggerCtxProvidedBuffer, E <: Throwable](exception: E)(msg: => String): F[Unit] =
    LoggerCtxProvidedBuffer[F].logToBuffer(Error(exception))(msg)
}

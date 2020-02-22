package logz.context.providers

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import logz._

sealed abstract class LoggerCtxProvidedBuffer[F[_]] private extends Logger[F] {
  def log(level: Level)(msg: => String): F[Unit]
  def logToBuffer(level: Level)(msg: => String): F[Unit]
}

object LoggerCtxProvidedBuffer {

  def apply[F[A]: Sync: LoggerContext, A](context: Context)(f: LoggerCtxProvidedBuffer[F] => F[A]): F[A] = {
    implicit val lcpb: LoggerCtxProvidedBuffer[F] = LoggerCtxProvidedBuffer.create[F](context)
    f(lcpb).handleErrorWith { error =>
      LoggerCtxProvidedBuffer.errorB[F, Throwable](error)("Unhandled Error") *> error.raiseError[F, A]
    }
  }

  private def create[F[_]: Sync: LoggerContext](context: Context): LoggerCtxProvidedBuffer[F] =
    new LoggerCtxProvidedBuffer[F] {
      override def log(level: Level)(msg: => String): F[Unit] = LoggerContext[F].log(context)(level)(msg)

      @SuppressWarnings(Array("org.wartremover.warts.Var"))
      private var logsBuffer: F[Unit] = Sync[F].unit
      override def logToBuffer(level: Level)(msg: => String): F[Unit] = level match {
        case _ @(Warn | Error(_)) =>
          for {
            _ <- logsBuffer
            _ <- LoggerContext[F].log(context)(level)(msg)
          } yield logsBuffer = Sync[F].unit
        case _ =>
          logsBuffer = logsBuffer.flatMap(_ => LoggerContext[F].log(context)(level)(msg))
          Sync[F].unit
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

package logz.syntax

import cats.effect.Sync
import logz.context.providers.{LoggerCtxProvided, LoggerCtxProvidedBuffer}
import logz.{Context, LoggerContext}

trait ContextSyntax {
  implicit final class LoggerCtxOps(private val context: Context) {
    def toLoggerCtxProvided[F[_]](loggerContext: LoggerContext[F]): LoggerCtxProvided[F] =
      LoggerCtxProvided[F](context)(loggerContext)

    def toLoggerCtxProvidedBuffer[F[_]: Sync, A](loggerContext: LoggerContext[F])(
      f: LoggerCtxProvidedBuffer[F] => F[A]): F[A] =
      LoggerCtxProvidedBuffer[F, A](context)(f)(Sync[F], loggerContext)
  }
}

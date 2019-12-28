package logz.instances.slf4j

import cats.effect.Sync
import cats.syntax.applicative._
import cats.syntax.apply._
import logz._
import org.slf4j.{MDC, Logger => SLF4J}

object SLF4JLoggerMDCContext {
  def apply[F[_]: Sync](logger: SLF4J): LoggerContext[F] =
    new LoggerContext[F] {
      override def log(level: Level)(msg: => String): F[Unit] = SLF4JLogger[F](logger).log(level)(msg)

      override def log(ctx: Context)(level: Level)(msg: => String): F[Unit] =
        Sync[F]
          .guarantee(
            ctx.entries.map { case (k, v) => MDC.put(k, v) }.pure[F] *> SLF4JLogger[F](logger).log(level)(msg)
          )(
            MDC.clear().pure[F]
          )
    }
}

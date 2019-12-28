package logz.instances.slf4j

import cats.Applicative
import cats.syntax.applicative._
import logz._
import org.slf4j.{Logger => SLF4J}

object SLF4JLogger {
  def apply[F[_]: Applicative](logger: SLF4J): Logger[F] = new Logger[F] {
    override def log(level: Level)(msg: => String): F[Unit] = level match {
      case Debug => logger.debug(msg).pure[F]
      case Info => logger.info(msg).pure[F]
      case Warn => logger.warn(msg).pure[F]
      case Error(None) => logger.error(msg).pure[F]
      case Error(Some(exception)) => logger.error(msg, exception).pure[F]
    }
  }
}

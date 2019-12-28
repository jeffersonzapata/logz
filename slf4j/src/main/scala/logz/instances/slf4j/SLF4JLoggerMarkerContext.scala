package logz.instances.slf4j

import cats.Applicative
import cats.syntax.applicative._
import logz._
import net.logstash.logback.marker.{LogstashMarker, Markers}
import org.slf4j.{Logger => SLF4J}

import scala.jdk.CollectionConverters._

object SLF4JLoggerMarkerContext {
  def apply[F[_]: Applicative](logger: Class[_]): LoggerContext[F] = apply[F](org.slf4j.LoggerFactory.getLogger(logger))

  def apply[F[_]: Applicative](logger: SLF4J): LoggerContext[F] =
    new LoggerContext[F] {
      override def log(level: Level)(msg: => String): F[Unit] = SLF4JLogger[F](logger).log(level)(msg)

      override def log(ctx: Context)(level: Level)(msg: => String): F[Unit] = {
        val logstashMarkers: LogstashMarker = Markers.appendEntries(ctx.entries.asJava)
        level match {
          case Debug => logger.debug(logstashMarkers, msg).pure[F]
          case Info => logger.info(logstashMarkers, msg).pure[F]
          case Warn => logger.warn(logstashMarkers, msg).pure[F]
          case Error(None) => logger.error(logstashMarkers, msg).pure[F]
          case Error(Some(th: Throwable)) => logger.error(logstashMarkers, msg, th).pure[F]
        }
      }
    }
}

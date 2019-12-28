package logz.instances.slf4j

import cats.effect.IO
import cats.syntax.applicative._
import com.github.valfirst.slf4jtest.LoggingEvent._
import com.github.valfirst.slf4jtest.TestLoggerFactory
import logz.{Context, LoggerContext}
import minitest.SimpleTestSuite
import net.logstash.logback.marker.Markers.appendEntries

import scala.jdk.CollectionConverters._

object TestSLF4JLoggerMarkerContextSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    val context1: Context = Context(Map("correlationId" -> "corId1"))
    val context2: Context = Context(Map("correlationId" -> "corId2"))
    val ex: Exception = new Exception("Exception")
    val result = for {
      slf4j <- TestLoggerFactory.getTestLogger("TestSLF4JLoggerMarkerContextSpec").pure[IO]
      implicit0(logger: LoggerContext[IO]) = SLF4JLoggerMarkerContext[IO](slf4j)
      _ <- LoggerContext.debug[IO](context1)("debug")
      _ <- LoggerContext.info[IO](context2)("info")
      _ <- LoggerContext.warn[IO](context2)("warn")
      _ <- LoggerContext.error[IO](context1)("error")
      _ <- LoggerContext.error[IO, Exception](context2)(ex)("error")
      _ <- IO.pure {
        val logs = slf4j.getLoggingEvents.asScala
        assertEquals(logs.size, 5)
        assertEquals(logs(0), debug(appendEntries(Map("correlationId" -> "corId1").asJava), "debug"))
        assertEquals(logs(1), info(appendEntries(Map("correlationId" -> "corId2").asJava), "info"))
        assertEquals(logs(2), warn(appendEntries(Map("correlationId" -> "corId2").asJava), "warn"))
        assertEquals(logs(3), error(appendEntries(Map("correlationId" -> "corId1").asJava), "error"))
        assertEquals(logs(4), error(appendEntries(Map("correlationId" -> "corId2").asJava), ex, "error"))
      }
    } yield ()
    result.unsafeToFuture()
  }

}

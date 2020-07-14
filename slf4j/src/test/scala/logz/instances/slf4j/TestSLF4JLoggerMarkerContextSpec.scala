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
    val context1: Context = Context(Map("correlation_id" -> "corId1"))
    val context2: Context = Context(Map("correlation_id" -> "corId2"))
    val ex: Exception     = new Exception("Exception")
    val result = for {
      slf4j <- TestLoggerFactory.getTestLogger("TestSLF4JLoggerMarkerContextSpec").pure[IO]
      implicit0(logger: LoggerContext[IO]) = SLF4JLoggerMarkerContext[IO](slf4j)
      _ <- LoggerContext.debug[IO](context1)("debug")
      _ <- LoggerContext.info[IO](context2)("info")
      _ <- LoggerContext.warn[IO](context2)("warn")
      _ <- LoggerContext.error[IO](context1)("error")
      _ <- LoggerContext.error[IO, Exception](context2)(ex)("error")
      _ <- IO.pure {
        val logs                                  = slf4j.getLoggingEvents.asScala
        val expectedContext1: Map[String, String] = context1.entries + ("context_id" -> context1.contextId)
        val expectedContext2: Map[String, String] = context2.entries + ("context_id" -> context2.contextId)
        assertEquals(logs.size, 5)
        assertEquals(logs(0), debug(appendEntries(expectedContext1.asJava), "debug"))
        assertEquals(logs(1), info(appendEntries(expectedContext2.asJava), "info"))
        assertEquals(logs(2), warn(appendEntries(expectedContext2.asJava), "warn"))
        assertEquals(logs(3), error(appendEntries(expectedContext1.asJava), "error"))
        assertEquals(logs(4), error(appendEntries(expectedContext2.asJava), ex, "error"))
      }
    } yield ()
    result.unsafeToFuture()
  }

}

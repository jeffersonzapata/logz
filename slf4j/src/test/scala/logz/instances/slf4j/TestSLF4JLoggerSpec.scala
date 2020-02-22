package logz.instances.slf4j

import cats.effect.IO
import cats.syntax.applicative._
import com.github.valfirst.slf4jtest.LoggingEvent._
import com.github.valfirst.slf4jtest.TestLoggerFactory
import logz.Logger
import minitest.SimpleTestSuite

import scala.jdk.CollectionConverters._

object TestSLF4JLoggerSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    val ex = new Exception("Exception")
    val result = for {
      slf4j <- TestLoggerFactory.getTestLogger("TestSLF4JLoggerSpec").pure[IO]
      implicit0(logger: Logger[IO]) = SLF4JLogger[IO](slf4j)
      _ <- Logger.debug[IO]("debug")
      _ <- Logger.info[IO]("info")
      _ <- Logger.warn[IO]("warn")
      _ <- Logger.error[IO]("error")
      _ <- Logger.error[IO, Exception](ex)("error")
      _ <- IO.pure {
        val logs = slf4j.getLoggingEvents.asScala
        assertEquals(logs.size, 5)
        assertEquals(logs(0), debug("debug"))
        assertEquals(logs(1), info("info"))
        assertEquals(logs(2), warn("warn"))
        assertEquals(logs(3), error("error"))
        assertEquals(logs(4), error(ex, "error"))
      }
    } yield ()
    result.unsafeToFuture()
  }

}

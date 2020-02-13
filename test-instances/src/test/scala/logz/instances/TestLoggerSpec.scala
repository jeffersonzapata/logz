package logz.instances

import cats.effect.IO
import cats.effect.concurrent.Ref
import logz.Logger
import minitest.SimpleTestSuite

object TestLoggerSpec extends SimpleTestSuite {

  testAsync("should log correctly") {
    val result = for {
      testLogger <- Ref.of[IO, List[String]](List.empty[String])
      implicit0(logger: Logger[IO]) = TestLogger[IO](testLogger)
      _ <- Logger.debug[IO]("debug")
      _ <- Logger.info[IO]("info")
      _ <- Logger.warn[IO]("warn")
      _ <- Logger.error[IO]("error")
      _ <- Logger.error[IO, Exception](new Exception("Exception"))("error")
      _ <- testLogger.get.map { logs =>
        assertEquals(logs.size, 5)
        assertEquals(logs(0), "Level: ERROR, msg: error, exception: Exception")
        assertEquals(logs(1), "Level: ERROR, msg: error")
        assertEquals(logs(2), "Level: WARN, msg: warn")
        assertEquals(logs(3), "Level: INFO, msg: info")
        assertEquals(logs(4), "Level: DEBUG, msg: debug")
      }
    } yield ()
    result.unsafeToFuture()
  }

}

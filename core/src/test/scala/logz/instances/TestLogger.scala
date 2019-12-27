package logz.instances

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import logz._

object TestLogger {

  def apply[F[_]: Applicative](logger: Ref[F, List[String]]): Logger[F] = new Logger[F] {
    override def log(level: Level)(msg: => String): F[Unit] = level match {
      case Debug =>
        logger.modify(list => (s"Level: DEBUG, msg: $msg" :: list, list)).map(_ => ())
      case Info =>
        logger.modify(list => (s"Level: INFO, msg: $msg" :: list, list)).map(_ => ())
      case Warn =>
        logger.modify(list => (s"Level: WARN, msg: $msg" :: list, list)).map(_ => ())
      case Error(None) =>
        logger.modify(list => (s"Level: ERROR, msg: $msg" :: list, list)).map(_ => ())
      case Error(Some(ex: Throwable)) =>
        logger.modify(list => (s"Level: ERROR, msg: $msg, exception: ${ex.getMessage}" :: list, list)).map(_ => ())
    }
  }
}

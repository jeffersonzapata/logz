package logz.instances

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import logz._

object TestLogger {

  def apply[F[_]: Applicative](ref: Ref[F, List[String]]): Logger[F] = new Logger[F] {
    override def log(level: Level)(msg: => String): F[Unit] = level match {
      case Debug =>
        ref.modify(list => (s"Level: DEBUG, msg: $msg" :: list, list)).map(_ => ())
      case Info =>
        ref.modify(list => (s"Level: INFO, msg: $msg" :: list, list)).map(_ => ())
      case Warn =>
        ref.modify(list => (s"Level: WARN, msg: $msg" :: list, list)).map(_ => ())
      case Error(None) =>
        ref.modify(list => (s"Level: ERROR, msg: $msg" :: list, list)).map(_ => ())
      case Error(Some(ex: Throwable)) =>
        ref.modify(list => (s"Level: ERROR, msg: $msg, exception: ${ex.getMessage}" :: list, list)).map(_ => ())
    }
  }
}

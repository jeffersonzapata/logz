package logz

trait Logger[F[_]] {
  def log(level: Level)(msg: => String): F[Unit]
}

object Logger {
  def apply[F[_]: Logger]: Logger[F] = implicitly

  def debug[F[_]: Logger](msg: => String): F[Unit] = Logger[F].log(Debug)(msg)

  def info[F[_]: Logger](msg: => String): F[Unit] = Logger[F].log(Info)(msg)

  def warn[F[_]: Logger](msg: => String): F[Unit] = Logger[F].log(Warn)(msg)

  def error[F[_]: Logger](msg: => String): F[Unit] = Logger[F].log(Error())(msg)

  def error[F[_]: Logger, E <: Throwable](exception: E)(msg: => String): F[Unit] =
    Logger[F].log(Error(exception))(msg)
}

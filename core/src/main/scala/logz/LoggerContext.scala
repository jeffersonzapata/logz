package logz

final case class Context(entries: Map[String, String])

trait LoggerContext[F[_]] extends Logger[F] {
  def log(ctx: Context)(level: Level)(msg: => String): F[Unit]
}

object LoggerContext {
  def apply[F[_]: LoggerContext]: LoggerContext[F] = implicitly

  def debug[F[_]: LoggerContext](ctx: Context)(msg: => String): F[Unit] =
    LoggerContext[F].log(ctx)(Debug)(msg)

  def info[F[_]: LoggerContext](ctx: Context)(msg: => String): F[Unit] =
    LoggerContext[F].log(ctx)(Info)(msg)

  def warn[F[_]: LoggerContext](ctx: Context)(msg: => String): F[Unit] =
    LoggerContext[F].log(ctx)(Warn)(msg)

  def error[F[_]: LoggerContext](ctx: Context)(msg: => String): F[Unit] =
    LoggerContext[F].log(ctx)(Error())(msg)

  def error[F[_]: LoggerContext, E <: Throwable](ctx: Context)(exception: E)(msg: => String): F[Unit] =
    LoggerContext[F].log(ctx)(Error(Option(exception)))(msg)
}

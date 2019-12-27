package logz

sealed trait Level

object Debug extends Level

object Info extends Level

object Warn extends Level

final case class Error[E <: Throwable] private (exception: Option[E]) extends Level

object Error {
  def apply[E <: Throwable](exception: E): Error[E] = new Error(Some(exception))
  def apply(): Error[Nothing] = new Error(None)
}

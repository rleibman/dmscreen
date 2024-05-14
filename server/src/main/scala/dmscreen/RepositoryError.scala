package dmscreen.dnd5e

import dmscreen.DMScreenError

object RepositoryError {

  def apply(cause: Throwable): RepositoryError = new RepositoryError("", Some(cause))

}

case class RepositoryError(
  override val msg:   String = "",
  override val cause: Option[Throwable] = None
) extends DMScreenError(msg, cause)

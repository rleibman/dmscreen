package dmscreen

opaque type UserId = Long

object UserId {

  def apply(userId: Long): UserId = userId

  extension (userId: UserId) {

    def value: Long = userId

  }

}

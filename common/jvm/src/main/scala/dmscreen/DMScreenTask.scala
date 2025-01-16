package dmscreen

import zio.ZIO

type DMScreenTask[+A] = ZIO[Any, DMScreenError, A] // Succeed with an `A`, may fail with `Throwable`, no requirements.

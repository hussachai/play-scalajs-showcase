package shared

import upickle.default._

// This @key is optional.  It makes the value of the @type field for heterogeneous lists not be fully qualified.
@key("Task")
case class Task(id: Option[Long] = None, txt: String, done: Boolean)

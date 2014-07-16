package example

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js

@JSExport
object Test {

  @JSExport
  def test() = {
    val i = js.Dynamic.literal("hello"->1)
  }

}

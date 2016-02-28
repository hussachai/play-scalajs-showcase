package shared

import utest._
import upickle.default._

/**
  * A test for [[Task]].
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 12/10/15
  *         Time: 7:07 AM
  */
object TaskTest extends TestSuite {
  def tests = TestSuite {
    "JSON format for Task" - {
      val expectedJson = """{"id":[],"txt":"clean room","done":false}"""

      "write" - {
        val json = write[Task](Task(None, "clean room", done = false))
        assert(json == expectedJson)
      }

      "read" - {
        val task = read[Task](expectedJson)
        assert(task == Task(None, "clean room", done = false))
      }
    }
  }
}

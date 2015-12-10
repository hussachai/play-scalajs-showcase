package example
package test

import utest._
import scala.scalajs.js
import js.Dynamic.{ global => g }

object ScalaJSExampleTest extends TestSuite {
  def tests = TestSuite {
    "ScalaJSExample" - {
      "should implement square()" - {
        import ExampleJS._

        assert(square(0) == 0)
        assert(square(4) == 16)
        assert(square(-5) == 25)
      }
    }
  }
}

import utest._
import play.api.test._
import play.api.test.Helpers._
import play.test.WithApplication

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
object ApplicationSpec extends TestSuite {
  def tests = TestSuite {
    "Application should" - {

      "send 404 on a bad request" - {
        new WithApplication {
          assert(route(FakeRequest(GET, "/boum")).isEmpty)
        }
      }

      "render the index page" - {
        new WithApplication {
          val home = route(FakeRequest(GET, "/")).get

          assert(status(home) == OK)
          assert(contentType(home).contains("text/html"))
          assert(contentAsString(home).contains("shouts out"))
        }
      }
    }
  }
}

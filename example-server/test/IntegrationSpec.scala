import utest._
import play.test.WithBrowser

//import play.api.test._
//import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
object IntegrationSpec extends TestSuite {
  def tests = TestSuite {
    "Application should" - {

      "work from within a browser" - {
        new WithBrowser {
          browser.goTo("http://localhost:" + port)

          assert(browser.pageSource.contains("shouts out"))
        }
      }
    }
  }
}

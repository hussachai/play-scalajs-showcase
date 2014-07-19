package controllers

import play.api.libs.json.Json
import play.api.mvc._
import shared.foo._

object InteropController extends Controller {

  implicit val kvFmt = Json.format[KV]
  implicit val fooFmt = Json.format[Foo]

  def index = Action{ implicit request =>
    val foo = Json.toJson(Foo())
    Ok(views.html.interop(foo.toString()))
  }

}

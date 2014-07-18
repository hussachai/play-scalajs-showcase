package controllers

import play.api.mvc._

object InteropController extends Controller {

  def index = Action{ implicit request =>
    Ok(views.html.interop())
  }

}

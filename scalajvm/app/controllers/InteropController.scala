package controllers

import play.api.mvc._

object InteropController extends Controller {

  def index = Action{
    Ok(views.html.interop())
  }

}

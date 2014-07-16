package controllers

import play.api.mvc.{Action, Controller}

object FileUploadController extends Controller {

  def index = Action {
    Ok(views.html.fileupload())
  }
}

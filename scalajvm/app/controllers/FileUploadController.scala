package controllers

import play.api.mvc.{Action, Controller}
import play.filters.csrf.CSRFAddToken
import shared.Csrf

object FileUploadController extends Controller {

  def index = CSRFAddToken { Action { implicit request =>
    import play.filters.csrf.CSRF
    val token = CSRF.getToken(request).map(t=>Csrf(t.value)).getOrElse(Csrf(""))
    Ok(views.html.fileupload(token))
  }}

  def upload = Action(parse.multipartFormData) { request =>
    println("WTF1")
    println(request.body.files)
    println("WTF2")
    request.body.file("fileSelect").map { file =>
      import java.io.File
      val filename = file.filename
      val contentType = file.contentType
//      picture.ref.moveTo(new File("/tmp/picture"))
      println(s"filename: $filename")
      Ok("File uploaded")
    }.getOrElse {
//      Redirect(routes.Application.index).flashing(
//        "error" -> "Missing file"
//      )
      BadRequest("Missing file")
    }
  }

}

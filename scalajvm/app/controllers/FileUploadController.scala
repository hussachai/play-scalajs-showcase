package controllers

import play.api.mvc.{Action, Controller}
import play.filters.csrf.CSRFAddToken
import shared.Csrf
import play.api.Play
import play.api.Play.current
import java.io.File
import java.util.UUID

object FileUploadController extends Controller {

  def index = CSRFAddToken { Action { implicit request =>
    import play.filters.csrf.CSRF
    val token = CSRF.getToken(request).map(t=>Csrf(t.value)).getOrElse(Csrf(""))
    Ok(views.html.fileupload(token))
  }}

  def upload = Action(parse.multipartFormData) { request =>
    println("FUCK")
    val upload = Play.getFile("upload").getAbsolutePath + File.separatorChar

    request.body.files.foreach{ file =>
      val extPos = file.filename.lastIndexOf(".")
      val ext = if(extPos != -1) file.filename.substring(extPos) else ""
      file.ref.moveTo(new File(upload+UUID.randomUUID.toString+ext), false)
    }

    Ok("File uploaded")

  }

}

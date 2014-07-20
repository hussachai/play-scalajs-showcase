package controllers

import models.ChatRoom
import play.api.libs.json.JsValue
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

object ChatController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.chat())
  }

  /**
   * Handles the chat websocket.
   */
  def chat(username: String) = WebSocket.tryAccept[JsValue] { request  =>

    ChatRoom.join(username).map{ io =>
      Right(io)
    }.recover{ case e => Left(Ok(e))}
  }

}

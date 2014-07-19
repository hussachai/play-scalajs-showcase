package controllers

import models.ChatRoom
import play.api.libs.json.JsValue
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

object ChatController extends Controller {

  /**
   * Just display the home page.
   */
  def index = Action { implicit request =>
    Ok(views.html.chat.index())
  }

  /**
   * Display the chat room page.
   */
  def chatRoom(username: Option[String]) = Action { implicit request =>
    username.filterNot(_.isEmpty).map { username =>
      Ok(views.html.chat.chatRoom(username))
    }.getOrElse {
      Redirect(routes.ChatController.index).flashing(
        "error" -> "Please choose a valid username."
      )
    }
  }

  def chatRoomJs(username: String) = Action { implicit request =>
    Ok(views.js.chat.chatRoom(username))
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

package example


import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.Dynamic.{ global => g }
import org.scalajs.dom
import scalatags.JsDom._
import all._
import rx._
import common.Framework._
import org.scalajs.jquery.{jQuery=>$}

@JSExport
object ChatJS {

  val maxMessages = 20

  var assetsDir: String = ""
  var wsBaseUrl: String = ""
  var socket: dom.WebSocket = null

  val username = Var("???")
  val wsUrl = Rx{wsBaseUrl+username()}

  def signInPanel = div(id:="signInPanel"){
    form(`class`:="form-inline", "role".attr:="form")(
      div(id:="usernameForm", `class`:="form-group")(
        div(`class`:="input-group")(
          div(`class`:="input-group-addon", raw("&#9786;")),
          input(id:="username", `class`:="form-control", `type`:="text", placeholder:="Enter username")
        )
      ), span(style:="margin:0px 5px"),
      button(`class`:="btn btn-default", onclick:={ () =>
        val input = $("#username").value().toString.trim
        if(input == "") {
          $("#usernameForm").addClass("has-error")
          dom.alert("Invalid username")
        }else{
          $("#usernameForm").removeClass("has-error")
          username() = input
          $("#username").value("")
          $("#signInPanel").addClass("hide")
          $("#chatPanel").removeClass("hide")
          socket = connectChatRoom
        }
        false
      })("Sign in")
    )
  }

  def chatPanel = div(id:="chatPanel", `class`:="hide")(
    div(`class`:="row", style:="margin-bottom: 10px;")(
      div(`class`:="col-md-12", style:="text-align: right;")(
        Rx {
          span(style := "padding: 0px 10px;", s"Login as: ${username()}")
        },
        button(`class`:="btn btn-default", onclick:={ () =>
          singOut
        }, "Sign out")
      )
    ),
    div(`class` := "panel panel-default")(
      div(`class` := "panel-heading")(
        h3(`class` := "panel-title")("Chat Room")
      ),
      div(`class` := "panel-body")(
        div(id := "messages")
      ),
      div(`class` := "panel-footer")(
        textarea(id:="message", `class` := "form-control message", placeholder := "Say something")
      )
    )
  )

  def createMessage(msg: String, username: String, avatar: String) = {
    div(`class`:=s"row message-box${if(username == this.username)"-me" else ""}")(
      div(`class`:="col-md-2")(
        div(`class`:="message-icon")(
          img(src:=s"$assetsDir/images/avatars/${avatar}", `class`:="img-rounded"),
          div(username)
        )
      ),
      div(`class`:="col-md-10")(raw(msg))
    )
  }

  def singOut = {
    socket.close()
    $("#signInPanel").removeClass("hide")
    $("#chatPanel").addClass("hide")
    $("#messages").html("")
  }

  def connectChatRoom = {

    val msgElem = dom.document.getElementById("messages")

    val socket = new dom.WebSocket(wsUrl())
    socket.onmessage = (e: dom.MessageEvent) => {
      val data = js.JSON.parse(e.data.toString)
      if(data.error.toString != "undefined"){
        dom.alert(data.error.toString)
        singOut
      }else{
        val user = data.user.name.toString
        val avatar = data.user.avatar.toString
        val message = data.message.toString
        msgElem.appendChild(createMessage(message, user, avatar).render)
        if(msgElem.childNodes.length >= maxMessages){
          msgElem.removeChild(msgElem.firstChild)
        }
        msgElem.scrollTop = msgElem.scrollHeight
      }
    }

    socket
  }

  def ready = {
    $("#message").keypress((e: dom.KeyboardEvent) => {
      if(!e.shiftKey && e.keyCode == 13) {
        e.preventDefault()
        val json = js.JSON.stringify(js.Dynamic.literal(text=$("#message").value()))
        socket.send(json)
        $("#message").value("")
      }
    })
  }

  @JSExport
  def main(settings: js.Dynamic) = {
    this.assetsDir = settings.assetsDir.toString
    this.wsBaseUrl = settings.wsBaseUrl.toString

    val content = dom.document.getElementById("content")
    content.appendChild(signInPanel.render)
    content.appendChild(chatPanel.render)
    ready
  }

}

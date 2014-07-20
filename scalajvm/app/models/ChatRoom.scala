package models

import java.io.{File, FileFilter}

import akka.actor._
import com.google.common.escape.Escaper
import com.google.common.html.HtmlEscapers
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.util.Random

object Robot {

  val jobsQuotes = Source.fromFile(play.Play.application().getFile("/public/text/jobs-quotes.txt")).mkString.split("\n")

  def apply(chatRoom: ActorRef) {

    // Create an Iteratee that logs all messages to the console.
    val loggerIteratee = Iteratee.foreach[JsValue](event => Logger("robot").info(event.toString))

    val robot = User("Robot", ChatRoom.randomAvatar())

    implicit val timeout = Timeout(1 second)
    // Make the robot join the room
    chatRoom ? (Join("Robot")) map {
      case Connected(robot, robotChannel) =>
        // Apply this Enumerator on the logger.
        robotChannel |>> loggerIteratee
    }

    def randomQuote = {
      chatRoom ! Talk(robot, jobsQuotes(ChatRoom.rand.nextInt(jobsQuotes.length)))
    }

    // Make the robot talk every 30 seconds
    Akka.system.scheduler.schedule(30 seconds, 30 seconds)(randomQuote)

  }

}

object ChatRoom {

  implicit val timeout = Timeout(1 second)

  val rand = new Random()
  val fileFilter = new FileFilter {
    override def accept(file: File): Boolean = file.getName.endsWith(".png")
  }

  val avatars = play.Play.application().getFile("/public/images/avatars").listFiles(fileFilter).map{f => f.getName}

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[ChatRoom])

    // Create a bot user (just for fun)
    Robot(roomActor)

    roomActor
  }

  def randomAvatar(): String = {
    avatars(rand.nextInt(avatars.length))
  }

  def join(username:String): Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(username)).map {

      case Connected(user, enumerator) =>

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! Talk(user, HtmlEscapers.htmlEscaper()
            .escape((event \ "text").as[String]).replace("\n", "<br/>"))
        }.map { _ =>
          default ! Quit(user)
        }

        (iteratee,enumerator)

      case CannotConnect(error) =>

        // Connection error

        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error))))
          .andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)

    }

  }

}

class ChatRoom extends Actor {

  var members = Set.empty[User]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case Join(username) => {
      if(members.find{u => u.username == username}.isDefined) {
        sender ! CannotConnect("This username is already used")
      } else {
        val user = User(username, ChatRoom.randomAvatar())
        members = members + user
        sender ! Connected(user, chatEnumerator)
        self ! NotifyJoin(user)
      }
    }

    case NotifyJoin(user) => {
      notifyAll("join", user, "has entered the room")
    }

    case Talk(user, text) => {
      notifyAll("talk", user, text)
    }

    case Quit(user) => {
      members = members - user
      notifyAll("quit", user, "has left the room")
    }

  }

  def notifyAll(kind: String, user: User, text: String) {
    def userToJson(u: User) = Json.obj("name"->u.username, "avatar"->u.avatar)
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> userToJson(user),
        "message" -> JsString(text),
        "members" -> JsArray(
          members.toList.map(userToJson(_))
        )
      )
    )
    chatChannel.push(msg)
  }

}

case class User(username: String, avatar: String)

case class Join(username: String)
case class Quit(user: User)
case class Talk(user: User, text: String)
case class NotifyJoin(user: User)

case class Connected(user: User, enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)

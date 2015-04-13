package models

import java.io.{File, FileFilter}

import akka.actor._
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
import play.{Logger=>log}
import scala.util.Random

trait ChatUserStore {

  def get(username: String): Option[User]

  def list(): List[User]

  def save(user: User): Boolean

  def remove(username: String): Boolean

}

object ChatUserMemStore extends ChatUserStore {

  import scala.collection.mutable.{Map=>MutableMap}

  val users = MutableMap.empty[String, String] //Map of username, avatar

  override def get(username: String): Option[User] = {
    users.get(username).map{User(username, _)}
  }

  override def list(): List[User] = {
    users.toList.map{e => User(e._1, e._2)}
  }

  override def save(user: User): Boolean = {
    users.put(user.username, user.avatar).map{s=>true}.getOrElse(false)
  }

  override def remove(username: String): Boolean = {
    users.remove(username).map{s=>true}.getOrElse(false)
  }


}

object Robot {

  val jobsQuotes = Source.fromFile(play.Play.application().getFile("/public/text/jobs-quotes.txt"), "UTF-8").mkString.split("\n")

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

  val store = ChatUserMemStore

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

  def join(username: String): Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(username)).map {

      case Connected(user, enumerator) =>
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          talk(user, (event \ "text").as[String])
        }.map { _ =>
          default ! Quit(user)
        }
        (iteratee, enumerator)

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

  def talk(user: User, msg: String): Unit = {
    default ! Talk(user, HtmlEscapers.htmlEscaper().escape(msg).replace("\n", "<br/>"))
  }

  def talk(username: String, msg: String): Unit = {
    ChatRoom.store.get(username).map{ user =>
      talk(user, msg)
    }.getOrElse{log.warn(s"Username: $username not found")}
  }
}

class ChatRoom extends Actor {

  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case Join(username) => {
      ChatRoom.store.get(username).map{ user =>
        println(ChatRoom.store.list())
        sender ! CannotConnect("This username is already used")
      }.getOrElse{
        val user = User(username, ChatRoom.randomAvatar())
        ChatRoom.store.save(user)
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
      ChatRoom.store.remove(user.username)
      notifyAll("quit", user, "has left the room")
    }

  }

  def notifyAll(kind: String, user: User, text: String) {
    def userToJson(user: User) = Json.obj("name"->user.username, "avatar"->user.avatar)
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> userToJson(user),
        "message" -> JsString(text),
        "members" -> JsArray(ChatRoom.store.list.map(userToJson(_)))
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

package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc._
import scala.concurrent.Future
import upickle._
import shared.Hangman

import scala.io.Source
import scala.util.Random

object HangmanController extends Controller{

  val sessionName = "hangman"

  val rand = new Random()

  val words = Source.fromInputStream(getClass.getResourceAsStream("/public/text/words.txt"))
      .mkString.split("[\\s,]+").filter(word => (word.length > 5 && word.forall(Character.isLetter)))

  def index = Action { implicit request =>
    Ok(views.html.hangman(readSession))
  }

  def start(level: Int) = Action { implicit request =>
    val word = words(rand.nextInt(words.length)).toUpperCase
    val value = write(Hangman(level, word))
    Ok(value).withSession(writeSession(value))
  }

  def session = Action{ implicit request =>
    readSession.map{ o => Ok(write(o)) }.getOrElse(NotFound)
  }

  def guess(g: Char) = Action { implicit request =>
    readSession.map{ hangman =>
      val misses = if(hangman.word.contains(g)) hangman.misses else hangman.misses + 1
      val value = write(hangman.copy(`guess` = hangman.guess :+ g, misses = misses))
      Ok(value).withSession(writeSession(value))
    }.getOrElse(BadRequest)
  }

  def giveup = Action { implicit request =>
    (if(isAjax) Ok else Redirect(routes.HangmanController.index()))
      .withSession(request.session - sessionName)
  }

  private def writeSession(value: String)(implicit request: RequestHeader) = {
    request.session + (sessionName -> value)
  }

  private def readSession(implicit request: RequestHeader): Option[Hangman] = {
    request.session.get(sessionName).map{ hangman =>
      read[Hangman](hangman)
    }
  }


}

package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc.{Result, Request, Action, Controller}
import scala.concurrent.Future
import upickle.{Json=>_,_}
import upickle.Implicits._
import shared.Hangman

object HangmanController extends Controller{

  def index = Action {
    Ok(views.html.hangman("Hangman"))
  }
}

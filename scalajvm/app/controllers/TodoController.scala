package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json=>_, _}
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.TaskModel
import upickle._
import upickle.Implicits._
import shared.Task

object TodoController extends Controller{

  implicit val taskPickler = Case3ReadWriter(Task.apply, Task.unapply)

  implicit val jsonReader = (
    (__ \ 'txt).read[String](minLength[String](2)) and
    (__ \ 'done).read[Boolean]
  ) tupled

  def index = Action {
    Ok(views.html.todo("TODO"))
  }

  def all = Action.async{ implicit request =>
    TaskModel.store.all.map{ r =>
      Ok(write(r))
    }.recover{ case err =>
      InternalServerError(err.getMessage)
    }
  }

  def create = Action.async(parse.json){ implicit request =>
    request.body.validate[(String, Boolean)].map{
      case (txt, done) => {
        TaskModel.store.create(txt, done).map{ r =>
          Ok(taskPickler.write(r).asInstanceOf[JsValue])
        }.recover{case e => InternalServerError}
      }
    }.recoverTotal{
      e => Future(BadRequest)
    }
  }

}

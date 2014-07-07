package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import models.TaskModel
import upickle._
import upickle.Implicits._
import shared.Task

object TodoController extends Controller{

  implicit val taskPickler = Case3ReadWriter(Task.apply, Task.unapply)

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

}

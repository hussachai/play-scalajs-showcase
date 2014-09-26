package controllers

import models.TaskMemStore.InsufficientStorageException
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc._
import scala.concurrent.Future
import models.TaskModel
import upickle._
import shared.Task

object TodoController extends Controller{

  implicit val jsonReader = (
    (__ \ 'txt).read[String](minLength[String](2)) and
    (__ \ 'done).read[Boolean]
  ).tupled

  def index = Action { implicit request =>
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
    val fn = (txt: String, done: Boolean) =>
      TaskModel.store.create(txt, done).map{ r =>
        Ok(write(r))
      }.recover{
        case e: InsufficientStorageException => InsufficientStorage(e)
        case e: Throwable => InternalServerError(e)
      }
    executeRequest(fn)
  }

  def update(id: Long) = Action.async(parse.json){ implicit request =>
    val fn = (txt: String, done: Boolean) =>
      TaskModel.store.update(Task(Some(id), txt, done)).map{ r =>
        if(r) Ok else BadRequest
      }.recover{ case e => InternalServerError(e)}
    executeRequest(fn)
  }

  def executeRequest(fn: (String, Boolean) => Future[Result])
    (implicit request: Request[JsValue]) = {
    request.body.validate[(String, Boolean)].map{
      case (txt, done) => {
        fn(txt, done)
      }
    }.recoverTotal{
      e => Future(BadRequest(e))
    }
  }

  def delete(id: Long) = Action.async{ implicit request =>
    TaskModel.store.delete(id).map{ r =>
      if(r) Ok else BadRequest
    }.recover{ case e => InternalServerError(e)}
  }

  def clear = Action.async{ implicit request =>
    TaskModel.store.clearCompletedTasks.map{ r =>
      Ok(write(r))
    }.recover{ case e => InternalServerError(e)}
  }

}

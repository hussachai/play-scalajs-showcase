package controllers

import models.TaskMemStore.InsufficientStorageException
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import upickle.Invalid
import scala.concurrent.Future
import models.TaskModel
import upickle.default._
import shared.Task

object TodoController extends Controller{

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

  def create = Action.async(parse.tolerantText){ implicit request =>
    executeRequestWithData[Task] { task =>
      TaskModel.store.create(task).map{ r =>
        Ok(write(r))
      }.recover{
        case e: InsufficientStorageException => InsufficientStorage(e)
        case e: Throwable => InternalServerError(e)
      }
    }
  }

  def update(id: Long) = Action.async(parse.tolerantText){ implicit request =>
    executeRequestWithData[Task] { task =>
      TaskModel.store.update(task.copy(id = Some(id))).map{ r =>
        if(r) Ok else BadRequest
      }.recover{ case e => InternalServerError(e)}
    }
  }

  private def executeRequestWithData[T : Reader](fn: T => Future[Result])
    (implicit request: Request[String]) = {
    if (!request.contentType.exists(_.endsWith("json"))) {
      Future(NotAcceptable)
    }
    try {
      val data = read[T](request.body)
      fn(data)
    } catch {
      case e: Invalid => Future(BadRequest(e))
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

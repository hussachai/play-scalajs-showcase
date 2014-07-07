package models

import scala.concurrent.Future
import shared.Task
import play.api.db.DB
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object TaskModel {

  val store: TaskStore = current.configuration.getString("module.todo.store") match {
    case Some(impl) => impl match {
      case "Anorm" => TaskAnormStore
    }
    case None => TaskMemStore
  }

  //some helper methods
}


trait TaskStore {

 def all: Future[List[Task]]

 def create(txt: String, done: Boolean): Future[Task]

 def update(task: Task): Future[Boolean]

 def delete(id: Int): Future[Boolean]

}

object TaskMemStore extends TaskStore {
  override def all(): Future[List[Task]] = Future{
    ???
  }

  override def create(txt: String, done: Boolean): Future[Task] = Future{
    ???
  }

  override def update(task: Task): Future[Boolean] = Future{
    ???
  }

  override def delete(id: Int): Future[Boolean] = Future{
    ???
  }
}

object TaskAnormStore extends TaskStore{
  import anorm._
  import anorm.SqlParser._

  override def all(): Future[List[Task]] = Future{
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Tasks").as(long("id").? ~ str("txt") ~ bool("done") *).map{
        case id ~ txt ~ done => Task(id, txt, done)
      }
    }
  }

  override def create(txt: String, done: Boolean): Future[Task] = Future{
    DB.withConnection { implicit c =>
      val id: Option[Long] = SQL("INSERT INTO Tasks(txt, done) VALUES({txt}, {done})")
        .on('txt -> txt, 'done -> done).executeInsert()
      Task(id, txt, done)
    }
  }

  override def update(task: Task): Future[Boolean] = Future{
    DB.withConnection { implicit c =>
      val sql = "UPDATE Tasks SET txt = {txt}, done = {done} WHERE id = {id}"
      SQL(sql).on('txt -> task.txt, 'done -> task.done
        , 'id -> task.id).executeUpdate() == 1
    }
  }

  override def delete(id: Int): Future[Boolean] = Future{
    DB.withConnection { implicit c =>
      val sql = "DELETE Tasks WHERE id = {id}"
      SQL(sql).on('id -> id).executeUpdate() == 1
    }
  }

}




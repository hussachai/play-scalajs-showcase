package models

import scala.concurrent.Future
import shared.Task
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global


object TaskModel {

  val store: TaskStore = current.configuration.getString("module.todo.store") match {
    case Some(impl) => impl match {
      case "Anorm" => TaskAnormStore
      case "Slick" => TaskSlickStore
    }
    case None => TaskMemStore
  }

  //some helper methods
}


trait TaskStore {

 def all: Future[List[Task]]

 def create(txt: String, done: Boolean): Future[Task]

 def update(task: Task): Future[Boolean]

 def delete(id: Long): Future[Boolean]

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

  override def delete(id: Long): Future[Boolean] = Future{
    ???
  }
}

object TaskSlickStore extends TaskStore {

  import play.api.db.slick.Config.driver.simple._
  //import scala.slick.driver.H2Driver.simple._
  import play.api.db.slick._

  //H2 always uses all upper case. That's annoying!!!
  class Tasks(tag: Tag) extends Table[Task](tag, "TASKS"){
    def id   = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def txt  = column[String]("TXT")
    def done = column[Boolean]("DONE")
    def * = (id, txt, done) <> (Task.tupled, Task.unapply)
  }

  val tasks = TableQuery[Tasks]

  override def all(): Future[List[Task]] = Future{
    DB.withSession{ implicit session =>
      tasks.sortBy(_.id.desc).list
    }
  }

  override def create(txt: String, done: Boolean): Future[Task] = Future{
    DB.withSession{ implicit session =>
      (tasks returning tasks.map(_.id) into ((task,id) => task.copy(id=id))) += Task(None, txt, done)
    }
  }

  override def update(task: Task): Future[Boolean] = Future{
    DB.withSession{ implicit session =>
      val q = for { t <- tasks if t.id === task.id } yield (t.txt, t.done)
      q.update(task.txt, task.done) == 1
    }
  }

  override def delete(id: Long): Future[Boolean] = Future{
    DB.withSession { implicit session =>
      tasks.filter(_.id === id).delete == 1
    }
  }
}

object TaskAnormStore extends TaskStore{
  import anorm._
  import anorm.SqlParser._
  import play.api.db.DB

  override def all(): Future[List[Task]] = Future{
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM Tasks ORDER BY id DESC").as(long("id").? ~ str("txt") ~ bool("done") *).map{
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

  override def delete(id: Long): Future[Boolean] = Future{
    DB.withConnection { implicit c =>
      val sql = "DELETE Tasks WHERE id = {id}"
      SQL(sql).on('id -> id).executeUpdate() == 1
    }
  }

}




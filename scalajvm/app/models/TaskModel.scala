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
      case _ => TaskMemStore
    }
    case None => TaskMemStore
  }

  //some helper methods
}


trait TaskStore {

  def all: Future[List[Task]]

  def create(txt: String, done: Boolean): Future[Task]

  def update(task: Task): Future[Boolean]

  def delete(ids: Long*): Future[Boolean]

  def clearCompletedTasks: Future[Int]
}

object TaskMemStore extends TaskStore {

  import scala.collection.mutable.{Map=>MutableMap}

  class InsufficientStorageException(m: String) extends Exception(m)

  val maxSize = 5
  val store = MutableMap.empty[Long, Task]
  store += (1L -> Task(Some(1L), "Upgrade Scala JS", true),
    2L -> Task(Some(2L), "Make it Rx", false),
    3L -> Task(Some(3L), "Make this example useful", false))

  var seq: Long = store.size

  def sequence() = {
    seq = seq + 1
    seq
  }

  override def all(): Future[List[Task]] = Future{
    store.values.toList.sortBy(- _.id.get)
  }

  override def create(txt: String, done: Boolean): Future[Task] = Future{
    if(store.size >= maxSize) throw new InsufficientStorageException("quota exceed for demo:"+maxSize)
    val task = Task(Some(sequence()), txt, done)
    store += (task.id.get -> task)
    task
  }

  override def update(task: Task): Future[Boolean] = Future{
    task.id.map{ id =>
      store += (id -> task)
      true
    }.getOrElse(false)
  }

  override def delete(ids: Long*): Future[Boolean] = Future{
    ids.foreach{ id =>
      store.remove(id)
    }
    true
  }

  override def clearCompletedTasks: Future[Int] = Future{
    store.values.foldLeft[Int](0){ case (c, task) =>
      if(task.done) {
        store -= task.id.get
        c + 1
      }else c
    }
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

  override def delete(ids: Long*): Future[Boolean] = Future{
    DB.withTransaction { implicit session =>
      (for(id <- ids) yield {
        tasks.filter(_.id === id).delete == 1
      }).find(i=>i==false) == None
    }
  }

  override def clearCompletedTasks: Future[Int] = Future{
    DB.withSession{ implicit session =>
      tasks.filter(_.done === true).delete
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

  override def delete(ids: Long*): Future[Boolean] = Future{
    DB.withTransaction { implicit c =>
      val sql = "DELETE Tasks WHERE id = {id}"
      (for(id <- ids) yield {
        SQL(sql).on('id -> id).executeUpdate() == 1
      }).find(i=>i==false) == None
    }
  }

  override def clearCompletedTasks: Future[Int] = Future{
    DB.withConnection { implicit c =>
      val sql = "DELETE Tasks WHERE done = true"
      SQL(sql).executeUpdate()
    }
  }

}




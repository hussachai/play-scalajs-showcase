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
      case "Gremlin" => TaskGremlinStore
      case _ => TaskMemStore
    }
    case None => TaskMemStore
  }

  //some helper methods
}


trait TaskStore {

  def all: Future[Seq[Task]]

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

  override def all(): Future[Seq[Task]] = Future{
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

  import play.api.db.DB
  import slick.driver.H2Driver.api._

  //H2 always uses all upper case. That's annoying!!!
  class Tasks(tag: Tag) extends Table[Task](tag, "TASKS"){
    def id   = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def txt  = column[String]("TXT")
    def done = column[Boolean]("DONE")
    def * = (id, txt, done) <> (Task.tupled, Task.unapply)
  }


  private def db: Database = Database.forDataSource(DB.getDataSource())

  val tasks = TableQuery[Tasks]

  override def all(): Future[Seq[Task]] = {
    db.run(tasks.sortBy(_.id.desc).result)
  }

  override def create(txt: String, done: Boolean): Future[Task] = {
    db.run{
      (tasks returning tasks.map(_.id) into ((task,id) => task.copy(id=id))) += Task(None, txt, done)
    }
  }

  override def update(task: Task): Future[Boolean] = {
    db.run{
      val q = for { t <- tasks if t.id === task.id } yield (t.txt, t.done)
      q.update(task.txt, task.done)
    }.map(_ == 1)
  }

  override def delete(ids: Long*): Future[Boolean] = {
    Future.sequence(for(id <- ids) yield { db.run(tasks.filter(_.id === id).delete).map(_==1)}).map{
      _.find(i => i == false) == None
    }
  }

  override def clearCompletedTasks: Future[Int] = {
    db.run{
      tasks.filter(_.done === true).delete
    }
  }
}

object TaskGremlinStore extends TaskStore {
  import gremlin.scala._
  import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

  val taskLabel = "task"
  val Name = Key[String]("name")
  val Done = Key[Boolean]("done")

  private val graph: ScalaGraph[TinkerGraph] = {
    val graph: ScalaGraph[TinkerGraph] = TinkerGraph.open().asScala
    (1 to 5) foreach { i =>
      graph + ("some_label", Name -> s"vertex $i")
    }
    graph
  }

  override def all: Future[Seq[Task]] = Future {
    graph.V.hasLabel(taskLabel).map { v => Task(Some(v.id().asInstanceOf[Long]), v.value2(Name), v.value2(Done)) }.toList()
  }

  override def create(txt: String, done: Boolean): Future[Task] = Future {
    val vertex = graph + (taskLabel, Name -> txt, Done -> done)
    Task(Some(vertex.id().asInstanceOf[Long]), txt, done)
  }

  override def update(task: Task): Future[Boolean] = Future {
    task.id.flatMap(graph.v(_)).map(_.setProperty(Name, task.txt).setProperty(Done, task.done)).isDefined
  }

  override def delete(ids: Long*): Future[Boolean] = Future {
    if (ids.isEmpty) {
      false
    } else {
      val vertices = graph.vertices(ids)
      vertices.foreach(_.remove())
      vertices.nonEmpty
    }
  }

  override def clearCompletedTasks: Future[Int] = Future {
    val tasks = graph.V.hasLabel(taskLabel).has(Done, true).toList()
    tasks.foreach(_.remove())
    tasks.size
  }
}

object TaskAnormStore extends TaskStore{
  import anorm._
  import anorm.SqlParser._
  import play.api.db.DB

  override def all(): Future[Seq[Task]] = Future{
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




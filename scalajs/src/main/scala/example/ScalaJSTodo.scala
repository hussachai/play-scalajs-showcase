package example

import org.scalajs.dom
import scalatags.JsDom._
import all._
import tags2.section
import rx._
import scala.scalajs.js.annotation.JSExport
import shared.Task
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

@JSExport
object ScalaJSTodo {
  import Framework._

  object API {
    import scala.scalajs.js
    import js.Dynamic.{global => g}
    import org.scalajs.dom.extensions.Ajax
    import org.scalajs.jquery.{jQuery=>$}
    import upickle._
    import upickle.Implicits._

    implicit val taskPickler = Case3ReadWriter(Task.apply, Task.unapply)

    val tasks = Var(List.empty[Task])

    def init: Future[Unit] = {
      Ajax.get("/todos/all").map { r =>
        read[List[Task]](r.responseText)
      }.map{ r =>
        tasks() = r
      }
    }

    def all: List[Task] = tasks()

    def create(txt: String, done: Boolean = false) = {
      val json = s"""{"txt": "${txt}", "done": ${done}}"""
      val headers = Seq("Content-Type" -> "application/json")
      Ajax.post("/todos/create", json, headers = headers).map{ r =>
        val task = read[Task](r.responseText)
        tasks() = task +: API.tasks()
      }
    }

  }

  val editing = Var[Option[Task]](None)

  object Sequence {
    var sequence = 0l
    def inc = {
      sequence = sequence + 1
      Some(sequence)
    }
  }

//  val tasks = Var(
//    Seq(
//      Task(Sequence.inc, "TodoMVC Task A", true),
//      Task(Sequence.inc, "TodoMVC Task B", false),
//      Task(Sequence.inc, "TodoMVC Task C", false)
//    )
//  )

  val filter = Var("All")

  val filters = Map[String, Task => Boolean](
    ("All", t => true),
    ("Active", !_.done),
    ("Completed", _.done)
  )

  val done = Rx{ API.tasks().count(_.done)}

  val notDone = Rx{ API.tasks().length - done()}

  val inputBox = input(
    id:="new-todo",
    placeholder:="What needs to be done?",
    autofocus:=true
  ).render

  def renderHead = {
    header(id:="header")(
      h1("todos"),
      form(
        inputBox,
        onsubmit := { () =>
          API.create(inputBox.value)
          inputBox.value = ""
          false
        }
      )
    )
  }

  def renderBody = {
    section(id:="main")(
      input(
        id:="toggle-all",
        `type`:="checkbox",
        cursor:="pointer",
        onclick := { () =>
          val target = API.tasks().exists(_.done == false)
//          Var.set(tasks().map(_.done -> target): _*)
        }
      ),
      label(`for`:="toggle-all", "Mark all as complete"),
      renderList,
      renderControls
    )
  }

  def renderFooter = {
    footer(id:="info")(
      p("Double-click to edit a todo"),
      p(a(href:="https://github.com/lihaoyi/workbench-example-app/blob/todomvc/src/main/scala/example/ScalaJSExample.scala")("Source Code")),
      p("Created by ", a(href:="http://github.com/lihaoyi")("Li Haoyi"))
    )
  }

  def renderList = Rx {
    ul(id := "todo-list")(
      for (task <- API.tasks() if filters(filter())(task)) yield {
        val inputRef = input(`class` := "edit", value := task.txt).render

        li(
          `class` := Rx{
            if (task.done) "completed"
            else if (editing() == Some(task)) "editing"
            else ""
          },
          div(`class` := "view")(
            "ondblclick".attr := { () =>
              editing() = Some(task)
            },
            input(
              `class` := "toggle",
              `type` := "checkbox",
              cursor := "pointer",
              onchange := { () =>
                API.tasks() = API.tasks().updated(API.tasks().indexOf(task), task.copy(done = !task.done))
              },
              if (task.done) checked := true
            ),
            label(task.txt),
            button(
              `class` := "destroy",
              cursor := "pointer",
              onclick := { () =>API.tasks() = API.tasks().filter(_ != task) }
            )
          ),
          form(
            onsubmit := { () =>
              API.tasks() = API.tasks().updated(API.tasks().indexOf(task), task.copy(txt = inputRef.value))
              editing() = None
              false
            },
            inputRef
          )
        )
      }
    )
  }

  def renderControls = {
    footer(id:="footer")(
      span(id:="todo-count")(strong(notDone), " item left"),
      ul(id:="filters")(
        for ((name, pred) <- filters.toSeq) yield {
          li(a(
            `class`:=Rx{
              if(name == filter()) "selected"
              else ""
            },
            name,
            href:="#",
            onclick := {() => filter() = name}
          ))
        }
      ),
      button(
        id:="clear-completed",
        onclick := { () => API.tasks() = API.tasks().filter(!_.done) },
        "Clear completed (", done, ")"
      )
    )
  }

  @JSExport
  def main(): Unit = {
    dom.document.body.innerHTML = ""
    API.init.map { r =>
      dom.document.body.appendChild(
        section(id:="todoapp")(
          renderHead,
          renderBody,
          renderFooter
        ).render
      )
    }
  }


}
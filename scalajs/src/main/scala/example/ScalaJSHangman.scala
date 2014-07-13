package example

import scala.scalajs.js.annotation.JSExport
import common.Framework._

import org.scalajs.dom
import scalatags.JsDom._
import all._
import rx._
import shared.Hangman

@JSExport
object ScalaJSHangman {

  object Model {

    val level = Var(0)
    val word = Var("")
    val misses = Var(0)
    val guessWord = Var(List("_"))


  }

  var currentNode: dom.Node = renderPlay.render

  def renderGuess: TypedTag[dom.HTMLElement] = {
    div(
      Rx {
        div(
          h2("Please make a guess"),
          h3(style := "letter-spacing: 4px;")(Model.guessWord()),
          p(s"You have made ${Model.misses()} bad guesses out of a maximum of ${Model.level()}"),
          p("Guess:")(
            for(c <- ('A' until 'Z').map(_.toString); if !Model.guessWord().contains(c)) yield {
              a(c)(style:="padding-left:5px;", href:="javascript:void(0);", onclick:={ () =>
                Model.guessWord() = Model.guessWord() :+ c
              })
            }
          ),
          input(`type` := "button", value := "Back!", onclick := { () =>
//            dom.alert(s"${Model.level()}")
            Model.level() = Model.level() + 1
            dom.document.replaceChild(renderPlay.render, dom.document.lastChild)
          })
        )
      }
    )
  }

  def renderPlay: TypedTag[dom.HTMLElement] = {
    val levels = Array(
      (10, "Easy game; you are allowed 10 misses."),
      (5, "Medium game; you are allowed 5 misses."),
      (3, "Hard game; you are allowed 3 misses.")
    )
    div(
      p("This is the game of Hangman. You must guess a word, a letter at a time.\n" +
        "If you make too many mistakes, you lose the game!"),
      form(id := "playForm")(
        for((level,text) <- levels) yield {
          val levelId = s"level_${level}"
          p(
            input(id:=levelId, `type`:="radio", name:="level"),
            label(`for`:=levelId)(text)
          )
        },
        input(`type`:="button", value:="Play!", onclick:={ () =>
//          dom.alert("Hello")
          Model.level() = Model.level() + 1
          dom.document.replaceChild(renderGuess.render, dom.document.lastChild)
        })
      )
    )
  }

  @JSExport
  def main(): Unit = {
    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(currentNode)
  }

}
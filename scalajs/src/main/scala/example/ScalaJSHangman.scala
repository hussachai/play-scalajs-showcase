package example


import common.Framework._
import config.Routes
import upickle.Implicits._
import scalatags.JsDom._
import all._
import rx._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import js.Dynamic.{global => g, _}
import org.scalajs.jquery.{jQuery=>$}
import shared.Hangman
import upickle._
import upickle.Implicits._

@JSExport
object ScalaJSHangman {

  implicit val hangmanPickler = Case4ReadWriter(Hangman.apply, Hangman.unapply)

  object Model {

    import org.scalajs.dom.extensions.Ajax
    import common.ExtAjax._

    val level = Var(0)
    val game = Var(Hangman(0, ""))

    def start(level: Int = Model.level()) = {
      Ajax.postAsForm(Routes.Hangman.start(level)).map{ r =>
        game() = r.responseAs[Hangman]
      }
    }

    def session(callback: (Boolean) => Unit) = {
      Ajax.get(Routes.Hangman.session).map { r =>
          game() = r.responseAs[Hangman]
          level() = game().level
          callback(true)
      }.recover{case e => callback(false)}
    }

    def guess(g: Char)(callback: ()=>Unit) = {
      Ajax.postAsForm(Routes.Hangman.guess(g)).map{ r =>
        if(r.ok){
          Model.game() = r.responseAs[Hangman]
          callback()
        }
      }
    }
  }

  var currentNode: dom.Node = null

  val header = h1("Hangman Game")

  def pageGuess: TypedTag[dom.HTMLElement] = div(`class`:="content"){
    div(header,
      Rx {
        div(
          h2("Please make a guess"),
          h3(style := "letter-spacing: 4px;")(Model.game().guessWord),
          p(s"You have made ${Model.game().misses} bad guesses out of a maximum of ${Model.game().level}"),
          p("Guess:")(
            for(c <- ('A' until 'Z'); if !Model.game().guess.contains(c)) yield {
              a(c)(style:="padding-left:5px;", href:="javascript:void(0);", onclick:={ () =>
                Model.guess(c){ () =>
                  if(Model.game().gameOver) {
                    goto(pageResult)
                  }
                }
              })
            }
          ),
          input(`type` := "button", value := "Back!", onclick := { () =>
            goto(pagePlay)
          })
        )
      }
    )
  }

  def pagePlay: TypedTag[dom.HTMLElement] = div{
    val levels = Array(
      (10, "Easy game; you are allowed 10 misses."),
      (5, "Medium game; you are allowed 5 misses."),
      (3, "Hard game; you are allowed 3 misses.")
    )
    div(header,
      p("This is the game of Hangman. You must guess a word, a letter at a time.\n" +
        "If you make too many mistakes, you lose the game!"),
      form(id := "playForm")(
        for((level,text) <- levels) yield {
          val levelId = s"level_${level}"
          p(
            input(id:=levelId, `type`:="radio", name:="level", onclick:={ ()=>
              Model.level() = level
            }, {if(level == Model.level()) checked:="checked"}),
            label(`for`:=levelId)(text)
          )
        },
        input(`type`:="button", value:="Play!", onclick:={ () =>
          if(Model.level() > 0) {
            Model.start()
            goto(pageGuess)
          }else{
            dom.alert("Please select level!")
          }
        })
      )
    )
  }

  def pageResult: TypedTag[dom.HTMLElement] = div{
    val result = if(Model.game().won) "You Win!" else "You Lose!"
    div(header,
      h2(result),
      p(s"The word was: ${Model.game().word}"),
      input(`type`:="button", value:="Start Again!", onclick:={ () =>
        goto(pagePlay)
      })
    )
  }

  def goto(page: TypedTag[dom.HTMLElement]) = {
    dom.document.replaceChild(div(style:="margin-left:20px;")(page).render, dom.document.lastChild)
  }

  @JSExport
  def main(): Unit = {
    Model.session{ (resume) => {
      currentNode = if(resume) {
        if(Model.game().gameOver) pageResult.render else pageGuess.render
      } else pagePlay.render
      dom.document.body.innerHTML = ""
      dom.document.body.appendChild(currentNode)
    }}
  }

}
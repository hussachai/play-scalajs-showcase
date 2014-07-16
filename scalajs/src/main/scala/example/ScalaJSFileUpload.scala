package example

import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import js.Dynamic.{global => g, _}
import scalatags.JsDom._
import all._

@JSExport
object ScalaJSFileUpload {

  def markup = div(
    h1("HTML5 File Drag &amp; Drop API"),
    p("""This is a demonstration of the HTML5 file drag &amp; drop API with asynchronous Ajax file uploads,
      graphical progress bars and progressive enhancement."""),
    form(id:= "", action:="", target:="", "method".attr:="POST", "enctype".attr:="multipart/form-data"){
      fieldset (
        legend("HTML File Upload"),
        input(`type` := "hidden", id := "maxFileSize", name := "maxFileSize", value := "300000"),
        div (
          label(`for` := "", "Files to upload:"),
          input(`type` := "file", id := "fileSelect", name := "fileSelect[]", "multiple".attr := "multiple"),
          div(id := "fileDrag")("or drop files here")
        ),
        div(id := "submitButton") (
          button(`type` := "submit")("Upload Files")
        )
      )
    },
    div(id:="progress"),
    div(id:="messages")(p("Status Messages")),
    br,
    h2("Disclaimer"),
    p("The original code was developed by ", a(href:="http://twitter.com/craigbuckler", "Craig Buckler"), " of ",
      a(href:="http://optimalworks.net/", "OptimalWorks.net"), " for ",
      a(href:="http://sitepoint.com/", "SitePoint.com"), ".")
  )

  def scripts = {

    def $(s: String) = dom.document.getElementById(s)

    def fileSelectHandler(e: dom.Event) = {
      dom.alert("YES")
    }

    def fileDragHover(e: dom.Event) = {
      e.stopPropagation()
      e.preventDefault()
//      e.target
    }

    def init = {

      val submitButton = $("submitButton")
      $("fileSelect").addEventListener("change", fileSelectHandler _, false)
      val xhr = new XMLHttpRequest
      if(xhr.upload != null){
        val fileDrag = $("fileDrag")
//        fileDrag.addEventListener("dragover", )
      }
    }

    init

    script {
      val message = "Hello"

//      dom.alert(message)
    }
  }

  @JSExport
  def main() = {
    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(markup.render)
    dom.document.body.appendChild(scripts.render)
  }
}

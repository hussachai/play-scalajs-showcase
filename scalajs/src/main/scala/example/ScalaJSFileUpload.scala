package example

import java.io.FileReader

import org.scalajs.dom
import shared.Csrf
import scala.scalajs.js
import scala.scalajs.js.Any
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import js.Dynamic.{global => g, _}
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$,_}

@JSExport
object ScalaJSFileUpload {

  def markup(csrfToken: String) = div(
    h1("HTML5 File Drag &amp; Drop API"),
    p("""This is a demonstration of the HTML5 file drag &amp; drop API with asynchronous Ajax file uploads,
      graphical progress bars and progressive enhancement."""),
    form(id:= "upload", action:=s"/upload",
      target:="", "method".attr:="POST", "enctype".attr:="multipart/form-data"){
      fieldset (
        legend("HTML File Upload"),
        input(`type`:="hidden", name:="csrfToken", value:=csrfToken),
        div (
          label(`for` := "", "Files to upload:"),
          input(`type` := "file", id := "fileSelect", name := "fileSelect", "multiple".attr := "multiple"),
          div(id := "fileDrag", backgroundColor:= "green")("or drop files here")
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

  trait EventTargetExt extends dom.EventTarget {
    var files: dom.FileList = ???
    var className: String = ???
    var result: js.Any = ???

  }
  trait EventExt extends dom.Event {
    var dataTransfer: dom.DataTransfer = ???

    var loaded: Int = ???
    var total: Int = ???
  }
  trait FileReaderExt extends dom.FileReader {
    var onload: js.Function1[dom.Event, _] = ???
  }


  def scripts = {
    implicit def monkeyizeEventTarget(e: dom.EventTarget): EventTargetExt = e.asInstanceOf[EventTargetExt]
    implicit def monkeyizeEvent(e: dom.Event): EventExt = e.asInstanceOf[EventExt]
    implicit def monkeyizeFileReader(e: dom.FileReader): FileReaderExt = e.asInstanceOf[FileReaderExt]

    val maxFileSize = 3000000l

    def $id(s: String) = dom.document.getElementById(s)

    def output(msg: String) = {
      val m = $id("messages")
      m.innerHTML = msg + m.innerHTML
    }

    def fileDragHover(e: dom.Event) = {
      e.stopPropagation()
      e.preventDefault()
      e.target.className = if(e.`type` == "dragover") "hover" else ""
    }

    def fileSelectHandler(e: dom.Event) = {
      fileDragHover(e)
      val files = if(e.target.files.toString != "undefined") {
        e.target.files
      } else {
        e.asInstanceOf[dom.DragEvent].dataTransfer.files
//        e.dataTransfer.files
      }
//      dom.alert(files.toString)
//      dom.alert((files != null).toString)
      (0 until files.length).foreach{ i =>
//        dom.alert(files(i).toString)
        try {
          parseFile(files(i))
          uploadFile(files(i))
        }catch{
          case e:Throwable => println(e)
        }
      }
    }

    def parseFile(file: dom.File) = {
      output(
        s"""
          |<p>File information: <strong>${file.name}</strong>
          | type: <strong>${file.`type`}</strong>
          | size: <strong>${file.size}</strong> bytes</p>
        """.stripMargin)
      val reader = new FileReader()
      if(file.`type`.indexOf("image") == 0) {
        reader.onload = (e: dom.Event) => {
          output(
            s"""
              |<p><strong>${file.name}:</strong><br />
              |<img src=""/>${e.target.result}</p>
            """.stripMargin)
        }
        reader.readAsDataURL(file)
      }else if(file.`type`.indexOf("text") == 0){
        reader.onload = (e: dom.Event) => {
          output(
            s"""
              |<p><strong>${file.name}:</strong></p>
              |<pre>${e.target.result}</pre>
            """.stripMargin)
          reader.readAsText(file)
        }
      }
    }

    def uploadFile(file: dom.File) = {
      val xhr = new dom.XMLHttpRequest
      if(xhr.upload != null && file.size <= maxFileSize){
        val o = $id("progress")
        val progress = o.appendChild(dom.document.createElement("p")).asInstanceOf[dom.HTMLElement]
        progress.appendChild(dom.document.createTextNode(s"upload ${file.name}"))

        xhr.upload.addEventListener("progress", (e: dom.Event) => {
          val pc = 100 - (e.loaded / e.total * 100)
          progress.style.backgroundPosition = s"$pc % 0"
        }, false)

        xhr.onreadystatechange = (e: dom.Event) => {
          if(xhr.readyState == 4){
            progress.className = if(xhr.status == 200) "success" else "failure"
          }
        }
        //start upload
        xhr.open("POST", $id("upload").asInstanceOf[dom.HTMLFormElement].action, true)
        xhr.setRequestHeader("X-Request-With", "XMLHttpRequest")
        xhr.setRequestHeader("X-FILENAME", file.name)
        xhr.send(file)
      }
    }

    def init = {

      $("#fileDrag").on("dragenter dragstart dragend dragleave dragover drag drop", (e: dom.Event) => {
        e.preventDefault();
      });

      $id("fileSelect").addEventListener("change", fileSelectHandler _, false)
      $id("fileSelect").ondragend
      val xhr = new dom.XMLHttpRequest
      if(xhr.upload != null){
        val fileDrag = $id("fileDrag")
        fileDrag.addEventListener("dragover", fileDragHover _, false)
        fileDrag.addEventListener("dragleave", fileDragHover _, false)
        fileDrag.addEventListener("drop", fileSelectHandler _, false)
        fileDrag.style.display = "block"

        $id("submitButton").style.display = "none"
      }
    }

    init
  }

  @JSExport
  def main(csrfToken: String) = {
    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(markup(csrfToken).render)
    scripts
  }
}

class FileReader() extends dom.EventTarget {

  var onload: js.Function1[dom.Event, _] = ???

  def readAsDataURL(blob: dom.Blob): Unit = ???
  def readAsText(blob: dom.Blob): Unit = ???

}

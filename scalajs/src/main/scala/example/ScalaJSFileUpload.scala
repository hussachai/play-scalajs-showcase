package example

import org.scalajs.dom
import org.scalajs.dom.{XMLHttpRequest, FileList, FileReader}
import shared.Csrf
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import js.Dynamic.{global => g}
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$,_}

@JSExport
object ScalaJSFileUpload {

  def markup(csrfToken: String) = div(
    h2(style:="text-align: center")(a(href:="http://filedropjs.org", "FileDrop"), " basic sample"),
    fieldset(id:="zone")(
      legend("Drop a file inside ..."),
      p("Or click here to ", em("Browse"), "..."),
      p(style:="z-index:10;position:relative")(
        input(`type`:="checkbox", id:="mulitple"),
        label(`for`:="multiple")("Allow multiple selection")
      ),
      input(id:="hello", value:="Hello World")
    )
  )

  trait ElementExt extends dom.Element {
    val checked: String = ???
  }
  trait FileExt extends dom.File {
    def event(name: String, fn: js.Function1[XMLHttpRequest, _]) = ???
    def sendTo(url: String)
  }
  implicit def monkeyizeFile(e: dom.File): FileExt = e.asInstanceOf[FileExt]
  implicit def monkeyizeElement(e: dom.Element): ElementExt = e.asInstanceOf[ElementExt]

  def scripts = {
    val options = js.Dynamic.literal("iframe"->js.Dynamic.literal("url"->"/upload"))
    val zone = js.Dynamic.newInstance(js.Dynamic.global.FileDrop)("zone", options)
    zone.event("send", (files: FileList) => {
      (0 until files.length).foreach{ i =>
        val file = files(i)
        dom.alert(file.name)
//        file.event("done", (xhr: XMLHttpRequest) => {
//          dom.alert(s"Done uploading ${file.name}, response:\n\n ${xhr.responseText}")
//        })
        file.sendTo("/upload")
      }
    })
    // React on successful iframe fallback upload (this is separate mechanism
    // from proper AJAX upload hence another handler):
    zone.event("iframeDone", (xhr: XMLHttpRequest) => {
      dom.alert(s"Done uploading via <iframe>, response: \n\n ${xhr.responseText}")
    })
    // A bit of sugar - toggling multiple selection:
    g.fd.addEvent(g.fd.byID("multiple"), "change", (e: dom.Event) =>{
      val m = ((e.currentTarget.toString != "undefined") || e.srcElement.checked == "checked")
      zone.multiple(m)
    })
  }

  @JSExport
  def main(csrfToken: String) = {
    dom.document.body.innerHTML = ""
    script(src:="")
    dom.document.body.appendChild(markup(csrfToken).render)
    scripts
  }
}




































package common

import upickle._

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.extensions.Ajax
import shared._

class ExtAjax(ajax: Ajax.type) {

  def delete(url: String,
          data: String = "",
          timeout: Int = 0,
          headers: Seq[(String, String)] = Nil,
          withCredentials: Boolean = false) = {
    ajax.apply("DELETE", url, data, timeout, headers, withCredentials)
  }

  def postAsForm(url: String,
           data: String = "",
           timeout: Int = 0,
           headers: Seq[(String, String)] = Nil,
           withCredentials: Boolean = false)(implicit csrf: Csrf) = {
    val contentType = Seq("Content-Type"->"application/x-www-form-urlencoded")
    val newHeaders = if(headers == Nil) contentType else contentType ++ headers
    val newData = (if(data != "") data + "&") + s"csrfToken=${csrf}"
    ajax.apply("POST", url, newData, timeout, newHeaders, withCredentials)
  }

  def postAsJson(url: String,
                 data: String = "",
                 timeout: Int = 0,
                 headers: Seq[(String, String)] = Nil,
                 withCredentials: Boolean = false)(implicit csrf: Csrf) = {
    val contentType = Seq("Content-Type"->"application/json")
    val newHeaders = if(headers == Nil) contentType else contentType ++ headers
    val newUrl = url + (if(url.contains("?")) "&" else "?") + s"csrfToken=${csrf}"
    ajax.apply("POST", newUrl, data, timeout, newHeaders, withCredentials)
  }
}

class ExtXMLHttpRequest(req: XMLHttpRequest) {

  def responseAs[T](implicit readWrite: ReadWriter[T]): T = read[T](req.responseText)

  def ok = req.status == 200

}

object ExtAjax {

  implicit def wrapperForAjax(ajax: Ajax.type) = new ExtAjax(ajax)

  implicit def wrapperForXMLHttpRequest(req: XMLHttpRequest) = new ExtXMLHttpRequest(req)

}
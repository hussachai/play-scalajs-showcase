package common

import upickle._

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.extensions.Ajax
import shared._

class ExtAjax(ajax: Ajax.type) {

  def put(url: String,
          data: String = "",
          timeout: Int = 0,
          headers: Seq[(String, String)] = Nil,
          withCredentials: Boolean = false) = {
    apply("PUT", url, data, timeout, headers, withCredentials)
  }

  def delete(url: String,
          data: String = "",
          timeout: Int = 0,
          headers: Seq[(String, String)] = Nil,
          withCredentials: Boolean = false) = {
    apply("DELETE", url, data, timeout, headers, withCredentials)
  }

  def postAsForm(url: String,
           data: String = "",
           timeout: Int = 0,
           headers: Seq[(String, String)] = Nil,
           withCredentials: Boolean = false)(implicit csrf: Csrf) = {
    val contentType = Seq("Content-Type"->"application/x-www-form-urlencoded")
    val newHeaders = if(headers == Nil) contentType else contentType ++ headers
    val newData = (if(data != "") data + "&") + s"csrfToken=${csrf}"
    apply("POST", url, newData, timeout, newHeaders, withCredentials)
  }

  def postAsJson(url: String,
                 data: String = "",
                 timeout: Int = 0,
                 headers: Seq[(String, String)] = Nil,
                 withCredentials: Boolean = false)(implicit csrf: Csrf) = {
    val contentType = Seq("Content-Type"->"application/json")
    val newHeaders = if(headers == Nil) contentType else contentType ++ headers
    val newUrl = url + (if(url.contains("?")) "&" else "?") + s"csrfToken=${csrf}"
    apply("POST", newUrl, data, timeout, newHeaders, withCredentials)
  }

  def apply(method: String,
            url: String,
            data: String = "",
            timeout: Int = 0,
            headers: Seq[(String, String)] = Nil,
            withCredentials: Boolean = false) = {
    val ajaxReq = Seq("X-Requested-With"->"XMLHttpRequest")
    val newHeaders = if(headers == Nil) ajaxReq else headers ++ ajaxReq
    ajax.apply(method, url, data, timeout, newHeaders, withCredentials)
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
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
           withCredentials: Boolean = false) = {
    val contentType = Seq("Content-Type"->"application/x-www-form-urlencoded")
    apply("POST", url, data, timeout, headers ++ contentType, withCredentials)
  }

  def postAsJson(url: String,
                 data: String = "",
                 timeout: Int = 0,
                 headers: Seq[(String, String)] = Nil,
                 withCredentials: Boolean = false) = {
    val contentType = Seq("Content-Type"->"application/json")
    apply("POST", url, data, timeout, headers ++ contentType, withCredentials)
  }

  def apply(method: String,
            url: String,
            data: String = "",
            timeout: Int = 0,
            headers: Seq[(String, String)] = Nil,
            withCredentials: Boolean = false) = {
    val ajaxReq = Seq("X-Requested-With"->"XMLHttpRequest")
    ajax.apply(method, url, data, timeout, headers ++ ajaxReq, withCredentials)
  }

}

class ExtXMLHttpRequest(req: XMLHttpRequest) {

//  def responseAs[T](implicit readWrite: ReadWriter[T]): T = read[T](req.responseText)

  def ok = req.status == 200

}

object ExtAjax {

  implicit def wrapperForAjax(ajax: Ajax.type) = new ExtAjax(ajax)

  implicit def wrapperForXMLHttpRequest(req: XMLHttpRequest) = new ExtXMLHttpRequest(req)

}
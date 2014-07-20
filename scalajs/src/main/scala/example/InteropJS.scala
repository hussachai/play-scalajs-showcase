package example

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import scalatags.JsDom._
import all._
import shared.foo._

@JSExport
object ScalaJSInterop {

  def template = div{
    ul(
      li(a("hello"))
    )
  }

  def runScript = {
//    val foo = new Foo
//    foo.test //Doesn't work because there's no method named test in javascript Foo class
//
//    new Alert("Hello this is native javascript alert")

    val bob = new Person("Bob")
//    dom.alert(bob.firstName)
//    bob.walk()
//    bob.sayHello()
    val student = new Student("Alice", "Biology")
//    student.sayHello()
//    student.sayGoodBye()
//    student.notExistInJs() //Throws error "undefined is not a function"
  }


  @JSExport
  def main(foo: js.Dynamic) = {
//    foo.listInt.asInstanceOf[js.Array[Int]](2).toString
    dom.alert(JSON.stringify(foo))
    dom.document.body.appendChild(template.render)
    runScript
  }

}


/**
 * This represents Foo class in javascript
 * You must not declare it as nested class
 */
class Foo extends js.Object {
  def test = {
    dom.alert("Test add-hoc method")
  }
}

class Alert(msg: String) extends js.Object {}

class Person(val firstName: String) extends js.Object{
  /**
   * The method must have () and Unit as return type in case it returns nothing
   * If you leave out the return type, scala will put Nothing as the return type
   * and Scala.js doesn't work with Nothing type yet.
   */
  def walk():Unit = ???

  def sayHello():Unit = ???
}

class Student(firstName: String, val subject: String) extends Person(firstName) {
  def sayGoodBye():Unit = ???

  def notExistInJs():Unit = {
    dom.alert("You will not see this")
  }
}
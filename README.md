# Play Framework with Scala.js Showcase

Have you ever dream to code everything in Scala?   
I've never dreamed about it because I didn't think it's possible :D   
But, I should know that nothing is impossible and [Scala.js project](http://scala-js.org) already proves that.

Scala.js can compile Scala to javascript (Super cool. I'm the one who hate javascript despite the fact
that it's now everywhere and a lot of cool projects are written in javascript. I think it's similar to 
iOS applications that are written in Objective-C. Everything is great except the language.)   
You don't have to worry about Type, Scope, Typos(It's a big problem for me in Dynamic Typed Language) anymore, 
let the compiler takes care of it. You can use Scala's collection API in Scala.js. Yeah you read it right.
And many more other APIs that you are already faimiliar in Scala, you can use them in Scala.js. 

You may think that Scala.js can just convert Scala to Javascript. What about HTML?
HTML is a subset of XML and XML can be parsed to DOM. It means that you can create HTML
element on the fly using DOM API. There is interesting project that offers you an intuitive
API to build HTML element on the fly. Check this out [scala-tags](https://github.com/lihaoyi/scalatags).
You can create type-safety HTML. It's funny, isn't it?

If you are a big fan of reactive, I can imply that you love Future.
You are a good planer and Monad is your best friend. Am I right?
Unsurprisingly, you can use Future in Scala.js as well and it will be converted to setTimeout 
by compiler. You can add reactive programming paradigm into Scala.js by using 
[scala-rx](https://github.com/lihaoyi/scala.rx).

Last but not least, Scala.js allows you to share the object between Scala.js and Scala.   
Read the [tutorial](http://www.scala-js.org/doc/tutorial.html)

This project was inspired by [Todo application](http://lihaoyi.github.io/workbench-example-app/todo.html)

Right now the project contains 4 examples. 
- Todo application with backend persistence. (InMemory, Slick, Anorm)
- Hangman (Inspired from [Yii's demo](http://www.yiiframework.com/demos/hangman/))
- HTML5 Fileupload (Modified from [How to Use HTML5 File Drag and Drop](http://www.sitepoint.com/html5-file-drag-and-drop/))
- Server Push Chat. It supports both Websocket and Server-Sent Event

Live demo: http://play-scalajs.hussachai.cloudbees.net

This project uses the same project structure as [play-with-scalajs-example](https://github.com/vmunier/play-with-scalajs-example)

I hope you enjoy learning Play and Scala.js from this project
Your contribution is always welcome.

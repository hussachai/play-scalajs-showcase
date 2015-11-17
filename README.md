# Play Framework with Scala.js Showcase
###Scala: 2.11.6, Scala.js: 0.6.5, Play: 2.4.0, Slick: 3.0.0

This is a small application showing how cool Scala.js is. You can share the code regardless of a layer in an architecture.
Beside CSS and a few lines of HTML, almost all code in this project are type-safety including HTML (Thanks to [scalatags](https://github.com/lihaoyi/scalatags)). I made this project for learning purpose during my summer intern.
So, the code you will see in this project may be not a good practice but I hope you can learn something from it like I did.
I will try to keep it update to make sure that it will run with the recent version of Scala.js.

The sbt build file contains 3 modules
- `exampleServer` Play application (server side)
- `exampleClient` Scala.js application (client side)
- `exampleShared` Scala code that you want to share between the server and the client.    

This project would not exist if I didn't find this kick-ass example 
[play-with-scalajs-example](https://github.com/vmunier/play-with-scalajs-example).

The project contains 4 simple examples. 
- Todo application with backend persistence.(Modified from [Todo application](http://lihaoyi.github.io/workbench-example-app/todo.html)) 
- Hangman (Inspired by [Yii's demo](http://www.yiiframework.com/demos/hangman/))
- HTML5 Fileupload (Modified from [How to Use HTML5 File Drag and Drop](http://www.sitepoint.com/html5-file-drag-and-drop/))
- Server Push Chat. It supports both Websocket and Server-Sent Event

### Run the application
```
$ sbt
> run
$ open http://localhost:9000
```

## Features

The application uses the [sbt-play-scalajs](https://github.com/vmunier/sbt-play-scalajs) sbt plugin and the [play-scalajs-scripts](https://github.com/vmunier/play-scalajs-scripts) library.

- Run your application like a regular Play app
  - `compile` simply triggers the Scala.js compilation
  - `run` triggers the Scala.js fastOptJS command on page refresh
  - `~compile`, `~run`, continuous compilation is also available
  - `start`, `stage` and `dist` generate the optimised javascript
  - [`playscalajs.html.scripts`](https://github.com/vmunier/play-with-scalajs-example/blob/c5fa9ce35954278bea903823a7f0528b1d68b5db/server/app/views/main.scala.html#L14) selects the optimised javascript file when the application runs in prod mode (`start`, `stage`, `dist`).
- Source maps
  - Open your browser dev tool to set breakpoints or to see the guilty line of code when an exception is thrown
  - Source Maps is _disabled in production_ by default to prevent your users from seeing the source files. But it can easily be enabled in production too by setting `emitSourceMaps in fullOptJS := true` in the Scala.js projects.

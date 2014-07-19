import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

def f(i:Int) = Future{
   if(i==0) "OK"
   else throw new Exception("Not OK")
}

val rF:Future[Either[Exception, String]] = f(1).map{ r => Right(r)}.recover{case e:Exception => Left(e)}
val r = Await.result(rF, 1 seconds)
println(">>>>"+r)


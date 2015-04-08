package example.rest

import scala.util.{Success, Failure}
import scala.concurrent.duration._
import akka.actor.{ActorSystem, Actor}
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json._
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.util._
import spray.client.pipelining._
import spray.http._
import scala.concurrent.Future
import akka.util.Timeout
import scala.collection.mutable.ArrayBuffer
import akka.actor.Props
import akka.actor.ActorRef

sealed trait RequestManagerMsg
case class StartTest extends RequestManagerMsg

sealed trait RequestTaskMsg
case class StartTestTask extends RequestTaskMsg

class RequestManager(n: Integer) extends Actor {
  implicit val rqClass: Class[_] = classOf[RequestTask]
  var result = ArrayBuffer[String]()
  var count = 0
  def receive = {
    case StartTest => {
      for (i <- 1 to n) {
        val task = context.actorOf(Props(rqClass))
        task ! StartTestTask
      }
    }
    case rs:String => {
      count += 1
      result += rs
      if (count == n) {
        processResult
      }
    }
    case _ => println("Sth Wrong (T_T)")
  }
  def processResult = {
    println("Got "+count+ " Result Task Finished")
    for (i <- 0 to count-1)
      println(result(i))
  }
}

class RequestTask extends Actor {
  val parent = context.parent
  implicit val executionContext = context.dispatcher
  implicit val clientPipeline = sendReceive
  implicit val requestTimeout = Timeout(60 seconds)
  
  def receive = {
    case StartTestTask => {
      doTask
      context.stop(self)
    }
    case _ => println("RT: Sth Wrong (0_0)")
  }
  
  def doTask = {
    
    val getUrlList = Array("http://10.87.127.181:30047") 
    for(url <- getUrlList){        
      val response = clientPipeline {
        Get(url)
      }          	  
      response.onComplete{
    	case Success(res) => {
         //println(res)
         val dataStr = res.entity.data.asString
    	 //println(dataStr)
    	  parent ! dataStr
    	}
    	case _ => println("something happened")
      }        
    }
  }
}

object Main extends App {
  
  val system = ActorSystem("TEST")
  //implicit val executionContext = system.dispatcher
  val reqMgr = system.actorOf(Props(classOf[RequestManager], 3))
  reqMgr ! StartTest
  
}
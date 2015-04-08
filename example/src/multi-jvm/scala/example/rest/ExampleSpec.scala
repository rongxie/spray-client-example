package example.rest

import scala.concurrent.duration._
import akka.remote.testconductor.RoleName
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec, MultiNodeSpecCallbacks}
import akka.testkit.ImplicitSender
import org.scalatest.{WordSpecLike,Matchers,BeforeAndAfterAll}
import akka.actor.{Actor, Props, ActorSystem, ActorRef}
import scala.collection.mutable.ArrayBuffer
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import scala.util.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

import org.scalatest.matchers.ShouldMatchers


object ExampleSpecConfig extends MultiNodeConfig {
  // register the named roles (nodes) of the test
  val node1 = role("node1")
  
}

class ExampleSpecMultiJvmNode1 extends ExampleSpec

class ExampleSpec extends MultiNodeSpec(ExampleSpecConfig)
  with MultiNodeSpecCallbacks with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {
  
  import ExampleSpecConfig._
  override def initialParticipants = roles.size
  override def beforeAll() = multiNodeSpecBeforeAll()
  override def afterAll() = multiNodeSpecAfterAll()
            
  override protected def atStartup() {
   
  }

  "The transaction service " should {
     
    "send back wellcome msgs for all requested client that connect to its home url" in within (10 seconds) {
      
      val system = ActorSystem("TEST")
      val reqMgr: ActorRef = system.actorOf(Props(classOf[ExampleTestManager], 2))
      reqMgr ! StartTest
      expectNoMsg
      ExampleTestManager.cnt should be (2)
    }
  }

}

object ExampleTestManager {
  var cnt = 0
}

class ExampleTestManager(val n: Integer) extends RequestManager(n) with ShouldMatchers{
  import ExampleTestManager._
  override implicit val rqClass: Class[_] = classOf[ExampleTestTask]
  override def processResult = {
    cnt += 1
    println("Have "+cnt+ " Result Task Finished")
    tmp should be ("Wellcome to Transaction Api Service!")
  }
}

class ExampleTestTask extends RequestTask {
  override def doTask {
    val getUrlList = Array("http://10.87.127.181:30047") 
    for(url <- getUrlList){        
      val response = clientPipeline {
        Get(url)
      }          	  
      response.onComplete{
    	case Success(res) => {
         val dataStr = res.entity.data.asString
    	  parent ! dataStr
    	}
    	case _ => println("something happened")
      }        
    }
  }
}




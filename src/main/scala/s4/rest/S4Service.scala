package s4.rest

import java.io.FileOutputStream

import spray.http.HttpRequest
import spray.routing.directives.{LoggingMagnet, DebuggingDirectives}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.language.postfixOps

import akka.actor.Actor
import s4.domain._
import spray.http.MediaTypes.{ `text/html` }
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.json.DefaultJsonProtocol
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.authentication.BasicAuth
import spray.routing.authentication.UserPass
import spray.routing.authentication.UserPassAuthenticator
import spray.routing.authentication.UserPassAuthenticator
import spray.routing.directives.AuthMagnet.fromContextAuthenticator
//import spray.routing.directives.CompletionMagnet.fromObject
import spray.routing.directives.FieldDefMagnet.apply

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class S4ServiceActor extends Actor with S4Service with ProductionDB {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(s4Route)
}

object JsonImplicits extends DefaultJsonProtocol {
  implicit val impPost = jsonFormat4(Post)
}

// this trait defines our service behavior independently from the service actor
trait S4Service extends HttpService { this: DBConfig =>

  case class UserProfile(fullName: String)

  def getUserProfile(name: String): Option[UserProfile] = {

    if (name == "bob")
      Some(UserProfile(s"$name"))
    else
      None
  }

  object CustomUserPassAuthenticator extends UserPassAuthenticator[UserProfile] {
    def apply(userPass: Option[UserPass]) = Promise.successful(
      userPass match {
        case Some(UserPass(user, pass)) => {
          getUserProfile(user)
        }
        case _ => None
      }).future
  }

  // This one doesn't use the implicit LoggingContext but uses `println` for logging
  def printRequestMethod(req: HttpRequest): Unit = println(req)
  val logRequestPrintln = DebuggingDirectives.logRequest(LoggingMagnet(printRequestMethod))

  val jsonRoute = {
    import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
    import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
    import JsonImplicits._

    get {
      pathPrefix("api" / "v1") {
        path("posts") {
          complete {
            m.getPosts(User(1, "bob", 12))
          }
        } ~
          path("posts" / IntNumber) { id =>
            complete {
              m.postById(id)
            }
          }
      }
    } ~
    path("seed") {
      get {
        complete {
          m.seed()
          "Seeded!"
        }
      }
    }

  }

  val s4Route = jsonRoute

}

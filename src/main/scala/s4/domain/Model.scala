package s4.domain

//import scala.slick.driver.H2Driver
//import scala.slick.session.{ Database, Session }

import scala.slick.driver.JdbcProfile
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.Database

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Model(name: String, dal: DAL, db: Database) {
  // We only need the DB/session imports outside the DAL
  import dal._
  import dal.profile.simple._

  // Put an implicitSession in scope for database actions
  implicit val implicitSession = db.createSession

  def seed() {
    posts ++= (0L until 100L).map(n => Post(Option.empty, "Lorem ipsum dir solor amet", System.currentTimeMillis(), n))
  }

  def createDB = dal.create

  def dropDB = dal.drop
  
  def purgeDB = dal.purge

  def getPosts(implicit  user:User): List[Post] = {
    posts.take(5).list
  }

  def postById(id: Long): Option[Post] = {
    (for {
      p <- posts if p.id === id
    } yield p).list.headOption
  }

//   def getPersons(): List[Person] = {
//    val result = findAllPersons
//    println("Got persons: " + result)
//    result
//  }
//
//  def addPerson(person: Person): Person = {
//    val result = insert(person)
//    println("Inserted person: " + result)
//    result
//  }
}
package s4.domain

import java.sql.Date

case class Post(id: Option[Long] = None, text: String, createdAt: Long, likesCount: Long)
case class PostWithLikes(text: String, createdAt:Long, likesCount:Long, likedByMe: Boolean)
case class User(id: Long, fullName: String, createdAt:Long)

// It is an adaptation of the official Slick example set (Cake Pattern):
// https://github.com/slick/slick-examples (MultiDBCakeExample.scala)
// Understanding that example will help to understand this code.

trait PostsComponent { this: Profile =>
  import profile.simple._

  class Posts(tag: Tag) extends Table[Post](tag, "post") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def createdAt = column[Long]("created_at")
    def likesCount = column[Long]("likes_count")
    def * = (id.?, text, createdAt, likesCount) <> (Post.tupled, Post.unapply)
  }
  val posts = TableQuery[Posts]

  class Users(tag: Tag) extends Table[(Long, String, Date)](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fullName = column[String]("full_name")
    def createdAt = column[Date]("created_at")
    def * = (id, fullName, createdAt)
  }
  val users = TableQuery[Users]

  class Likes(tag: Tag) extends Table[(Long, Long, Date)](tag, "like") {
    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def createdAt = column[Date]("created_at")
    def * = (userId, postId, createdAt)

    def user = foreignKey("user_fk", userId, users)(_.id)
    def post = foreignKey("post_fk", postId, posts)(_.id)
  }
  val likes = TableQuery[Likes]

  //    // Query Execution
  //    //def findAllPersons(implicit session: Session): List[Person] = { findAll.list map { x => Person(fname = x._1, lname = x._2, id = x._3) } }
  //    // def insert(person: Person)(implicit session: Session): Person = {
  //    //  autoInc.insert(person.fname, person.lname)
  //    // }
}

//trait PersonComponent { this: Profile =>
//  import profile.simple._
//
//  class Persons(tag: Tag) extends Table[(String, String, Option[Long])](tag, "PERSON") {
//    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
//    def fname = column[String]("FNAME", O.NotNull)
//    def lname = column[String]("LNAME", O.NotNull)
//    def * = (fname, lname, id)
//    // def mapped = fname ~ lname ~ id <> ({ (f, l, i) => Person(f, l, i) }, { x: Person => Some((x.fname, x.lname, x.id)) })
//
//    // Query Definition
//    //val autoInc = fname ~ lname returning id into { case (c, i) => Person(c._1, c._2, i) }
//
//    //def findAll = for (x <- Persons) yield x
//
//    //def forInsert = fname ~ lname <>
//    //  ({ (f, l) => Person(f, l, None) }, { x: Person => Some((x.fname, x.lname)) })
//

//  }
//
//  val persons = TableQuery[Persons]
//
//  private val usersAutoInc = persons.map(u => (u.fname, u.lname)) returning persons.map(_.id) into {
//    case (_, id) => id
//  }
//
//  def insert(person: Person)(implicit session: Session): Person = {
//    // val picture = if (user.picture.id.isEmpty) { //if no picture id...
//    //   insert(user.picture) //...insert
//    //  } else user.picture //else return current picture
//    val id = usersAutoInc.insert(person.fname, person.lname)
//    person.copy(id = id)
//  }
//
//  def findAllPersons(implicit session: Session): List[Person] = { persons.list map { x => Person(fname = x._1, lname = x._2, id = x._3) } }
//}
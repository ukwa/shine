package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Account(email: String, name: String, password: String)

object Account {
  
  // -- Parsers
  
  /**
   * Parse a Account from a ResultSet
   */
  val simple = {
    get[String]("account.email") ~
    get[String]("account.name") ~
    get[String]("account.password") map {
      case email~name~password => Account(email, name, password)
    }
  }
  
  // -- Queries
  
  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Account where email = {email}").on(
        'email -> email
      ).as(Account.simple.singleOpt)
    }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Account").as(Account.simple *)
    }
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[Account] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from Account where 
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(Account.simple.singleOpt)
    }
  }
   
  /**
   * Create a User.
   */
  def create(account: Account): Account = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into Account values (
            {email}, {name}, {password}
          )
        """
      ).on(
        'email -> account.email,
        'name -> account.name,
        'password -> account.password
      ).executeUpdate()
      
      account
      
    }
  }
  
}

package controllers

import java.util
import java.util.Date
import java.sql.ResultSet
import _root_.slick.backend
import _root_.slick.driver.PostgresDriver
import play.api._
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc._
import play.api.db._
import play.api.libs.json._
import akka.actor._
import scala.collection.JavaConversions._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import java.sql.Statement

/*
This is a singleton that contains all the logged in users
*/
object UserRepository {
  //The Table of Logged In Users
  private val users: util.HashMap[String, AdminUser] = new util.HashMap[String, AdminUser]();
  //Last time we looked for expired cookies
  private var lastSweep = new Date();
  /*
  Check if the user is logged in
  @user The user we are checking
  @cookieVal the value of his login cookie
   */
  def isLoggedIn(userName: String, cookieVal: String): Boolean =
  {
    //Check if the user is logged in and if this Cookie is a valid one of his cookies
    val isLoggedIn = if (users.containsKey(userName)) {
      val theUser: AdminUser = users.get(userName);
      (theUser.checkCookie(cookieVal))
    } else false
    //If he is not logged in I don't mind hassling him a bit so now is a potential time to cleanup the cookies
    if(!isLoggedIn)
    {
      try{
        //don't care about a minor delay if not logged in so every 5 mintes cleat out unued cookies
        val newDate = new Date();
        //Check if we have done a sweep in the last 5 minutes
        if(newDate.getTime - lastSweep.getTime() > (5*1000*60))
        {
          lastSweep = newDate;
          // check each user for expired cookies
          users.foreach(kv => kv._2.cleanCookies);

        }
      }
      catch {case e: Exception =>
        println(e.getMessage)

      }
    }
    isLoggedIn
  };

  def getUserInfo(user: String): Option[AdminUser] = {
    //Check if the user is logged in and if this Cookie is a valid one of his cookies
    if (users.containsKey(user)) {
      val theUser: AdminUser = users.get(user);
      return Some(theUser);
    }
    return None;
  }
  /*
  Log in a user from a new browser
  @user the user to log in
  @password the password he is using to log in with
   */
  def logIn(user: String, password: String)(dbConfig: backend.DatabaseConfig[PostgresDriver]): String = {

    //Lets check the user is allowed to log in against the DB
    val sess = dbConfig.db.createSession()
    var pstmt: java.sql.PreparedStatement = null;
    var rs: java.sql.ResultSet = null;
    var newCookie: String = "Not Allowed";
    try {
      var query: String = "Select id,access_level,last_name,first_name,office_location_id from agent where user_name = ? AND password = ?";
      pstmt = sess.prepareStatement(query);
      pstmt.setString(1, user);
      pstmt.setString(2, password);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        //If the user has logged in previously update his info and set a new cookie
        if (users.containsKey(user)) {
          val oldUser: AdminUser = users.get(user);
          oldUser.password = password;
          oldUser.accessLevel = rs.getString("access_level");
          oldUser.fullName = rs.getString("first_name") + " " + rs.getString("last_name");
          oldUser.officeID = rs.getInt("office_location_id");
          oldUser.ID = rs.getInt("id");
          newCookie = oldUser.addCookie();
        }
        //Otherwise add his info to the repository
        else {
          val newUser: AdminUser = new AdminUser(user, password, rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("access_level"),rs.getInt("office_location_id"),rs.getInt("id"));
          users.put(user, newUser);
          newCookie = newUser.addCookie();
        }
      }
    }
    finally {
      sess.close()
      if (pstmt != null && !pstmt.isClosed()) {
        pstmt.close();
      }
      if (rs != null && !rs.isClosed()) {
        rs.close();
      }

    }
    return user + ":" + newCookie;
  }

}
/*
An Object to define a Cookie
@lastLogin when this cookie was last seen
@token the value of the cookie
 */
class cookieItem{
  var lastSeen: util.Date = new Date();
  var token:String = null;
}

/*
The class to define the admin user
@username
@password
@fullName
@accessLevel is not used yet but we will use to define which pages he can see
 */
case class AdminUser
(var userName: String,
 var password: String,
 var fullName: String,
 var accessLevel: String,
 var officeID:Int,
 var ID:Int) {
  var lastLogin: util.Date = new Date();
  //A Map of all the cookies this user has (ie from multiple browsers)
  val openCookies: util.HashMap[String,cookieItem] = new util.HashMap[String,cookieItem]();

  def getPassword(): String = {
    return password;
  }
  //Every time we see the user update we have seen him
  //I don't use this anymore as I update the cookie itself
  def touch(): Unit = {
    lastLogin = new util.Date();
  }
  /*
  Automatically generate a new Cookie Value and add it to the user
   */
  def addCookie(): String = {
    //generate Cookie
    touch();
    //use the time so it is random
    var newCookieVal: String = System.currentTimeMillis() + userName;
    newCookieVal = newCookieVal.replaceAll("[\\s;,=]","")//newCookieVal.replace(" ","");
    //TODO:need to encrypt it
    val newCookie:cookieItem = new cookieItem();
    openCookies.put(newCookieVal,newCookie);
    return newCookieVal;
  }
  /*
  Go through all the users cookies and clean out the expired ones
   */
  def cleanCookies = {
    val newDate: util.Date = new util.Date();
    openCookies.foreach(kv =>
      try{
        val myCookie = kv._2;
        if(newDate.getTime() - myCookie.lastSeen.getTime() > (30 * 60 * 1000))
        {
          openCookies.remove(myCookie.token);
        }
      }
      catch{
        case e:Exception =>
          println(e.getMessage);
      });
  }
  /*
  Check if the Cookie is related to this user
  @cookieVal the value of the cookie we are checking
   */
  def checkCookie(cookieVal: String): Boolean = {
    val newDate: util.Date = new util.Date();

    if (openCookies.containsKey(cookieVal)) {
      val myCookie:cookieItem = openCookies.get(cookieVal);
      //If we have it but it's expired remove it and declare them not logged in
      if(newDate.getTime() - myCookie.lastSeen.getTime() > (30 * 60 * 1000))
      {
        openCookies.remove(cookieVal);
        println("removing cookie "+cookieVal);
        return false;
      }
      else
      {
        //update the Cookies so it won't expire
        myCookie.lastSeen = new util.Date();
      }
      return true;
    }
    return false;
  }
}
object AdminUser {
  implicit val format = Json.format[AdminUser]
}

package controllers

import java.util
import java.util.Date
import java.sql.ResultSet
import play.api.Play.current
import play.api._
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc._
import play.api.db._
import play.api.libs.json._
import akka.actor._
import scala.collection.JavaConversions._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future


object UserRepository {
  var users: util.HashMap[String, AdminUser] = new util.HashMap[String, AdminUser]();
  var lastSweep = new Date();
  def isLoggedIn(user: String, cookieVal: String): Boolean = {
    println("Checking Logged In"+user+"  "+cookieVal);
    if (users.containsKey(user)) {
      var theUser: AdminUser = users.get(user);
      println("Has User")
      if (theUser.checkCookie(cookieVal)) {
        return true;
      }
    }
    try{
      //don't care about a minor delay if not logged in so every 5 mintes cleat out unued cookies
      var newDate = new Date();
      if(newDate.getTime - lastSweep.getTime() > (5*1000*60))
        {
          lastSweep = newDate;
        //  for(u:AdminUser <- users)
       //   {
       //     u.cleanCookies;
       //   }
          users.foreach(kv => kv._2.cleanCookies);

        }
    }
    catch{case e: Exception =>
      println(e.getMessage)

    }
    return false;
  };

  def logIn(user: String, password: String): String = {

    //either the password was changed or the user is not yet in memory
    //Lets get it from the db
    var conn: java.sql.Connection = DB.getConnection();
    var pstmt: java.sql.PreparedStatement = null;
    var rs: java.sql.ResultSet = null;
    var newCookie: String = "Not Allowed";
    try {
      var query: String = "Select access_level,last_name,first_name from agent where user_name = ? AND password = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setString(1, user);
      pstmt.setString(2, password);
      rs = pstmt.executeQuery();
      if (rs.next()) {
      //  println("Yeah have something");
        if (users.containsKey(user)) {
          var oldUser: AdminUser = users.get(user);
          oldUser.password = password;
          oldUser.accessLevel = rs.getString("access_level");
          oldUser.fullName = rs.getString("first_name") + " " + rs.getString("last_name");
          newCookie = oldUser.addCookie();
        }
        else {
          var newUser: AdminUser = new AdminUser(user, password, rs.getString("first_name") + " " + rs.getString("last_name"), rs.getString("access_level"));
          users.put(user, newUser);
          newCookie = newUser.addCookie();
        }
      }
    }
    finally {
      if (!conn.isClosed()) {
        conn.close();
      }
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

class cookieItem{
  var lastLogin: util.Date = new Date();
  var token:String = null;
}

class AdminUser
(var userName: String,
 var password: String,
 var fullName: String,
 var accessLevel: String) {
  var lastLogin: util.Date = new Date();
  //if expired then forward to the login page
  var openCookies: util.HashMap[String,cookieItem] = new util.HashMap[String,cookieItem]();

  //etc.

  def getPassword(): String = {
    return password;
  }

  def touch(): Unit = {
    lastLogin = new util.Date();
  }

  def addCookie(): String = {
    //generate Cookie
    touch();
    var newCookieVal: String = (new util.Date()).toGMTString() + userName;
    newCookieVal = newCookieVal.replace(" ","");
    //TODO:need to encrypt it
    val newCookie:cookieItem = new cookieItem();
    openCookies.put(newCookieVal,newCookie);
    return newCookieVal;
  }

  def cleanCookies = {
    var newDate: util.Date = new util.Date();
   // for(myCookie:cookieItem <- openCookies)
   //   {
    openCookies.foreach(kv =>
        try{
          var myCookie = kv._2;
          if(newDate.getTime() - myCookie.lastLogin.getTime() > (30 * 60 * 1000))
          {
            openCookies.remove(myCookie.token);
            println("removing cookie "+myCookie.token);
          }
        }
        catch{
          case e:Exception =>
            println(e.getMessage);
        });
     // }
  }
  def checkCookie(cookieVal: String): Boolean = {
    var newDate: util.Date = new util.Date();

    if (openCookies.containsKey(cookieVal)) {
      val myCookie:cookieItem = openCookies.get(cookieVal);
      if(newDate.getTime() - myCookie.lastLogin.getTime() > (30 * 60 * 1000))
      {
        openCookies.remove(cookieVal);
        println("removing cookie "+cookieVal);
        return false;
      }
      else
      {
        myCookie.lastLogin = new util.Date();
      }
      return true;
    }
    return false;
  }
}

object LoggedInAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    try {
      var cookieVal:String = "";
      if(request.cookies("userCookie") != null) {
        cookieVal = request.cookies("userCookie").value;
      }
      var tokens: Array[String] = cookieVal.split(":", 2);
      println("Pre Check: "+tokens(0)+" : "+tokens(1));
      if (UserRepository.isLoggedIn(tokens(0), tokens(1))) {
        block(request);
      }
      else {
        if (request.accepts("application/json"))
        {
          Future.successful(Results.Ok("{\"action\":\"redirect\",\"location\":\"http://localhost:9000/simpleLogin.html?block=notAllowed\"}"));
        }
        else
        {
          Future.successful(Results.Redirect("http://localhost:9000/simpleLogin.html?block=notAllowed&url="+java.net.URLEncoder.encode(request.uri,"utf-8")));
        }
      }
    }
    catch {
      case e: Exception =>
        //if can't get cookie go to login
        println("MY ERROR:  "+e.getMessage()+" "+request.contentType+" "+(request.contentType+"").contains("json"));
        if(request.accepts("application/json"))
        {
          Future.successful(Results.Ok("{\"action\":\"redirect\",\"location\":\"http://localhost:9000/simpleLogin.html?block=notAllowed\"}"));
        }
        else
        {
          Future.successful(Results.Redirect("http://localhost:9000/simpleLogin.html?block=notAllowed&url="+java.net.URLEncoder.encode(request.uri,"utf-8")));
        }
    }
  }
}


class Application extends Controller {
  var vTicketBase:String = "SELECT t.id AS \"TICKET_ID\",    t.version AS \"VERSION\",    ta.name AS \"ACTION\",    t.description AS \"DESCRIPTION\",    s.status AS \"STATUS\",    t.account_id AS \"ACCOUNT_ID\",    u.first_name AS \"FIRST_NAME\",    u.last_name AS \"LAST_NAME\",    de.device_type AS \"DEVICE\",    cg.user_name AS \"CREATED_BY\",    ag.user_name AS \"ASSIGNED_TO\",    t.notes AS \"NOTES\",    l.location_name AS \"LOCATION\",    ta.description AS \"ACTION_DESCRIPTION\",    t.person_id AS \"PERSON_ID\",    t.person_device_id AS \"PERSON_DEVICE_ID\",    t.action_id AS \"ACTION_ID\",    t.status_id AS \"STATUS_ID\",    t.office_location_id AS \"OFFICE_LOCATION_ID\",    t.created_agent_id AS \"CREATED_AGENT_ID\",    t.assigned_agent_id AS \"ASSIGNED_AGENT_ID\",    t.\"time\" AS \"TIME\",    t.priority AS \"PRIORITY\"   FROM ticket t,    account a,    person u,    person_device d,    device de,    ticket_action ta,    ticket_status s,    office_location l,    agent ag,    agent cg  WHERE t.account_id = a.id AND t.person_id = u.id AND t.person_device_id = d.id AND d.device_id = de.id AND t.action_id = ta.id AND t.status_id = s.id AND t.office_location_id = l.id AND t.created_agent_id = cg.id AND t.assigned_agent_id = ag.id";
  def index = Action {
    Ok
  }

  def test1 = Action {
    Ok("hello world");
  };

  def test2 = LoggedInAction {
    Ok("hello world");
  };

  def loginFunction = Action {
    request =>
      request.body.asJson.map { json =>
        val user = (json \ "username").as[String];
        val pwd = (json \ "password").as[String];
        println(user + "  "+pwd);
        val newCookie = UserRepository.logIn(user, pwd);
        println(newCookie);
        if (newCookie.contains("Not Allowed")) {
          Ok(Json.obj(
            "success" -> false,
            "error" -> "Not Allowed"

          ));
        }
        else {
          Ok(Json.obj(
            "success" -> true,
            "error" -> "",
            "redirect" -> "http://localhost:9000/test2"
          )).withCookies(Cookie("userCookie", newCookie));
        }
      }.getOrElse {
        Ok(Json.obj(
          "success" -> false,
          "error" -> "Invalid Input"

        ));
      }
  }

  def queryToJson(query: String, rsToJsRow: ResultSet => JsValue): JsArray = {
    var jsonBuffer = ArrayBuffer.empty[JsValue]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(query)
      while (rs.next()) {
        jsonBuffer += rsToJsRow(rs)
      }
      rs.close()
      stmt.close()
    }
    finally {
      conn.close()
    }
    JsArray(jsonBuffer)
  }

  def queryToJson(query: java.sql.PreparedStatement,conn: java.sql.Connection, rsToJsRow: ResultSet => JsValue): JsArray = {
    var jsonBuffer = ArrayBuffer.empty[JsValue];
    try {
      val rs = query.executeQuery();
      while (rs.next()) {
        jsonBuffer += rsToJsRow(rs)
      }
      if(!rs.isClosed()) {
        rs.close();
      }
    }
    finally {

    }
    JsArray(jsonBuffer)
  }


  def listAgents = LoggedInAction {
    val query = "SELECT id, first_name, last_name, user_name from AGENT"
    val json = queryToJson(query, (rs: ResultSet) =>
      Json.obj(
        "id" -> rs.getInt("ID"),
        "firstName" -> rs.getString("first_name"),
        "lastName" -> rs.getString("last_name"),
        "userName" -> rs.getString("user_name")
      )
    )
    Ok(json)
  }


  //TODO we want to take some parameters for this call
  // - Office location
  // - Agent ID for tickets assigned to that agent
  // - can these be
  def listTickets = LoggedInAction {

    val query = "SELECT * from V_TICKET"
    val json = queryToJson(query, (rs: ResultSet)=>
      Json.obj(
        "ticket_id" -> rs.getInt("TICKET_ID"),
        "version" -> rs.getInt("VERSION"),
        "Action" -> rs.getString("ACTION"),
        "Description" -> rs.getString("DESCRIPTION"),
        "Status"  -> rs.getString("STATUS"),
        "Account_id" -> rs.getInt("ACCOUNT_ID"),
        "Person" -> rs.getInt("PERSON_ID"),
        "First Name" -> rs.getString("FIRST_NAME"),
        "Last Name" -> rs.getString("LAST_NAME"),
        "Device_id" -> rs.getInt("PERSON_DEVICE_ID"),
        "Device" -> rs.getString("DEVICE"),
        "Office_id" -> rs.getInt("OFFICE_LOCATION_ID"),
        "Office" -> rs.getString("LOCATION"),
        "Created By" -> rs.getString("CREATED_BY"),
        "Assigned To" -> rs.getString("ASSIGNED_TO"),
        "Notes" -> rs.getString("NOTES"),
        "Time" -> rs.getTimestamp("TIME"),
        "Priority" -> rs.getInt("PRIORITY")
      )
    )
    Ok(json)

  }

  def getTicketById(tickID:Int) : JsArray = {
    val conn:java.sql.Connection = DB.getConnection();
    var query = vTicketBase+" and t.id = ?";
    var pstmt: java.sql.PreparedStatement = conn.prepareStatement(query);
    pstmt.setInt(1,tickID);
    val json = queryToJson(pstmt, conn, (rs: ResultSet) =>
      Json.obj(
        "ticket_id" -> rs.getInt("TICKET_ID"),
        "version" -> rs.getInt("VERSION"),
        "Action" -> rs.getString("ACTION"),
        "Description" -> rs.getString("DESCRIPTION"),
        "Status"  -> rs.getString("STATUS"),
        "Account_id" -> rs.getInt("ACCOUNT_ID"),
        "Person" -> rs.getInt("PERSON_ID"),
        "First Name" -> rs.getString("FIRST_NAME"),
        "Last Name" -> rs.getString("LAST_NAME"),
        "Device_id" -> rs.getInt("PERSON_DEVICE_ID"),
        "Device" -> rs.getString("DEVICE"),
        "Office_id" -> rs.getInt("OFFICE_LOCATION_ID"),
        "Office" -> rs.getString("LOCATION"),
        "Created By" -> rs.getString("CREATED_BY"),
        "Assigned To" -> rs.getString("ASSIGNED_TO"),
        "Notes" -> rs.getString("NOTES"),
        "Time" -> rs.getTimestamp("TIME"),
        "Priority" -> rs.getInt("PRIORITY")
      )
    )
    dbCleanup(pstmt,conn);
    return json;
  }

  def dbCleanup(pstmt:java.sql.PreparedStatement,conn:java.sql.Connection) =
  {
    if(pstmt != null && !pstmt.isClosed)
    {
      pstmt.close()
    }
    if(conn != null && !conn.isClosed)
    {
      conn.close()
    }
  }

  def updateExtras(json:JsArray,logInfo:String,tickID:Int) = {
    //Do some logging
    wsList.foreach(ws => ws.receive(
      Json.obj(
        "sender"->"server",
        "Action"->"update",
        "message" -> json
      )
    ));
  }

  def setUpSQL(conn:java.sql.Connection,newField:String,newValue:String,tickID:Int) : java.sql.PreparedStatement= {
    var pstmt: java.sql.PreparedStatement = null;
    if(newField == "notes")
    {
      var query:String = "update ticket set notes = ?, time = time where id = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setString(1,newValue);
      pstmt.setInt(2,tickID);
    }

    if(newField == "assignment")
    {
      var query:String = "update ticket set assigned_agent_id = ?, time = time where id = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setInt(1,newValue.toInt);
      pstmt.setInt(2,tickID);
     // println(query);
    }

    if(newField == "status")
    {
      var query:String = "update ticket set status_id = ?, time = time where id = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setInt(1,newValue.toInt);
      pstmt.setInt(2,tickID);
    }
    return pstmt;
  }
  def updateTickInfo = LoggedInAction(parse.json) { request =>
    val json = request.body;
    val tickID = (json \ "tickID").as[Int];
    val newInfo = (json \ "newInfo").as[String];
    val newField = (json \ "newField").as[String];
    var conn = DB.getConnection();
    var pstmt: java.sql.PreparedStatement= setUpSQL(conn,newField,newInfo,tickID);
    if(pstmt == null)
    {
      Ok(Json.obj(
        "result" -> "invalid info"
      ))
    }

    var updated:Int = 0;
    try{
      updated = pstmt.executeUpdate();
    }
    finally{
      dbCleanup(pstmt,conn);
    }
    if(updated > 0)
    {
      var json = getTicketById(tickID);
      updateExtras(json,"",tickID);
      Ok(Json.obj(
        "result" -> "updated"
      ))
    }
    dbCleanup(null,conn);
    Ok(Json.obj(
    "result" -> "error"
    ))
  }


  def listPersonsDevices = LoggedInAction(parse.json) { request =>
      val json = request.body;
        val deviceID = (json \ "id").as[Int];
    var conn = DB.getConnection();
    var query:String = "Select filter_user_name,filter_password,p.notes as notes,provider,url,f.notes as filterNotes,device_type,manufacturer,model,os,browser,d.notes as deviceNotes  from   person_device p inner join device d on p.device_id = d.id\n   inner join filter f on p.filter_id = f.id  WHERE person_id = ?";
    val outputJSON = try {
      var pstmt: java.sql.PreparedStatement = conn.prepareStatement(query);
      pstmt.setInt(1, deviceID);
      val ret = queryToJson(pstmt, conn, (rs: ResultSet) =>
        Json.obj(
          "filterUsername" -> rs.getString("filter_user_name"),
          "filterPassword" -> rs.getString("filter_password"),
          "notes" -> rs.getString("notes"),
          "filterProvider" -> rs.getString("provider"),
          "filterURL" -> rs.getString("url"),
          "filterNotes" -> rs.getString("filterNotes"),
          "deviceType" -> rs.getString("device_type"),
          "deviceManufacturer" -> rs.getString("manufacturer"),
          "deviceModel" -> rs.getString("model"),
          "os" -> rs.getString("os"),
          "browser" -> rs.getString("browser"),
          "deviceNotes" -> rs.getString("deviceNotes")
        )
      )
      if(!pstmt.isClosed())
      {
        pstmt.close();
      }
      ret
    }
    finally
    {
      if(!conn.isClosed())
      {
        conn.close();
      }
}
Ok(outputJSON)

}


  def getTicketTypes = Action {

    val query = "SELECT id, name, category, description, wiki_link, notes from TICKET_ACTION"
    val json = queryToJson(query, (rs: ResultSet) =>
      Json.obj(
        "id" -> rs.getInt("id"),
        "Name" -> rs.getString("name"),
        "Category" -> rs.getString("category"),
        "Description" -> rs.getString("description"),
        "Wiki" -> rs.getString("wiki_link"),
        "Notes" -> rs.getString("notes")
      )
    )
    Ok(json)

  }


  def getTicketStatusTypes = Action {
    val query = "SELECT id, status, description from TICKET_STATUS"
    val json = queryToJson(query, (rs: ResultSet) =>
      Json.obj(
        "id" -> rs.getInt("id"),
        "Status" -> rs.getString("status"),
        "Description" -> rs.getString("description")
      )
    )
    Ok(json)
  }

  def validateAgent() = TODO

  def addAgent() = TODO

  def findAccount = TODO

  def createTicket = TODO

  def getAccount = TODO

  def updateTicket = TODO

  def createAccount = TODO


   /*
	- account details
	- new person / update / remove person
	- new device / remove device
	- new filter / remove filter
     */
	 def updateAccount = TODO

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    MyWebSocketActor.props(out)
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  def clientAction(action:String,args:String) : JsValue = {
    if(action == "echo")
    {
      return Json.obj(
        "response" -> ("Message received :" + args+" "+wsList.size())
      );
    }
    return Json.obj(
      "response" -> "Not Found"
    );
  }

  var wsList = new util.ArrayList[Actor]();
  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: JsValue =>
        val sender = (msg \ "sender").as[String];
        if(sender == "client")
        {
          out ! clientAction((msg \ "action").as[String],(msg \ "args").as[String]);
        }
        if(sender == "server")
        {
          out ! msg;
        }
    }

    override def preStart = {
      wsList.add(this);
    }
    override def postStop() = {
      wsList.remove(this);
    }
  }

  def wsTester = Action{
    wsList.foreach(ws => ws.receive(
    Json.obj(
      "sender"->"server",
      "message" -> "Over Here"
    )
    ));
    Ok("done");
  }
}

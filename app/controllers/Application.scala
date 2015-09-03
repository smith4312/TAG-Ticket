package controllers

import java.util
import java.util.Date
import java.sql.{Timestamp, ResultSet, Statement}
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

/*
Create an action that authenticates the user before allowing login
 */
object LoggedInAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    try {
      var cookieVal:String = "";
      //Check the cookie is set
      if(request.cookies("userCookie") != null) {
        cookieVal = request.cookies("userCookie").value;
      }
      var tokens: Array[String] = cookieVal.split(":", 2);
      //check the user is logged in before allowing the rest of the action
      if (UserRepository.isLoggedIn(tokens(0), tokens(1))) {
        block(request);
      }
      //If not redirect to the Login
      else {
        //If this is expecting JSON it needs to tell the JS to redirect
        if (request.accepts("application/json"))
        {
          Future.successful(Results.Ok("{\"action\":\"redirect\",\"location\":\"http://localhost:9000/simpleLogin.html?block=notAllowed\"}"));
        }
        else
        {
          //redirect to login page and tell the login page where you were trying to go so it can redirect there after
          Future.successful(Results.Redirect("http://localhost:9000/simpleLogin.html?block=notAllowed&url="+java.net.URLEncoder.encode(request.uri,"utf-8")));
        }
      }
    }
    catch {
      case e: Exception => ;
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
  //An easy replacement where I need to mimic v_ticket view
  var vTicketBase:String =
    """SELECT t.id AS "TICKET_ID",
      |    t.version AS "VERSION",
      |    ta.name AS "ACTION",
      |    t.description AS "DESCRIPTION",
      |    s.status AS "STATUS",
      |    t.account_id AS "ACCOUNT_ID",
      |    u.first_name AS "FIRST_NAME",
      |    u.last_name AS "LAST_NAME",
      |    de.device_type AS "DEVICE",
      |    cg.user_name AS "CREATED_BY",
      |    ag.user_name AS "ASSIGNED_TO",
      |    t.notes AS "NOTES",
      |    l.location_name AS "LOCATION",
      |    ta.description AS "ACTION_DESCRIPTION",
      |    t.person_id AS "PERSON_ID",
      |    t.person_device_id AS "PERSON_DEVICE_ID",
      |    t.action_id AS "ACTION_ID",
      |    t.status_id AS "STATUS_ID",
      |    t.office_location_id AS "OFFICE_LOCATION_ID",
      |    t.created_agent_id AS "CREATED_AGENT_ID",
      |    t.assigned_agent_id AS "ASSIGNED_AGENT_ID",
      |    t."time" AS "TIME",
      |    t.priority AS "PRIORITY"
      |   FROM ticket t INNER JOIN
      |    account a ON (t.account_id = a.id) INNER JOIN
      |    person u ON (t.person_id = u.id) INNER JOIN
      |    ticket_action ta ON (t.action_id = ta.id) INNER JOIN
      |    ticket_status s ON (t.status_id = s.id) INNER JOIN
      |    office_location l ON (t.office_location_id = l.id ) Left JOIN
      |    agent ag ON (t.assigned_agent_id = ag.id) INNER JOIN
      |    agent cg ON (t.created_agent_id = cg.id) LEFT JOIN
      |    person_device d ON (t.person_device_id = d.id) LEFT JOIN
      |    device de ON (d.device_id = de.id) """.stripMargin;

  def index = Action {
    Ok
  }
//DO Login
  def loginFunction = Action {
    request =>
      request.body.asJson.map { json =>
        val user = (json \ "username").as[String];
        val pwd = (json \ "password").as[String];
        //Try to login and generate a new login cookie
        val newCookie = UserRepository.logIn(user, pwd);
        //If login failed it would generate this so respond accordingly
        if (newCookie.contains("Not Allowed")) {
          Ok(Json.obj(
            "success" -> false,
            "error" -> "Not Allowed"

          ));
        }
        else {
          //tell it by default to redirect to the ticket page the Page can decide to ignore this and redirect to another page if it knew where you were going first
          Ok(Json.obj(
            "success" -> true,
            "error" -> "",
            "redirect" -> "http://localhost:9000/tickets.html"
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
        try {
          jsonBuffer += rsToJsRow(rs)
        }
        catch{
          case e:Exception => println(e.getMessage)
        }
      }
      rs.close()
      stmt.close()
    }
    finally {
      conn.close()
    }
    JsArray(jsonBuffer)
  }

  /*
  Same as above but with Prepared Statements to prevent SQL Injection
  The connection is needed to generate the PreparedStatement so can't be closed here as it needs to be passed in
   */
  def queryToJson(query: java.sql.PreparedStatement,conn: java.sql.Connection, rsToJsRow: ResultSet => JsValue): JsArray = {
    var jsonBuffer = ArrayBuffer.empty[JsValue];
    try {
      val rs = query.executeQuery();
      while (rs.next()) {
        try {
          jsonBuffer += rsToJsRow(rs)
        }
        catch{
          case e:Exception => println(e.getMessage)
        }
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

  def listOffices = LoggedInAction {
    val query = "SELECT id,location_name,city,country,phone,address,hours FROM office_location"
    val json = queryToJson(query, (rs: ResultSet) =>
      Json.obj(
        "id" -> rs.getInt("id"),
        "name" -> rs.getString("location_name"),
        "city" -> rs.getString("city"),
        "country" -> rs.getString("country"),
        "phone" -> rs.getString("phone"),
        "address" -> rs.getString("address"),
        "hours" -> rs.getString("hours")
      )
    )
    Ok(json)
  }


  //TODO we want to take some parameters for this call
  // - Office location
  // - Agent ID for tickets assigned to that agent
  // - can these be
  def listTickets = LoggedInAction {

    val json = getTicketWithWhere("",null);
      Ok(json);


  }

  def getTicketWithWhere(whereClause:String,vals:Seq[Object]) : JsArray = {
    val conn:java.sql.Connection = DB.getConnection();
    var query = vTicketBase+whereClause;
    var pstmt: java.sql.PreparedStatement = conn.prepareStatement(query);
    if(vals != null)
    {
      for((v, i) <- vals.zipWithIndex) {
        pstmt.setObject(i+1,v);
      }
    }
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

    def getTicketById(tickID:Integer) : JsArray = {
      val params: Seq[AnyRef] = Seq(tickID: AnyRef)
      getTicketWithWhere(" WHERE t.id = ?",params);
  }

  //check and close if open DB relevant items
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

  /*
  This is a place to run the additional things that need to be done after a ticket is updated like logging the action and broadcasting the new ticket to all the websockets
  @json the ticket to broadcast to the websockets
  @logInfo the text that should be added to the log
  @tickID the ticket ID that the action applies to
   */
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

  /*
  Create the PreparedStatments for the different types of update types
   */
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
      if(newValue.toInt >= 0) {
        pstmt.setInt(1, newValue.toInt);
      }
      else
      {
        pstmt.setNull(1,java.sql.Types.NULL);
      }
      pstmt.setInt(2,tickID);
    }

    if(newField == "status")
    {
      var query:String = "update ticket set status_id = ?, time = time where id = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setInt(1,newValue.toInt);
      pstmt.setInt(2,tickID);
    }

    if(newField == "office")
    {
      var query:String = "update ticket set office_location = ?, time = time where id = ?";
      pstmt = conn.prepareStatement(query);
      pstmt.setInt(1,newValue.toInt);
      pstmt.setInt(2,tickID);
    }
    return pstmt;
  }
  /*
  Generic service for updating tickets

   */
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

  def getAdminInfo = LoggedInAction{request =>
    var user:Option[AdminUser] = None;
    //Check the cookie is set
    if(request.cookies("userCookie") != null) {
      var cookieVal = request.cookies("userCookie").value;
      var tokens: Array[String] = cookieVal.split(":", 2);
      user = UserRepository.getUserInfo(tokens(0));
    }
    user match {
      case Some(u) => Ok(Json.toJson(u))
      case None => Ok(Json.obj("error"->"Not Found"))
    }
  };

  def createTicket = LoggedInAction(parse.json){ request =>
    val json = request.body;
    val userID = (json \ "userID").as[Int];
    val description = (json \ "subject").as[String];
    val tickType = (json \ "type").as[Int];
    val office = (json \ "office").as[Int];
    val notes = (json \ "notes").as[String];
    val agent = (json \ "agent").as[Int];
    val assign = (json \ "assign").as[Int];
    val priority = (json \ "priority").as[Int];
    val account = (json \ "account").as[Int];
    var pstmt:java.sql.PreparedStatement = null;
    var success:Boolean = false;
    val query:String = "Insert Into ticket (person_id,description,action_id,office_location_id,notes,created_agent_id,assigned_agent_id,priority,account_id,version,status_id,time) VALUES (?,?,?,?,?,?,?,?,?,1,1,?);";
    var conn = DB.getConnection();
    try{
      pstmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
      pstmt.setInt(1,userID);
      pstmt.setString(2,description);
      pstmt.setInt(3,tickType);
      pstmt.setInt(4,office);
      pstmt.setString(5,notes);
      pstmt.setInt(6,agent);
      if(assign >= 0)
      {
        pstmt.setInt(7,assign);
      }
      else
      {
        pstmt.setNull(7,java.sql.Types.NULL);
      }
      pstmt.setInt(8,priority);
      pstmt.setInt(9,account);
      pstmt.setTimestamp(10,new java.sql.Timestamp(new Date().getTime))
      var result = pstmt.executeUpdate();
      var keys = pstmt.getGeneratedKeys();
      keys.next();
      var key = keys.getInt(1);
      if(result > 0)
      {
        success = true;
        var json = getTicketById(key);
        updateExtras(json,"created TickID: "+key,key);
      }
    }
    catch{
      case e:Exception => println(e.getMessage());
    }
    finally{
      dbCleanup(pstmt,conn);
    }
    if(success)
    {
      Ok(Json.obj(
        "result" -> "ticket created"
      ))
    }
    Ok(Json.obj(
      "result" -> "Error creating ticket"
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

  def getAllSortedUsers = LoggedInAction {

    val query = "SELECT * FROM PERSON ORDER BY last_name,first_name";
    val json = queryToJson(query, (rs: ResultSet) =>
      Json.obj(
        "id" -> rs.getInt("id"),
        "lastName" -> rs.getString("last_name"),
        "firstName" -> rs.getString("first_name"),
        "account" -> rs.getInt("account_id"),
        "email" -> rs.getString("email"),
        "phone" -> rs.getString("phone"),
        "mobile" -> rs.getString("mobile_phone"),
        "notes" -> rs.getString("notes")
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


  def getAccount = TODO

  def createAccount = TODO


   /*
	- account details
	- new person / update / remove person
	- new device / remove device
	- new filter / remove filter
     */
	 def updateAccount = TODO
/*
The code for setting up the websockets to broadcast the ticket changes
 */
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

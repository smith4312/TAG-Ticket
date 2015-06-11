package controllers

import play.api.Play.current
import play.api._
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc._
import play.api.db._
import play.twirl.api.Html
import play.api.libs.json._

import scala.collection.mutable.ArrayBuffer

class Application extends Controller {

  def index = Action {
    Ok
  }


  def listAgents = Action {
    var jsonBuffer = ArrayBuffer.empty[JsValue]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT id, first_name, last_name, user_name from AGENT")
      while (rs.next()) {
        val json: JsValue = Json.obj(
          "id" -> rs.getInt("ID"),
          "firstName" -> rs.getString("first_name"),
          "lastName" -> rs.getString("last_name"),
          "userName" -> rs.getString("user_name")
        )
        jsonBuffer += json
      }
    } finally {
      conn.close()
    }
    val j = JsArray(jsonBuffer)
    //val j = Json.arr(jsonBuffer.map(x => x: JsValueWrapper): _*)
    Ok(j)
  }


  def addAgent() = Action {


    Ok
  }


  def validateAgent() = Action {
    /*
        request=>(
    val userName = (result \ "user_name" \ name).asOpt[String]
    val passWord = (result \ "password" \\ "emails").map(_.as[String])

    )
    val conn = DB.getConnection()
    try{
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("validateAgent " + user_name + "," + password)
    }finally{
      conn.close()
    }
*/

    Ok
  }


  //TODO we want to take some parameters for this call
  // - Office location
  // - Agent ID for tickets assigned to that agent
  // - can these be
  def listTickets = Action {
    var jsonBuffer = ArrayBuffer.empty[JsValue]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * from V_TICKET")
      while (rs.next()) {
        val json: JsValue = Json.obj(
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
          "Notes" -> rs.getString("NOTES")
        )
        jsonBuffer += json
      }
    } finally {
      conn.close()
    }
    val j = JsArray(jsonBuffer)

    Ok(j)

  }


}


//def is function action is request response function,ok means 200

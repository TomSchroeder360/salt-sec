package services

import io.circe.parser.*
import io.circe.{HCursor, Json}
import models.types.ParamType
import models.{PayloadModel, PayloadParam}
import utils.DateUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PayloadVerifierController {

  def verifyArr(jsonOpt: Option[Json], templates: Seq[PayloadParam], part: String): Seq[String] = {
    jsonOpt.fold(List(s"missing $part"))(json => json.asArray match {
        case Some(x) if x.isEmpty && templates.exists(_.isRequired) =>
          // TODO - add expected field names.
          List(s"missing fields in $part, expected (...)")
        case Some(x) if x.isEmpty => List.empty
        case Some(x) if x.nonEmpty && templates.isEmpty =>
          // TODO add field names.
          List(s"unknown fields in ($part), fields: ...")
        case Some(params) =>
          params.map(param => verifyParam(param, templates, part)).flatten
        case None =>
          List(s"invalid part: $part, expected list of values, or empty.")
      })
  }

  private def verifyParam(json: Json, templates: Seq[PayloadParam], part: String): Option[String] = {
    // TODO can we relay on the payload structure being correct (name/value)?
    //  REMOVE ".get"
    val objMap = json.asObject.map(_.toMap).get
    val name = objMap.get("name").get.asString.get
    val value = objMap.get("value").get

    templates.find(_.name.equals(name)) match {
      case Some(param) if !param.types.exists(pType => verifyField(pType, value)) =>
        Some(s"invalid param ($part -> $name), allowed types (${param.types.map(_.name).toString()}) got ($value)")
      case Some(_) => None // valid
      case None => Some(s"invalid param ($part) , unknown field ($name)") // return missing field message.
    }
  }


  // TODO - implement validators.
  //  Ideally we wouldn't want to do a match case on enums that might change in the future.
  //  instead the enum should implement the logic, but in my case I didn't want the enum to know what a json is.
  private def verifyField(param: ParamType, value: Json): Boolean = {
    param match {
      case ParamType.StringType => value.asString.nonEmpty
      case ParamType.IntType => value.asNumber.flatMap(_.toInt).nonEmpty
      case ParamType.BooleanType => value.asBoolean.nonEmpty
      case ParamType.DateType => value.asString.flatMap(DateUtils.parseDate).nonEmpty
      case ParamType.ListType =>  value.asArray.nonEmpty
      case ParamType.AuthTokenType => false
      case ParamType.EmailType => false
      case ParamType.UUIDType => false
    }
  }

}

package services

import io.circe.parser.*
import io.circe.{HCursor, Json}
import models.types.ParamTypeUtil
import models.{PayloadModel, PayloadParam}

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

  def verifyParam(json: Json, templates: Seq[PayloadParam], part: String): Option[String] = {
    // TODO this block is bad. we need to verify that the field exists.
    val objMap = json.asObject.map(_.toMap).get
    val name = objMap.get("name").get.asString.get
    val value = objMap.get("value").get.asString.get

    templates.find(_.name.equals(name)) match {
      case Some(param) if !param.types.exists(pType => ParamTypeUtil.isValidParam(pType, value)) =>
        Some(s"invalid param ($part -> $name), allowed types (${param.types.map(_.name).toString()}) got ($value)")
      case Some(_) => None // valid
      case None => Some(s"invalid param ($part) , unknown field ($name)") // return missing field message.
    }
  }
}

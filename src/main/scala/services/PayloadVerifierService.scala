package services

import io.circe.parser.*
import io.circe.{HCursor, Json}
import models.types.ParamType
import models.{PayloadModel, PayloadParam}
import org.http4s.InvalidMessageBodyFailure
import utils.Cache.urlTemplatesCache
import utils.DateUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}
import utils.Cache.*
import utils.Convertors.*

import scala.util.matching.Regex

object PayloadVerifierService {

  val successMessage: Json = parse(""" {"status": "normal"} """).getOrElse(Json.Null)
  val failureMessage: Json = parse(""" {"status": "abnormal"} """).getOrElse(Json.Null)

  val authTokenStarter = "Bearer "
  val authTokenPattern = """[^0-9A-Za-z]+""".r

  def verifyEntity(json: Json): Seq[String] = {
    val templateTry = fetchTemplate(json)

    templateTry match {
      case Success(template) => validateFields(json, template)
      case Failure(err) => List(err.getMessage)
    }
  }

  def appendErrorMessages(json: Json, errorMessages: Seq[String]): Json = {
    if (errorMessages.isEmpty) json
    else {
      val message = StringBuilder().append("[")
      errorMessages.map(msg => message.append(s""" "$msg" """))
      message.append("]")
      val msgJson = parse(message.result).getOrElse(Json.Null)

      json.hcursor.withFocus(_.mapObject(_.add("abnormalities", msgJson))).top.get
    }
  }

  private def validateFields(json: Json, payloadModel: PayloadModel): Seq[String] = {
    val resOpt = json.asObject.map(_.toMap).map { objMap =>
      verifyArr(objMap.get(PayloadModel.BODY), payloadModel.body, PayloadModel.BODY) ++
        verifyArr(objMap.get(PayloadModel.HEADERS), payloadModel.headers, PayloadModel.HEADERS) ++
        verifyArr(objMap.get(PayloadModel.QUERY_PARAMS), payloadModel.query_params, PayloadModel.QUERY_PARAMS)
    }

    resOpt.getOrElse(List(s"invalid payload structure"))
  }

  private def fetchTemplate(json: Json): Try[PayloadModel] = {
    json.asObject.map(_.toMap)
      .toTry(InvalidMessageBodyFailure(s"invalid payload"))
      .flatMap(jMap => {
        for {
          path <- jMap.get(PayloadModel.PATH).flatMap(_.asString)
            .toTry(InvalidMessageBodyFailure(s"invalid payload -> path"))
          method <- jMap.get(PayloadModel.METHOD).flatMap(_.asString)
            .toTry(InvalidMessageBodyFailure(s"invalid payload -> method"))
          template <- urlTemplatesCache.getIfPresent((path, method))
            .toTry(InvalidMessageBodyFailure(s"entity not found"))
        } yield template
      })
  }


  private def verifyArr(jsonOpt: Option[Json], templates: Seq[PayloadParam], part: String): Seq[String] = {
    jsonOpt.fold(List(s"missing $part"))(json => json.asArray match {
        case Some(x) if x.isEmpty && templates.exists(_.isRequired) =>
          // TODO - add expected field names.
          List(s"missing fields in $part, expected (...)")
        case Some(x) if x.isEmpty => List.empty
        case Some(x) if x.nonEmpty && templates.isEmpty =>
          // TODO add field names.
          List(s"unknown fields in ($part), fields: (...)")
        case Some(params) =>
          params.map(param => verifyParam(param, templates, part)).flatten
        case None =>
          List(s"invalid part: $part, expected list of values, or empty.")
      })

    // TODO - need to check if all required fields exist in payload.
  }

  private def verifyParam(json: Json, templates: Seq[PayloadParam], part: String): Option[String] = {
    // can we relay on the payload structure being correct (name/value)?
    val objMap = json.asObject.map(_.toMap).get
    val name = objMap.get("name").get.asString.get
    val value = objMap.get("value").get

    templates.find(_.name.equals(name)) match {
      case Some(param) if !param.types.exists(pType => verifyField(pType, value)) =>
        Some(s"invalid param ($part -> $name), allowed types (${param.types.map(_.name).toString()}) got (${value.name})")
      case Some(_) => None
      case None => Some(s"invalid param ($part) , unknown field ($name)") // return missing field message.
    }
  }


  // TODO - implement validators
  private def verifyField(param: ParamType, value: Json): Boolean = {
    param match {
      case ParamType.StringType => value.asString.nonEmpty
      case ParamType.IntType => value.asNumber.flatMap(_.toInt).nonEmpty
      case ParamType.BooleanType => value.asBoolean.nonEmpty
      case ParamType.DateType => value.asString.flatMap(DateUtils.parseDate).nonEmpty
      case ParamType.ListType =>  value.asArray.nonEmpty
      case ParamType.AuthTokenType =>
        value.asString.exists(token =>
          token.startsWith(authTokenStarter)
          // fix
          //authTokenPattern.pattern.matcher(token.substring(authTokenStarter.size)).matches
        )
      case ParamType.EmailType => false
      case ParamType.UUIDType => false
    }
  }

}

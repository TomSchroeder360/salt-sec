package services

import io.circe.Json
import models.types.ParamType
import models.{RequestModel, RequestParamModel, TemplateModel, TemplateParamModel}
import utils.Cache.urlTemplatesCache
import utils.Convertors.*
import utils.DateUtils

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class VerifierService:

  private val authTokenStarter: String = "Bearer "
  private val authTokenPattern: Regex = """[^0-9A-Za-z]+""".r

  def verify(request: RequestModel): Seq[String] = {
    fetchTemplate(request) match {
      case Success(template) =>
        verifyArray(request.body, template.body, TemplateModel.BODY) ++
          verifyArray(request.headers, template.headers, TemplateModel.HEADERS) ++
          verifyArray(request.query_params, template.query_params, TemplateModel.QUERY_PARAMS)
      case Failure(err) => List(err.getMessage)
    }
  }

  private def fetchTemplate(request: RequestModel): Try[TemplateModel] = {
    urlTemplatesCache
      .getIfPresent((request.path, request.method))
      .toTry(Exception(s"Template for given request not found"))
  }

  private def verifyArray(params: Seq[RequestParamModel], templates: Seq[TemplateParamModel], part: String): Seq[String] = {
    params match {
      case Nil if templates.exists(_.isRequired) =>
        val requiredFields = templates.filter(_.isRequired).map(_.name).mkString(",")
        List(s"Missing required fields in $part, expected ($requiredFields)")
      case Nil =>
        List.empty
      case list =>
        val res = list.flatMap(param => verifyParam(param, templates, part))
        val missingFields = templates
          .filter(_.isRequired)
          .filter(template => !list.exists(_.name.equalsIgnoreCase(template.name)))
          .mkString(",")

        res ++ {
          if (missingFields.isEmpty) List.empty
          else List(s"Missing required fields in $part, expected ($missingFields)")
        }
    }
  }

  private def verifyParam(request: RequestParamModel, templates: Seq[TemplateParamModel], part: String): Option[String] = {
    templates.find(_.name.equalsIgnoreCase(request.name)) match {
      case Some(param) if !param.types.exists(pType => verifyField(pType, request.value)) =>
        Some(s"Invalid parameter in $part -> ${request.name} , allowed types: ${param.types.map(_.name).mkString(", ")}")
      case Some(_) => None
      case None => Some(s"Invalid parameter in $part , unknown field: (${request.name})")
    }
  }

  // TODO finish validators.
  private def verifyField(param: ParamType, json: Json): Boolean = {
    param match {
      case ParamType.StringType => json.asString.nonEmpty
      case ParamType.IntType => json.asNumber.flatMap(_.toInt).nonEmpty
      case ParamType.BooleanType => json.asBoolean.nonEmpty
      case ParamType.DateType => json.asString.flatMap(DateUtils.parseDate).nonEmpty
      case ParamType.ListType => json.asArray.nonEmpty
      case ParamType.AuthTokenType =>
        json.asString.exists(token =>
          token.startsWith(authTokenStarter) &&
            authTokenPattern.pattern.matcher(token.substring(authTokenStarter.length)).matches
        )
      case ParamType.EmailType => false
      case ParamType.UUIDType => false
    }
  }
end VerifierService
package models

import io.circe.{Json, JsonObject}
import models.types.{ParamType, UrlMethodType}
import org.http4s.InvalidMessageBodyFailure
import utils.Convertors.*

import scala.util.{Failure, Success, Try}

case class PayloadModel(method: UrlMethodType, path: String, query_params: Seq[PayloadParam], headers: Seq[PayloadParam], body: Seq[PayloadParam])

end PayloadModel

object PayloadModel:

  val PATH: String = "path"
  val METHOD: String = "method"
  val QUERY_PARAMS: String = "query_params"
  val HEADERS: String = "headers"
  val BODY: String = "body"
  
  def parse(json: Json): Try[PayloadModel] = {
    if (!json.isObject){
      return Failure(InvalidMessageBodyFailure(s"body is not a valid json object ${json.toString}"))
    }

    json.asObject.map(_.toMap).toTry
      .flatMap { jMap =>
        if (jMap.size != 5) {
          Failure(InvalidMessageBodyFailure(s"body does not have correct number of fields(3). json: ${jMap.toString()}"))
        } else {

          for {
            path <- jMap.get(PATH).flatMap(_.asString).toTry
            method <- jMap.get(METHOD).flatMap(_.asString).toTry.flatMap(_.toUrlMethodType)
            queryParams <- jMap.get(QUERY_PARAMS).toTry.flatMap(parsePayloadParam)
            headers <- jMap.get(HEADERS).toTry.flatMap(parsePayloadParam)
            body <- jMap.get(BODY).toTry.flatMap(parsePayloadParam)
          } yield PayloadModel(method, path, query_params = queryParams, headers = headers, body = body)
        }
      }
  }

  private def parsePayloadParam(json: Json): Try[Seq[PayloadParam]] = {
    json.asArray.toTry.flatMap(_.parseSeq[PayloadParam](PayloadParam.parse))
  }

end PayloadModel

enum PayloadModelFields(val name: String):

  case path extends PayloadModelFields("path")

end PayloadModelFields


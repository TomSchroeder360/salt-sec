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

    json.asObject.map(_.toMap)
      .toTry(InvalidMessageBodyFailure(s"body is not an object"))
      .flatMap { jMap =>
        if (jMap.size != 5) {
          Failure(InvalidMessageBodyFailure(s"body does not have correct number of fields(3). json: ${jMap.toString()}"))
        } else {

          for {
            path <- jMap.get(PATH).flatMap(_.asString)
              .toTry(InvalidMessageBodyFailure(s"path value not found"))
            method <- jMap.get(METHOD).flatMap(_.asString)
              .toTry(InvalidMessageBodyFailure(s"method value not found"))
              .flatMap(_.toUrlMethodType)
            queryParams <- jMap.get(QUERY_PARAMS)
              .toTry(InvalidMessageBodyFailure(s"query params value not found"))
              .flatMap(json => parsePayloadParam(json, QUERY_PARAMS))
            headers <- jMap.get(HEADERS)
              .toTry(InvalidMessageBodyFailure(s"header value not found"))
              .flatMap(json => parsePayloadParam(json, HEADERS))
            body <- jMap.get(BODY)
              .toTry(InvalidMessageBodyFailure(s"body value not found"))
              .flatMap(json => parsePayloadParam(json, BODY))
          } yield PayloadModel(method, path, query_params = queryParams, headers = headers, body = body)
        }
      }
  }

  private def parsePayloadParam(json: Json, owner: String): Try[Seq[PayloadParam]] = {
    json.asArray
      .toTry(InvalidMessageBodyFailure(s"expected payload ($owner) should be array"))
      .flatMap(_.parseSeq[PayloadParam](PayloadParam.parse))
  }

end PayloadModel


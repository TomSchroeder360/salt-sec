package models

import io.circe.{Json, JsonObject}
import models.PayloadModel.{BODY, parseFailure}
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

  private def parseFailure(received: String): InvalidMessageBodyFailure =
    InvalidMessageBodyFailure(s"expected entity with ($PATH, $METHOD, $QUERY_PARAMS, $HEADERS, $BODY). received: $received")

  def parse(json: Json): Try[PayloadModel] = {
    json.asObject.map(_.toMap)
      .toTry(InvalidMessageBodyFailure(s"body is not an object"))
      .flatMap { jMap =>
        if (jMap.size != 5) {
          Failure(InvalidMessageBodyFailure(s"body does not have correct number of fields(5). json: ${jMap.toString()}"))
        } else {
          parsePayloadMap(jMap)
        }
      }
  }

  private def parsePayloadMap(jMap: Map[String, Json]): Try[PayloadModel] = {
    for {
      path <- jMap.get(PATH).flatMap(_.asString)
        .toTry(parseFailure(jMap.keys.toString()))
      method <- jMap.get(METHOD).flatMap(_.asString)
        .toTry(parseFailure(jMap.keys.toString()))
        .flatMap(methodType =>
          methodType.toUrlMethodType.toTry(InvalidMessageBodyFailure(s"invalid method type $methodType")))
      queryParams <- jMap.get(QUERY_PARAMS)
        .toTry(parseFailure(jMap.keys.toString()))
        .flatMap(json => parsePayloadParam(json, QUERY_PARAMS))
      headers <- jMap.get(HEADERS)
        .toTry(parseFailure(jMap.keys.toString()))
        .flatMap(json => parsePayloadParam(json, HEADERS))
      body <- jMap.get(BODY)
        .toTry(parseFailure(jMap.keys.toString()))
        .flatMap(json => parsePayloadParam(json, BODY))
    } yield PayloadModel(method, path, query_params = queryParams, headers = headers, body = body)
  }

  private def parsePayloadParam(json: Json, owner: String): Try[Seq[PayloadParam]] = {
    json.asArray
      .toTry(InvalidMessageBodyFailure(s"expected payload ($owner) should be array"))
      .flatMap(_.parseSeq[PayloadParam](PayloadParam.parse))
  }

end PayloadModel


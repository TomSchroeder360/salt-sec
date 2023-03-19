package models

import io.circe.Json
import models.types.ParamType
import org.http4s.InvalidMessageBodyFailure
import utils.Convertors._

import scala.util.{Failure, Try}

case class PayloadParam(name: String, types: Seq[ParamType], isRequired: Boolean)

object PayloadParam:

  val NAME: String = "name"
  val TYPES: String = "types"
  val REQUIRED: String = "required"

  private def parseFailure(received: String): InvalidMessageBodyFailure =
    InvalidMessageBodyFailure(s"expected entity with (name, types, required). received: $received")


  def parse(json: Json): Try[PayloadParam] = {
    json.asObject.map(_.toMap)
      .toTry(parseFailure(json.toString))
      .flatMap(jMap =>{
        if (jMap.size != 3) {
          Failure(parseFailure(json.toString))
        } else {
          for {
            name <- jMap.get(NAME).flatMap(_.asString)
              .toTry(parseFailure(json.toString))
            types <- jMap.get(TYPES).flatMap(_.asArray)
              .toTry(parseFailure(json.toString))
              .flatMap(parseParamTypes)
            required <- jMap.get(REQUIRED).flatMap(_.asBoolean)
              .toTry(parseFailure(json.toString))
          } yield PayloadParam(name, types, required)
        }
      })
  }

  private def parseParamTypes(jSeq: Seq[Json]): Try[Seq[ParamType]] = {
    jSeq.parseSeq[ParamType](_.asString
      .flatMap(_.toParamType)
      .toTry(InvalidMessageBodyFailure(s"received an invalid type: ${jSeq.toString}")))
  }

end PayloadParam

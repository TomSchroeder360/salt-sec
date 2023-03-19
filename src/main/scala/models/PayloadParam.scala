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

  def retreiveMandatories(paramsSeq: Seq[PayloadParam]): Seq[PayloadParam] = {
    paramsSeq.filter(_.isRequired)
  }
  def parse(json: Json): Try[PayloadParam] = {
    if (!json.isObject) {
      return Failure(InvalidMessageBodyFailure(s"body is not a valid json object ${json.toString}"))
    }

    json.asObject.map(_.toMap).toTry
      .flatMap(jMap =>{
        if (jMap.size != 3) {
          Failure(InvalidMessageBodyFailure(s"body PARAM does not have correct number of fields(3). json: ${jMap.toString()}"))
        } else {
          for {
            name <- jMap.get(NAME).flatMap(_.asString).toTry
            types <- jMap.get(TYPES).flatMap(_.asArray).toTry.flatMap(parseParamTypes)
            required <- jMap.get(REQUIRED).flatMap(_.asBoolean).toTry
          } yield PayloadParam(name, types, required)
        }
      })
  }

  private def parseParamTypes(jSeq: Seq[Json]): Try[Seq[ParamType]] = {
    jSeq.parseSeq[ParamType](_.asString.toTry.flatMap(_.toParamType))
  }

end PayloadParam

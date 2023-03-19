package services

import cats.effect.IO
import com.github.benmanes.caffeine.cache.Caffeine
import io.circe.{HCursor, Json}
import models.*
import models.types.UrlMethodType
import utils.Cache.*
import utils.Convertors.*
import io.circe.parser.*
import org.http4s.InvalidMessageBodyFailure
import services.ProtectorService

import scala.util.{Failure, Success, Try}

class ProtectorService:
  def save(model: Json): Try[Unit] = {
    PayloadModel
      .parse(model)
      .map(payload => urlTemplatesCache.put((payload.path, payload.method.toString) , payload))
  }

  def verify(entity: Json): Json = {
    val issues = ProtectorService.verifyEntity(entity)
    if (issues.isEmpty) ProtectorService.successMessage
    else {
      // TODO add failureMessage
      ProtectorService.appendErrorMessages(entity, issues)
    }
  }

end ProtectorService

object ProtectorService:
  val successMessage: Json = parse(""" {"status": "normal"} """).getOrElse(Json.Null)
  val failureMessage: Json = parse(""" {"status": "abnormal"} """).getOrElse(Json.Null)

  private def verifyEntity(json: Json): Seq[String] = {
    val templateTry = fetchTemplate(json)

    templateTry match {
      case Success(template) => validateFields(json, template)
      case Failure(err) => List(err.getMessage)
    }
  }

  private def validateFields(json: Json, payloadModel: PayloadModel): Seq[String] = {
    val resOpt = json.asObject.map(_.toMap).map { objMap =>
      PayloadVerifierController.verifyArr(objMap.get(PayloadModel.BODY), payloadModel.body, PayloadModel.BODY) ++
        PayloadVerifierController.verifyArr(objMap.get(PayloadModel.HEADERS), payloadModel.headers, PayloadModel.HEADERS) ++
        PayloadVerifierController.verifyArr(objMap.get(PayloadModel.QUERY_PARAMS), payloadModel.query_params, PayloadModel.QUERY_PARAMS)
    }
    
    resOpt.getOrElse(List(s"invalid payload structure"))
  }

  private def fetchTemplate(json: Json): Try[PayloadModel] ={
    json.asObject.map(_.toMap)
      .toTry(InvalidMessageBodyFailure(s"invalid payload"))
      .flatMap(jMap => {
      for {
        path <- jMap.get(PayloadModel.PATH).flatMap(_.asString)
          .toTry(InvalidMessageBodyFailure(s"invalid payload -> path"))
        method <- jMap.get(PayloadModel.METHOD).flatMap(_.asString)
          .toTry(InvalidMessageBodyFailure(s"invalid payload -> method"))
        template <- urlTemplatesCache.getIfPresent((path, method))
          .toTry(InvalidMessageBodyFailure(s"entity model not found"))
      } yield template
    })
  }

  private def appendErrorMessages(json: Json, err: Seq[String]): Json = {
    if (err.isEmpty) json
    else {
      // TODO build message.
      val msg = ""

      // TODO - should use arrayOrObject to cover all types not just mapObject.
      // TODO - handle parse failure.
      json.hcursor.withFocus(_.mapObject(_.add("abnormalities", parse(msg).getOrElse(json)))).top.get
    }
  }

end ProtectorService




  
  

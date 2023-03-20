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
import services.PayloadVerifierService as verifier

import scala.util.{Failure, Success, Try}

class ProtectorService:

  def save(model: Json): Try[Unit] = {
    PayloadModel
      .parse(model)
      .map(payload => urlTemplatesCache.put((payload.path, payload.method.toString) , payload))
  }

  def verify(entity: Json): Json = {
    val issues = verifier.verifyEntity(entity)
    if (issues.isEmpty) verifier.successMessage
    else {
      val payload = verifier.appendErrorMessages(entity, issues)
      verifier.failureMessage.hcursor.withFocus(_.mapObject(_.add("payload", payload))).top.get
    }
  }

end ProtectorService

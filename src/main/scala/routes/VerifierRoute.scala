package routes

import cats.effect.IO
import io.circe.Json
import models.RequestModel
import org.http4s.HttpRoutes
import org.http4s.Status.Ok
import org.http4s.circe.*
import org.http4s.dsl.io.*
import services.VerifierService
import utils.Codecs.RequestCodec.decoder
import io.circe.generic.auto.*
import io.circe.syntax.*

object VerifierRoute:
  private case class RequestResultDTO(success: String, errors: Seq[String])

  private val verifierService: VerifierService = new VerifierService()

  val route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@GET -> Root / "verify" =>
      req
        .as[Json]
        .map(_.as[RequestModel])
        .flatMap {
          case Right(request) => Ok(verify(request).asJson)
          case Left(err) => BadRequest(s"Failed parsing body: ${err.getMessage}")
        }
    }

  private def verify(request: RequestModel): RequestResultDTO = {
    verifierService.verify(request) match {
      case Nil => RequestResultDTO("valid", List.empty)
      case list => RequestResultDTO("abnormal", list)
    }
  }

end VerifierRoute
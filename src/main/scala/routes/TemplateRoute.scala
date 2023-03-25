package routes

import cats.effect.IO
import io.circe.Json
import models.{RequestModel, TemplateModel}
import org.http4s.HttpRoutes
import org.http4s.Status.Ok
import org.http4s.circe.*
import org.http4s.dsl.io.*
import services.TemplateService
import utils.Codecs.TemplateCodec.decoder

object TemplateRoute:
  private val templateService = new TemplateService()

  val route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root  =>
      req
        .as[Json]
        .map(_.as[TemplateModel])
        .flatMap {
          case Right(template) => Ok(templateService.save(template))
          case Left(err) =>
            BadRequest(s"Failed parsing body: ${err.getMessage}")
        }
  }
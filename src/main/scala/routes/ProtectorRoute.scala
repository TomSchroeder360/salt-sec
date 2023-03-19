package routes

import cats.effect.{ExitCode, IO, IOApp}
import cats.parse.strings.Json
import io.circe.{Json, ParsingFailure}
import org.http4s.Status.Ok
import org.http4s.dsl.io.*
import org.http4s.{EntityDecoder, HttpRoutes, InvalidMessageBodyFailure}
import services.ProtectorService
import org.http4s.circe.*
import scala.util.{Failure, Success, Try}

object ProtectorRoute:
  private val protector = new ProtectorService()

  val route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // 400 - if as[Json] fails
    case req @ GET -> Root / "verify" =>
      req.as[Json].flatMap(body => Ok(protector.verify(body))
    )

    // 400 - if as[Json] fails
    case req@POST -> Root / "save" =>
      req.as[Json].flatMap(body => protector.save(body) match {
        case Success(_) => Ok()
        case Failure(err: InvalidMessageBodyFailure) => BadRequest(err.getMessage)
        // Should not occur anymore
        // case Failure(err: NoSuchElementException) => BadRequest(err.getMessage)
        case Failure(err) =>
          Console.println(s"Something bad happened: ${err}")
          InternalServerError("An unexpected failure occurred")
      })
  }

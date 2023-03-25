package routes

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.server.Router
import org.http4s.{Request, Response}

object BaseRouter:
  val routes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/verifier" -> VerifierRoute.route,
    "/template" -> TemplateRoute.route
  ).orNotFound
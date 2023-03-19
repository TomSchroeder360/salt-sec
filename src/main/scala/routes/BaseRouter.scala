package routes

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response}


object BaseRouter:
  val routes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/verifier" -> ProtectorRoute.route
  ).orNotFound

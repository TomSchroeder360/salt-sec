import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io.*
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response}
import routes.{BaseRouter, ProtectorRoute}

object SaltServer extends IOApp {
  
  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(BaseRouter.routes)
      .resource
      .useForever
      .as(ExitCode.Success)
}
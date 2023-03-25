
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import routes.BaseRouter

object SaltServer extends IOApp {
  
  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(BaseRouter.routes)
      .resource
      .useForever
      .as(ExitCode.Success)
}
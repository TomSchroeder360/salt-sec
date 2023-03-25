package utils

import cats.syntax.apply.*
import io.circe.{Decoder, Encoder, Json}
import models.{RequestModel, RequestParamModel, TemplateModel, TemplateParamModel}
import utils.Convertors.*

/**
 * Converts case classes to and from json.
 */
object Codecs:

  // Circe does not support auto decoding for case class's with inner case classes.
  // so we need to implement a semi automatic decoder/encoder.

  object TemplateCodec {
    implicit val decoder: Decoder[TemplateModel] = {
      Decoder.instance { cursor =>
        (cursor.get[String](TemplateModel.METHOD).map(_.toUrlMethodType.get),
          cursor.get[String](TemplateModel.PATH),
          cursor.get[Seq[TemplateParamModel]](TemplateModel.QUERY_PARAMS),
          cursor.get[Seq[TemplateParamModel]](TemplateModel.HEADERS),
          cursor.get[Seq[TemplateParamModel]](TemplateModel.BODY)
        ).mapN(TemplateModel.apply)
      }
    }

    private implicit val decodeParam: Decoder[TemplateParamModel] = {
      Decoder.instance { cursor =>
        (cursor.get[String](TemplateParamModel.NAME),
          cursor.get[Seq[String]](TemplateParamModel.TYPES).map(_.map(_.toParamType.get)),
          cursor.get[Boolean](TemplateParamModel.REQUIRED)
        ).mapN(TemplateParamModel.apply)
      }
    }
  }

  object RequestCodec {
    implicit val decoder: Decoder[RequestModel] = {
      Decoder.instance { cursor =>
        (cursor.get[String](RequestModel.METHOD),
          cursor.get[String](RequestModel.PATH),
          cursor.get[Seq[RequestParamModel]](RequestModel.QUERY_PARAMS),
          cursor.get[Seq[RequestParamModel]](RequestModel.HEADERS),
          cursor.get[Seq[RequestParamModel]](RequestModel.BODY)
        ).mapN(RequestModel.apply)
      }
    }

    private implicit val decodeParam: Decoder[RequestParamModel] = {
      Decoder.instance(cursor =>
        (cursor.get[String](RequestParamModel.NAME),
          cursor.get[Json](RequestParamModel.VALUE)).mapN(RequestParamModel.apply)
      )
    }
  }

end Codecs
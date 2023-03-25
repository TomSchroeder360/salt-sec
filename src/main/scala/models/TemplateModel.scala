package models

import models.types.UrlMethodType

case class TemplateModel(method: UrlMethodType,
                         path: String,
                         query_params: Seq[TemplateParamModel],
                         headers: Seq[TemplateParamModel],
                         body: Seq[TemplateParamModel])

object TemplateModel:
  val PATH: String = "path"
  val METHOD: String = "method"
  val QUERY_PARAMS: String = "query_params"
  val HEADERS: String = "headers"
  val BODY: String = "body"
end TemplateModel
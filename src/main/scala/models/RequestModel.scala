package models

case class RequestModel(method: String,
                        path: String,
                        query_params: Seq[RequestParamModel],
                        headers: Seq[RequestParamModel],
                        body: Seq[RequestParamModel])

object RequestModel:
  val PATH: String = "path"
  val METHOD: String = "method"
  val QUERY_PARAMS: String = "query_params"
  val HEADERS: String = "headers"
  val BODY: String = "body"
end RequestModel
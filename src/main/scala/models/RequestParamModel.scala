package models

import io.circe.Json

// TODO - remove Json and store a dynamic variable.
case class RequestParamModel(name: String, value: Json)

object RequestParamModel:
  val NAME: String = "name"
  val VALUE: String = "value"
end RequestParamModel
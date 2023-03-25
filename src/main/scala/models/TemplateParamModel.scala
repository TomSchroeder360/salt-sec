package models

import models.types.ParamType

case class TemplateParamModel(name: String, types: Seq[ParamType], isRequired: Boolean)

object TemplateParamModel:
  val NAME: String = "name"
  val TYPES: String = "types"
  val REQUIRED: String = "required"
end TemplateParamModel
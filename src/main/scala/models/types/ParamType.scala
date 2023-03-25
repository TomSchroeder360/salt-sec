package models.types

enum ParamType(val name: String):
  case StringType extends ParamType("String")
  case IntType extends ParamType("Int")
  case BooleanType extends ParamType("Boolean")
  case ListType extends ParamType("List")
  case DateType extends ParamType("Date")
  case EmailType extends ParamType("Email")
  case UUIDType extends ParamType("UUID")
  case AuthTokenType extends ParamType("Auth-Token")
end ParamType
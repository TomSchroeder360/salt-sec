package utils

import io.circe.Json
import models.*
import models.types.{ParamType, UrlMethodType}

import scala.util.{Success, Failure, Try}

object Convertors {

  // add support for error message.
  extension [T](opt: Option[T]) def toTry: Try[T] = Try(opt.get)

  extension (opt: Option[_]) def toTry2(err: Throwable): Try[_] = opt.map(Success(_)).getOrElse(Failure(err))

  extension (value: String) def toParamType: Try[ParamType] =ParamType.values.find(param => value.equalsIgnoreCase(param.name)).toTry

  // Maybe use valueOf.
  extension (value: String) def toUrlMethodType: Try[UrlMethodType] = Try(UrlMethodType.valueOf(value))

  /**
   * Receives seq[Y], Returns Try[Seq[T]] -
   * Tries to convert every Y to T. first failure will propagate to caller.
   */
  extension [Y](jSeq: Seq[Y]) def parseSeq[T](handler: Y => Try[T]): Try[Seq[T]] = {
    jSeq.iterator.foldLeft(Try(Seq.empty[T])) {
      (optionalSeq, innerJson) => optionalSeq.flatMap(seq => handler(innerJson).map(_ +: seq))
    }
  }
}

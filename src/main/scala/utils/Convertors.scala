package utils

import models.types.{ParamType, UrlMethodType}
import scala.util.{Success, Failure, Try}

object Convertors:
  
  extension [T](opt: Option[T]) def toTry(err: Throwable): Try[T] = opt.map(Success(_)).getOrElse(Failure(err))

  extension (value: String) def toParamType: Option[ParamType] =ParamType.values.find(param => value.equalsIgnoreCase(param.name))
  
  extension (value: String) def toUrlMethodType: Option[UrlMethodType] = Try(UrlMethodType.valueOf(value)).toOption

  /**
   * Receives seq[Y], Returns Try[Seq[T]] -
   * Tries to convert every Y to T. first failure will propagate to caller.
   */
// Not used
//  extension [Y](jSeq: Seq[Y]) def parseSeq[T](handler: Y => Try[T]): Try[Seq[T]] = {
//    jSeq.iterator.foldLeft(Try(Seq.empty[T])) {
//      (optionalSeq, value) => optionalSeq.flatMap(seq => handler(value).map(_ +: seq))
//    }
//  }

end Convertors
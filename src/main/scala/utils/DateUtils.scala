package utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import scala.util.Try

object DateUtils {
  
  val outputFormat = new SimpleDateFormat("dd-mm-yyyy")
  def parseDate(isDate: String): Option[Date] = {
    // TODO - improve performance here?
    Try(outputFormat.parse(isDate)).toOption
  }
}

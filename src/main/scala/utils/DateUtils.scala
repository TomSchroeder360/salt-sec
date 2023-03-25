package utils

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

object DateUtils:
  
  private val outputFormat = new SimpleDateFormat("dd-mm-yyyy")
  
  def parseDate(isDate: String): Option[Date] = Try(outputFormat.parse(isDate)).toOption

end DateUtils
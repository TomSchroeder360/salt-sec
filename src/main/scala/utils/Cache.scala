package utils

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import models.TemplateModel

object Cache:
  val urlTemplatesCache: Cache[(String, String), TemplateModel] =
    Scaffeine()
      .recordStats()
      .maximumSize(10000)
      .build[(String, String), TemplateModel]()

end Cache
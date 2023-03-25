package services

import models.TemplateModel
import utils.Cache.*

class TemplateService:
  def save(template: TemplateModel): Unit = {
    urlTemplatesCache.put((template.path, template.method.toString), template)
  }
end TemplateService
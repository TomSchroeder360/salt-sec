package utils

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import io.circe.Json
import models.PayloadModel
import models.types.UrlMethodType

object Cache {

  val urlTemplatesCache: Cache[(String, String), PayloadModel] =
    Scaffeine()
      .recordStats()
      .maximumSize(10000)
      .build[(String, String), PayloadModel]()
//  private val modelCaffeineCache = Caffeine.newBuilder().maximumSize(10000L).build[String, Entry[Json]]
//  implicit val modelCache: Cache[IO, String, Json] = CaffeineCache(modelCaffeineCache)
}

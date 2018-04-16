package com.gu.mediaservice.lib.play

import com.gu.mediaservice.lib.auth.Authentication
import com.gu.mediaservice.lib.config.CommonConfig
import com.gu.mediaservice.lib.management.Management
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents
import play.filters.gzip.GzipFilterComponents

import scala.concurrent.ExecutionContext

abstract class GridComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with AhcWSComponents with HttpFiltersComponents with CORSComponents with GzipFilterComponents {

  def config: CommonConfig

  implicit val ec: ExecutionContext = executionContext

  private val disabledFilters: Set[EssentialFilter] = Set(allowedHostsFilter)

  final override def httpFilters: Seq[EssentialFilter] = {
    super.httpFilters.filterNot(disabledFilters.contains) ++ Seq(corsFilter, gzipFilter, new RequestLoggingFilter(materializer))
  }

  // TODO MRB: set allowed CORS origins

  val management = new Management(controllerComponents)
  val auth = new Authentication(config, actorSystem, defaultBodyParser, wsClient, controllerComponents, executionContext)
}

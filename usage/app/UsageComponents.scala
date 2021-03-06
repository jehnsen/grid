import com.gu.mediaservice.lib.play.{GridComponents, RequestLoggingFilter}
import controllers.UsageApi
import lib._
import model._
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents
import play.filters.gzip.GzipFilterComponents
import router.Routes

import scala.concurrent.Future

class UsageComponents(context: Context) extends GridComponents(context) {

  final override lazy val config = new UsageConfig(configuration)

  val usageMetadataBuilder = new UsageMetadataBuilder(config)
  val mediaWrapper = new MediaWrapperOps(usageMetadataBuilder)
  val mediaUsage = new MediaUsageOps(usageMetadataBuilder)
  val liveContentApi = new LiveContentApi(config)
  val usageGroup = new UsageGroupOps(config, mediaUsage, liveContentApi, mediaWrapper)
  val usageTable = new UsageTable(config, mediaUsage)
  val usageMetrics = new UsageMetrics(config)
  val usageNotifier = new UsageNotifier(config, usageTable)
  val usageStream = new UsageStream(usageGroup)
  val usageRecorder = new UsageRecorder(usageMetrics, usageTable, usageStream, usageNotifier, usageNotifier)
  val notifications = new Notifications(config)

  val apiOnly = config.appTagBasedConfig.getOrElse("apiOnly", false)
  if(!apiOnly) {
    val crierReader = new CrierStreamReader(config)
    crierReader.start()
  }

  usageRecorder.start()
  context.lifecycle.addStopHook(() => {
    usageRecorder.stop()
    Future.successful(())
  })

  val controller = new UsageApi(auth, usageTable, usageGroup, notifications, config, usageRecorder, liveContentApi, controllerComponents, playBodyParsers)

  override lazy val router = new Routes(httpErrorHandler, controller, management)
}

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Inject, Provides}
import play.cache.CacheApi
import uk.bl.wa.shine.vis.Rescued
import uk.bl.wa.shine.{Pagination, Shine, Solr}

class Module extends AbstractModule {

  override def configure() = {
    // Initialise InitialData on application startup
    bind(classOf[InitialData]).asEagerSingleton()
  }

  /**
    * Provide the Shine object, the access object for Solr already configured.
    * Uses the Play *Java* Configuration (from application.conf)
    */
  @Provides
  def provideShine(configuration: play.Configuration, cacheApi: CacheApi) : Shine = {
    val config = configuration.getConfig("shine")
    new Shine(config, cacheApi)
  }

  /**
    * Provide the Rescued object.
    * Uses the Play *Java* Configuration (from application.conf)
    */
  @Provides
  def provideRescued(configuration: play.Configuration) : Rescued = {
    val config = configuration.getConfig("shine")
    new Rescued(config)
  }

  /**
    * Provide the Pagination object.
    * Uses the Play *Java* Configuration (from application.conf)
    */
  @Provides
  def providePagination(configuration: play.Configuration) : Pagination = {
    val config = configuration.getConfig("shine")
    val recordsPerPage = config.getInt("per_page")
    val maxNumberOfLinksOnPage = config.getInt("max_number_of_links_on_page")
    val maxViewablePages = config.getInt("max_viewable_pages")

    new Pagination(recordsPerPage, maxNumberOfLinksOnPage, maxViewablePages)
  }

  /**
    * This is a shortcut for injecting the Shine *Scala* configuration directly without having to do the
    * configuration.getConfig("shine").get every time.
    *
    * Also, it throws an exception is the shine { } object doesn't exist in application.conf
    *
    * Example of usage in a controller:
    * class Search @Inject() (@Named("ShineConfiguration") shineConfig: play.api.Configuration) { ... }
    */
  @Provides
  @Named("ShineConfiguration")
  def provideShineConfiguration(configuration: play.api.Configuration) = {
    configuration.getConfig("shine") match {
      case Some(c) => c
      case None => throw new RuntimeException("Shine configuration was not found, trying to provide ShineConfiguration in Module.scala")
    }
  }
}

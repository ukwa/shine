import com.google.inject.{AbstractModule, Inject, Provides}
import play.Configuration
import play.cache.CacheApi
import uk.bl.wa.shine.{Pagination, Shine, Solr}

class Module extends AbstractModule {

  override def configure() = {
    // Initialise InitialData on application startup
    bind(classOf[InitialData]).asEagerSingleton()
  }

  /**
    * Provide the Shine object, the access object for Solr already configured.
    *
    * @param configuration  The Play Java Configuration (from application.conf)
    * @return
    */
  @Provides
  def provideShine(configuration: Configuration, cacheApi: CacheApi) : Shine = {
    val config = configuration.getConfig("shine")
    new Shine(config, cacheApi)
  }

  @Provides
  def providePagination(configuration: Configuration) : Pagination = {
    val config = configuration.getConfig("shine")
    val recordsPerPage = config.getInt("per_page")
    val maxNumberOfLinksOnPage = config.getInt("max_number_of_links_on_page")
    val maxViewablePages = config.getInt("max_viewable_pages")

    new Pagination(recordsPerPage, maxNumberOfLinksOnPage, maxViewablePages)
  }

}

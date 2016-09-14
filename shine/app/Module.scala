import com.google.inject.{AbstractModule, Inject, Provides}
import play.Configuration
import uk.bl.wa.shine.{Shine, Solr}

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
  def provideShine(configuration: Configuration) : Shine = {
    val config = configuration.getConfig("shine")
    new Shine(config)
  }



}

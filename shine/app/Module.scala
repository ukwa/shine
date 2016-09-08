import com.google.inject.AbstractModule

class Module extends AbstractModule {

  override def configure() = {
    // Initialise InitialData on application startup
    bind(classOf[InitialData]).asEagerSingleton()
  }
}

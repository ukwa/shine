import cucumber.api.java.After;
import cucumber.api.java.Before;
import net.sf.ehcache.CacheManager;
import play.test.TestBrowser;
import play.test.TestServer;
import static play.test.Helpers.*;

/**
 * @author kinmanli
 *
 * Initialise a Test Server for Steps
 */
public class GlobalHooks {
    public static int PORT = 8888;
    public static TestBrowser TEST_BROWSER;
    private static TestServer TEST_SERVER;
    private static boolean initialised = false;

    /**
     * This is executed before every scenario
     */
    @Before
    public void before() {
        if (!initialised) {
            TEST_SERVER = testServer(PORT, fakeApplication(inMemoryDatabase()));
            TEST_BROWSER = testBrowser(HTMLUNIT, PORT);
            start(TEST_SERVER);
            initialised = true;
        }
    }
    
    @After
    public void after() {
        TEST_BROWSER.quit();
    	stop(TEST_SERVER);
    	CacheManager.getInstance().shutdown();
    }
}

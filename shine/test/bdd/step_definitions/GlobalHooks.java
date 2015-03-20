package bdd.step_definitions;
import cucumber.api.java.Before;
import play.test.TestBrowser;
import play.test.TestServer;
import static play.test.Helpers.*;

/**
 * @author kinmanli
 *
 * Initialise a Test Server for Steps
 */
public class GlobalHooks {
    public static int PORT = 9999;
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
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    TEST_BROWSER.quit();
                    TEST_SERVER.stop();
                }
            });
        }
    }
}

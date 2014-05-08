package web;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.monitors.CompositeMonitor;
import org.seleniumhq.selenium.fluent.monitors.HighlightOnError;
import org.seleniumhq.selenium.fluent.monitors.ScreenShotOnError;

/**
 * Created by antoine on 3/23/14.
 */
    public class BasePage extends FluentWebDriver {

        public BasePage(FirefoxDriver delegate) {
            super(delegate,
                    new CompositeMonitor(WholeSuiteListener.codahaleMetricsMonitor,
                            new HighlightOnError(delegate),
                            new ScreenShotOnError.WithUnitTestFrameWorkContext(delegate, BasePage.class, "test-classes", "surefire-reports")));
        }
    }


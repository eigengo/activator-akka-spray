package web.fruits;

import web.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.seleniumhq.selenium.fluent.FluentBy;
import org.seleniumhq.selenium.fluent.TestableString;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.By.className;
import static org.seleniumhq.selenium.fluent.FluentBy.attribute;

/**
 * Created by antoine on 3/23/14.
 */
public class FruitsPage extends BasePage {
    public FruitsPage(FirefoxDriver wd) {
        super(wd);
        div(ngWait(attribute("ng-controller", "fruits-controller"))).getText().shouldContain("FRUITS BLA");
    }
    public TestableString fruitName(int index) {
        return tds(className("name")).get(index).getText();
    }
    public TestableString price(int index) {
        return tds(className("price")).get(index).getText();
    }
    public static By ngWait(final By by) {
        return new FluentBy() {
            @Override
            public void beforeFindElement(WebDriver driver) {
                driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
                ((JavascriptExecutor) driver).executeAsyncScript("var callback = arguments[arguments.length - 1];" +
                        "angular.element(document.body).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);");
                super.beforeFindElement(driver);
            }

            @Override
            public List<WebElement> findElements(SearchContext context) {
                return by.findElements(context);
            }

            @Override
            public WebElement findElement(SearchContext context) {
                return by.findElement(context);
            }

            @Override
            public String toString() {
                return "ngWait(" + by.toString() + ")";
            }
        };
    }


}

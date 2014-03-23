package web.fruits;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Created by antoine on 3/23/14.
 */
public class FruitsTest  {
    private FirefoxDriver wd;

    @Before
    public void makeWebDriverAndGotoSite() {
        wd = new FirefoxDriver();
        wd.get("http://localhost:8080/fruits.html");
    }

    @After
    public void killWebDriver() {
        wd.quit();
    }
    @Test
    public void testFruits() {
        new FruitsPage(wd){{
            fruitName(0).shouldBe("banana");
            price(0).shouldBe("0.79");
            fruitName(1).shouldBe("apple");
            price(1).shouldBe("1.89");
            fruitName(2).shouldBe("raspberry");
            price(2).shouldBe("12.50");
        }
        };

    }

}

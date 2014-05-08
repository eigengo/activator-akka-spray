package web.fruits;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.openqa.selenium.phantomjs.PhantomJSDriver
;

/**
 * Created by antoine on 3/23/14.
 */
class FruitsSpec extends FlatSpec with BeforeAndAfterAll{
    var wd : FirefoxDriver = null

    @Before
    protected override def beforeAll() = {
        wd = new FirefoxDriver()
        wd.get("http://localhost:8080/fruits.html")
    }


    protected override def afterAll() {
        wd.quit();
    }
    "The fruit page "  should "have banana, apple, raspberry" in
     {
        new FruitsPage(wd){{
            fruitName(0).shouldBe("banana")
            price(0).shouldBe("0.79")
            fruitName(1).shouldBe("apple")
            price(1).shouldBe("1.89")
            fruitName(2).shouldBe("raspberry")
            price(2).shouldBe("12.50")
        }
        };

    }

}

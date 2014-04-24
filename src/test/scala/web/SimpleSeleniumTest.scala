package web

import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import org.scalatest.selenium.{WebBrowser}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium._
import java.net.URL
import java.io.IOException
import org.seleniumhq.selenium.fluent.FluentBy
import java.util.concurrent.TimeUnit
import scala.Some
import org.openqa.selenium.phantomjs.PhantomJSDriver
import com.paulhammant.ngwebdriver.WaitForAngularRequestsToFinish.waitForAngularRequestsToFinish;
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.is
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.openqa.selenium.By._
import org.openqa.selenium.By.tagName
import com.paulhammant.ngwebdriver.ByAngular
import org.openqa.selenium.firefox.FirefoxDriver

class SimpleSeleniumTest extends FlatSpec with BeforeAndAfterAll with Matchers with WebBrowser  {

  behavior of "Spray Web Application"
  implicit val webDriver: WebDriver = new FirefoxDriver()
  var ng : ByAngular = null
  var javaScriptExecutor: JavascriptExecutor  = null
  // start the server as this is necessary for the tests
  Rest

   override def beforeAll() {
    // firing up HTTP is asynchronous and takes a variable amount of time
    waitForServerStartup()
     javaScriptExecutor =
     webDriver match {
       case myJavaScriptExecutor: JavascriptExecutor => ng = new ByAngular(myJavaScriptExecutor)
         waitForAngularRequestsToFinish(myJavaScriptExecutor)
         myJavaScriptExecutor
       case _ => println("webDriver is not a JavascriptExecutor")
         null
     }


   }

  "the home page " should " have an input of type file" in {
    webDriver.get(serverUrl + "fruits.html")
    val wes  = webDriver.findElements(ng.repeater("fruit in fruitlist"));

    assertThat(wes.size(), is(3));
    assertThat(wes.get(0).findElement(org.openqa.selenium.By.className("name")).getText(), containsString("banana"));
    assertThat(wes.get(1).findElement(org.openqa.selenium.By.className("name")).getText(), containsString("apple"));
    assertThat(wes.get(2).findElement(org.openqa.selenium.By.className("name")).getText(), containsString("raspberry"));
  }

  def serverUrl = new URL("http://localhost:8080/")

  def waitForServerStartup(): Unit = {
    // Keep sleeping until we see the server respond.
    val secondsToWait = 60
    // remaining = half-second ticks
    def checkAlive(remaining: Int = secondsToWait * 2): Unit =
      if (!httpPing(serverUrl)) remaining match {
        case 0 => sys error "Web server never started!"
        case _ =>
          Thread sleep 500L
          checkAlive(remaining - 1)
      }
    checkAlive()
  }
  /** Returns true if the URL responds with HTTP_OK to a GET request, false on other status codes or refused connection */
  private def httpPing(url: URL): Boolean = {
    import java.net._
    HttpURLConnection setFollowRedirects true
    val request = url.openConnection()
    request setDoOutput true
    val http = request.asInstanceOf[HttpURLConnection]
    http setRequestMethod "GET"
    try {
      http.connect()
      val response = http.getResponseCode
      response == HttpURLConnection.HTTP_OK
    } catch {
      case io: IOException => false
    } finally http.disconnect()
  }
}

object ngWait {
  def ngWait(by: By ) = {
    new FluentBy() {
      override def beforeFindElement(driver : WebDriver ) {
        driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        driver match {
          case javaScriptExecutor: JavascriptExecutor =>
            javaScriptExecutor.executeAsyncScript("var callback = arguments[arguments.length - 1];" +
              "angular.element(document.body).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);");
        }
        super.beforeFindElement(driver)
      }
      override def findElements( context : SearchContext) : java.util.List[WebElement] =  {
        by.findElements(context);
      }

      override def findElement(context : SearchContext) : WebElement = {
        by.findElement(context);
      }
    };
  }
}


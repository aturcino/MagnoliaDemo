import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoginTest {
    private WebDriver driver;
    private String baseUrl;

    @Before
    public void setUp() {
        // Set the path to the chrome executable
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chrome/chromedriver.exe");

        driver = new ChromeDriver();
        baseUrl = "https://demoauthor.magnolia-cms.com/travel/";
        driver.get(baseUrl);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // ToDo: store credentials in Jenkins Secrets / read from env variables
        login("superuser", "superuser");

        // Assertion to verify login success
        WebElement dashboard = driver.findElement(By.id("myCarousel"));
        assertTrue(dashboard.isDisplayed());
    }

    private void login(String username, String password) {
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//form//button[text()='Login']"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }

    @Test
    public void testNavigation() throws InterruptedException {
        navigateAndVerify("/travel/stories.html", "https://demoauthor.magnolia-cms.com/travel/stories.html");
        navigateAndVerify("/travel/about.html", "https://demoauthor.magnolia-cms.com/travel/about.html");
        navigateAndVerify("/travel/contact.html", "https://demoauthor.magnolia-cms.com/travel/contact.html");
        navigateAndVerify("/travel/members.html", "https://demoauthor.magnolia-cms.com/travel/members.html");

        clickFirstDropdownItem("Tours", "/travel/tour-type~active~.html", "https://demoauthor.magnolia-cms.com/travel/tour-type~active~.html");
        clickFirstDropdownItem("Destinations", "/travel/destination~northAmerica~.html", "https://demoauthor.magnolia-cms.com/travel/destination~northAmerica~.html");
    }

    private void navigateAndVerify(String relativeUrl, String expectedUrl) {
        WebElement link = driver.findElement(By.xpath("//a[@href='" + relativeUrl + "']"));
        link.click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        assertEquals(expectedUrl, driver.getCurrentUrl());
        driver.navigate().back();
    }

    private void clickFirstDropdownItem(String menuText, String firstItemRelativeUrl, String firstItemExpectedUrl) throws InterruptedException {
        WebElement dropdownToggle = driver.findElement(By.xpath("//a[contains(text(), '" + menuText + "') and @class='dropdown-toggle']"));
        dropdownToggle.click();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement firstItem = driver.findElement(By.xpath("//a[@href='" + firstItemRelativeUrl + "']"));
        firstItem.click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        assertEquals(firstItemExpectedUrl, driver.getCurrentUrl());

        driver.navigate().back();
        Thread.sleep(1000);
    }

    @Test
    public void testLanguageChange() {
        WebElement germanLanguage = driver.findElement(By.xpath("//a[@href='/travel/de/' and @title='German']"));
        germanLanguage.click();

        WebElement elementInGerman = driver.findElement(By.xpath("//button[contains(@class, 'btn-primary')]"));
        String expectedTextInGerman = "Reise ansehen";
        assertEquals(expectedTextInGerman, elementInGerman.getText());
    }

    @Test
    public void testSearch() {
        WebElement searchField = driver.findElement(By.id("nav-search"));
        searchField.sendKeys("Europe");
        searchField.submit();

        List<WebElement> searchResults = driver.findElements(By.cssSelector(".list-group .list-group-item"));
        assertTrue("There should be at least 3 search results for 'Europe'", searchResults.size() >= 3);

        WebElement firstResult = searchResults.get(0);
        firstResult.click();

        WebElement jumbotron = driver.findElement(By.cssSelector(".jumbotron h2"));
        String jumbotronText = jumbotron.getText().toLowerCase();
        assertEquals("The jumbotron should contain the text 'Careers'", "careers", jumbotronText);

        WebElement body = driver.findElement(By.tagName("body"));
        String pageText = body.getText();
        assertTrue("The word 'Europe' should be present on the page", pageText.contains("Europe"));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
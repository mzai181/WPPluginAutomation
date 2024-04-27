package com.wpautomation.wpautomationtest;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AdminDarkModeTest {
    private ChromeDriver driver;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("env");

        var file = new File(resource.toURI());

        var lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        var url = lines.get(0);
        var userName = lines.get(1);
        var password = lines.get(2);

        driver = new ChromeDriver();
        driver.get(url + "wp-admin/");

        Login(driver, userName, password);

        GoToPlugins(driver);

        GoToAddNewPlugin(driver);

        SearchForWPDarkMode(driver);

        InstallWPDarkMode(driver);

        EnableAdminPanelDarkMode(driver);

        var isDarkModeEnabledForAdmin = isDarkModeEnabledForAdmin(driver);

        if (!isDarkModeEnabledForAdmin) {
            EnableDarkMode(driver);
        }


    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void CheckIfDarkModeWorking() {
        var isDarkModeWorking = driver.findElement(By.cssSelector("html")).getAttribute("class").contains("wp-dark-mode-active");

        assertTrue(isDarkModeWorking);
    }


    public void Login(ChromeDriver driver, String userName, String password)
    {
        driver.findElement(By.id("user_login")).sendKeys(userName);
        driver.findElement(By.id("user_pass")).sendKeys(password);

        driver.findElement(By.id("wp-submit")).click();
    }

    private static void GoToPlugins(ChromeDriver driver) {
        driver.navigate().to("http://localhost:8000/wp-admin/plugins.php");
    }

    private static void GoToAddNewPlugin(ChromeDriver driver) {
        driver.navigate().to("http://localhost:8000/wp-admin/plugin-install.php");
    }

    private static void CheckIfDarkModeEnabledForFrontEnd(ChromeDriver driver) throws Exception {
        driver.navigate().to("http://localhost:8000/");

        var isDarkModeEnabled = driver.findElement(By.xpath("html")).getAttribute("data-wp-dark-mode-active").equals("true");

        if (!isDarkModeEnabled) {
            throw new Exception("Dark mode not enabled in frontend!");
        }
    }

    private static void EnableAdminPanelDarkMode(ChromeDriver driver) {

        driver.navigate().to("http://localhost:8000/wp-admin/admin.php?page=wp-dark-mode#/admin");

        var darkModeSwitch = driver.findElement(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/section[1]/div[1]/div[1]/label/div[1]/div"));
        var isDarkModeDisabled = darkModeSwitch.getAttribute("class").contains("bg-slate-200");

        if (isDarkModeDisabled) {
            darkModeSwitch.click();

            Pause(driver, 1);

            var saveButton = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[4]/button[2]"));
            if (!saveButton.isEmpty()) {
                saveButton.getFirst().click();
            }
        }
    }

    private static void Pause(WebDriver driver, int timeInSeconds) {
        driver.manage().timeouts().implicitlyWait(timeInSeconds, TimeUnit.SECONDS);
    }

    private static void EnableDarkMode(ChromeDriver driver) {
        driver.findElement(By.cssSelector("#wp-admin-bar-wp-dark-mode-admin-bar-switch > div > div > div > span:nth-child(2)")).click();
    }

    private static boolean isDarkModeEnabledForAdmin(ChromeDriver driver) {
        var darkModeToggle = driver.findElement(By.cssSelector("#wp-admin-bar-wp-dark-mode-admin-bar-switch > div > div"));
        var isDarkModeEnabled = darkModeToggle.getAttribute("class").contains("active");
        return isDarkModeEnabled;
    }

    private static void InstallWPDarkMode(ChromeDriver driver) {
        WebDriverWait searchWait = new WebDriverWait(driver, Duration.ofMinutes(2));
        searchWait.until((WebDriver d) -> {
            var searchItem = driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.name.column-name > h3 > a"));
            return !searchItem.isEmpty() && searchItem.getFirst().getText().contains("WP Dark Mode – WordPress Dark Mode Plugin for Improved Accessibility, Dark Theme, Night Mode, and Social Sharing");
        });

        var activeButtons = driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > button"));
        boolean isInActiveState = !activeButtons.isEmpty() && Objects.equals(activeButtons.getFirst().getText(), "Active");
        if (!isInActiveState) {
            var stateElements = driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > a"));

            boolean isInNotActivatedState = Objects.equals(stateElements.getFirst().getText(), "Activate");
            boolean isInNotInstalledState = Objects.equals(driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > a")).getFirst().getText(), "install");

            stateElements.getFirst().click();

            if (!isInNotInstalledState && !isInNotActivatedState) {
                WebDriverWait downloadWait = new WebDriverWait(driver, Duration.ofMinutes(5));
                downloadWait.until((WebDriver d) -> {
                    var activateElements = driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > a"));
                    return !activateElements.isEmpty() && activateElements.getFirst().getText().equals("Activate");
                });

                driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > a")).getFirst().click();
            }

            WebDriverWait activateWait = new WebDriverWait(driver, Duration.ofMinutes(5));
            activateWait.until((WebDriver d) -> {
                var activeElements = driver.findElements(By.cssSelector("#the-list > div > div.plugin-card-top > div.action-links > ul > li:nth-child(1) > a"));
                return !activeElements.isEmpty() && activeElements.getFirst().getText().equals("Active");
            });
        }

        if (!isInActiveState) {
            driver.navigate().refresh();
        }
    }

    private static void SearchForWPDarkMode(ChromeDriver driver) {

        Pause(driver, 1);

        WebElement searchBoxPlugin = driver.findElement(By.id("search-plugins"));
        searchBoxPlugin.sendKeys("WP Dark Mode – WordPress Dark Mode Plugin for Improved Accessibility, Dark Theme, Night Mode, and Social Sharing");
        searchBoxPlugin.submit();
    }
}

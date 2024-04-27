package com.wpautomation.wpautomationtest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.bidi.log.Log;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontEndDarkModeTest {

    private ChromeDriver driver;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
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

        EnableFrontEndDarkMode(driver);

        ChangeSwitchStyle(driver);

        DisabledKeyboardShortcut(driver);

        ChangeAnimationSettings(driver);

    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void CheckIfDarkModeEnabledForFrontEnd() {
        driver.navigate().to("http://localhost:8000/");

        var isDarkModeEnabled = driver.findElement(By.xpath("html")).getAttribute("data-wp-dark-mode-active").equals("true");

        assertTrue(isDarkModeEnabled);
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

    private static void EnableFrontEndDarkMode(ChromeDriver driver) {
        driver.navigate().to("http://localhost:8000/wp-admin/admin.php?page=wp-dark-mode#/frontend");

        Pause(driver, 2);

        var darkModeSwitch = driver.findElement(By.cssSelector("#wp-dark-mode-admin > div > div > div > div.main-content > div.main-content-body > section:nth-child(1) > div.rounded.text-base.flex.flex-col.gap-3.bg-transparent.gap-5 > div:nth-child(1) > label > div.w-auto.h-6.flex.items-center.justify-center > div"));
        var isDarkModeDisabled = darkModeSwitch.getAttribute("class").contains("bg-slate-200");

        if (isDarkModeDisabled) {
            darkModeSwitch.click();

            Pause(driver, 1);
        }

        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/section[1]/div[1]/div[3]/div[2]/label/span[1]")).click();

        Pause(driver, 1);

        var saveButton = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[4]"));
        if (!saveButton.isEmpty()) {
            saveButton.getFirst().click();
        }
    }

    private static void ChangeAnimationSettings(WebDriver driver) {
        driver.navigate().to("http://localhost:8000/wp-admin/admin.php?page=wp-dark-mode#/animation");

        new WebDriverWait(driver, Duration.ofMinutes(2)).until((WebDriver d) -> {
            var animationItem = driver.findElements(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]/div"));
            return !animationItem.isEmpty();
        });

        /// html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]/div
        // /html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]
        // /html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]/div
        // //*[@id="wp-dark-mode-admin"]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]/div
        var animationSwitch = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[1]/div/label/div[1]/div"));
        var isAnimationDisabled = animationSwitch.getAttribute("class").contains("bg-slate-200");
        if (isAnimationDisabled) {
            animationSwitch.click();
        }

        WebDriverWait searchWait = new WebDriverWait(driver, Duration.ofMinutes(2));
        searchWait.until((WebDriver d) -> {
            var animationItem = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[2]/div[1]/div/div[2]/div[5]/span[1]"));
            return !animationItem.isEmpty();
        });

        // select slide left
        driver.findElement(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/div/div/div[1]/div/div[2]/div[1]/div/div[2]/div[5]/span[1]")).click();

        var saveButton = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[4]/button[2]"));
        if (!saveButton.isEmpty()) {
            saveButton.getFirst().click();
        }
    }

    private static void DisabledKeyboardShortcut(ChromeDriver driver) {

        driver.navigate().to("http://localhost:8000/wp-admin/admin.php?page=wp-dark-mode#/accessibility");

        // get keyboard shortcut switch
        var switchElement = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/div[6]/div[1]/label/div[1]/div"));

        var isDisabled = switchElement.getAttribute("class").contains("bg-slate-200");
        if (!isDisabled) {
            switchElement.click();

            Pause(driver, 1);

            var saveKeyboardBtn = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[4]/button[2]"));
            if (!saveKeyboardBtn.isEmpty())
                saveKeyboardBtn.getFirst().click();
        }
    }

    private static void ChangeSwitchStyle(ChromeDriver driver) {

        driver.navigate().to("http://localhost:8000/wp-admin/admin.php?page=wp-dark-mode#/switch");

        // select style
        driver.findElement(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/div/section/div[2]/div/div[2]/div[2]/div[1]/div[2]/div[3]")).click();

        // select custom
        var customButton = driver.findElement(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/div/section/div[2]/div/div[2]/div[4]/div/div[1]/div[1]/div[2]/div[6]/span"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", customButton);
        Pause(driver, 1);
        customButton.click();

        Pause(driver, 1);

        var inputWait = new WebDriverWait(driver, Duration.ofMinutes(5));
        inputWait.until((WebDriver d) -> {
            var elements = d.findElements(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/section/div[2]/div/div[2]/div[4]/div/div[1]/div[1]/div[3]/div[2]/div/div[2]/input"));
            return !elements.isEmpty();
        });

        // set 220
        var scaleElement = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[2]/div[1]/div[4]/div/div/div/div[2]/div[3]/div/section/div[2]/div/div[2]/div[4]/div/div[1]/div[1]/div[3]/div[2]/div/div[2]/input"));
        scaleElement.clear();
        scaleElement.sendKeys("220");

        // set left
        driver.findElement(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[3]/div/section/div[2]/div/div[2]/div[4]/div/div[1]/div[2]/div[2]/div[1]/span")).click();

        Pause(driver, 1);

        // save, this could be empty if settings is already saved before
        var saveButtons = driver.findElements(By.xpath("//*[@id=\"wp-dark-mode-admin\"]/div/div/div/div[2]/div[4]/button[2]"));
        if (!saveButtons.isEmpty())
            saveButtons.getFirst().click();

        Pause(driver, 1);
    }

    private static void Pause(WebDriver driver, int timeInSeconds) {
        driver.manage().timeouts().implicitlyWait(timeInSeconds, TimeUnit.SECONDS);
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
        WebElement searchBoxPlugin = driver.findElement(By.id("search-plugins"));
        searchBoxPlugin.sendKeys("WP Dark Mode – WordPress Dark Mode Plugin for Improved Accessibility, Dark Theme, Night Mode, and Social Sharing");
        searchBoxPlugin.submit();
    }


}

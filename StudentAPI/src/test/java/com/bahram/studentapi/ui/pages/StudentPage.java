package com.bahram.studentapi.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for index.html. Tests call these methods instead of using locators directly,
 * so if the page changes only this class needs to be updated.
 */
public class StudentPage {

    private static final By NAME = By.cssSelector("[data-testid='name-input']");
    private static final By AGE = By.cssSelector("[data-testid='age-input']");
    private static final By GENDER = By.cssSelector("[data-testid='gender-input']");
    private static final By MAJOR = By.cssSelector("[data-testid='major-input']");
    private static final By EMAIL = By.cssSelector("[data-testid='email-input']");
    private static final By SUBMIT = By.cssSelector("[data-testid='submit-button']");
    private static final By ERROR = By.cssSelector("[data-testid='error-message']");
    private static final By ROWS = By.cssSelector("[data-testid='student-rows']");
    private static final By ROW = By.cssSelector("[data-testid='student-row']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public StudentPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** Opens the page and waits until the student table has been loaded from the API. */
    public StudentPage open() {
        driver.get(baseUrl + "/index.html");
        wait.until(ExpectedConditions.attributeToBe(ROWS, "data-loaded", "true"));
        return this;
    }

    public void addStudent(String name, String age, String gender, String major, String email) {
        type(NAME, name);
        type(AGE, age);
        type(GENDER, gender);
        type(MAJOR, major);
        type(EMAIL, email);
        driver.findElement(SUBMIT).click();
    }

    public List<WebElement> rows() {
        return driver.findElements(ROW);
    }

    /** Waits until a table row contains the given text, e.g. a student's email. */
    public void waitForRowContaining(String text) {
        wait.until(d -> rows().stream().anyMatch(r -> r.getText().contains(text)));
    }

    /** Waits until the error area is visible and returns its text. */
    public String errorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }

    private void type(By locator, String value) {
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }
}

package com.gvp.com.HibernateCRUDApp2;

import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

public class ProductSearchAutomation {

    private static final String TARGET_URL = "https://www.amazon.in/";
    private static final String SEARCH_QUERY = "laptop";
    private static final int MIN_PRICE = 2000;
    private static final char BRAND_STARTS_WITH = 'C';
    private static final int MIN_RATING = 4;
    private static final LocalTime START_TIME = LocalTime.of(15, 0);
    private static final LocalTime END_TIME = LocalTime.of(18, 0);   

    public static void main(String[] args) {
        LocalTime currentTime = LocalTime.now();

        if (!isWithinTimeWindow(currentTime)) {
            System.out.printf("Automation allowed only between %s to %s. Current time: %s%n",
                    START_TIME, END_TIME, currentTime);
            return;
        }

        System.out.println("Starting automation at: " + currentTime);

        WebDriver driver = null;
        try {
            driver = setupWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            navigateToAmazon(driver);
            searchForProduct(driver, wait, SEARCH_QUERY);
            applyPriceFilter(driver, wait, MIN_PRICE);
            applyRatingFilter(driver, wait, MIN_RATING);
            displayFilteredResults(driver, wait, 5);
        } catch (Exception e) {
            System.err.println("Critical error during automation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
                System.out.println("\nAutomation complete.");
            }
        }
    }

    private static WebDriver setupWebDriver() {
        System.setProperty("webdriver.chrome.silentOutput", "true"); 
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--ignore-certificate-errors");
        
        
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.9999.99 Safari/537.36");
        
      
        
        System.out.println("Initializing Chrome WebDriver...");
        return new ChromeDriver(options);
    }

    private static boolean isWithinTimeWindow(LocalTime currentTime) {
        return !currentTime.isBefore(START_TIME) && !currentTime.isAfter(END_TIME);
    }

    private static void navigateToAmazon(WebDriver driver) {
        System.out.println("Navigating to Amazon...");
        driver.get(TARGET_URL);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        if (driver.getTitle().contains("Amazon")) {
            System.out.println("Successfully loaded Amazon website");
        } else {
            System.out.println("Warning: Page title doesn't contain 'Amazon'. Title: " + driver.getTitle());
        }
    }

    private static void searchForProduct(WebDriver driver, WebDriverWait wait, String query) {
        try {
            System.out.println("Looking for search box...");
            WebElement searchBox = null;
            
            try {
                searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("twotabsearchtextbox")));
            } catch (TimeoutException e) {
                System.out.println("Could not find search box by ID. Trying alternate selectors...");
                try {
                    searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//input[@type='text' and @placeholder='Search Amazon.in']")));
                } catch (TimeoutException e2) {
                    // Try JavaScript as last resort
                    System.out.println("Using JavaScript to find search box...");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    searchBox = (WebElement) js.executeScript(
                            "return document.querySelector('input[type=\"text\"][placeholder*=\"Search\"]');");
                }
            }
            
            if (searchBox == null) {
                throw new NoSuchElementException("Could not locate search box with any method");
            }
            
            searchBox.clear();
            searchBox.sendKeys(query);
            System.out.println("Submitting search query: " + query);
            searchBox.submit();
            
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.s-main-slot")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".s-result-list")),
                    ExpectedConditions.titleContains(query)
                ));
                System.out.println("Search results loaded for: " + query);
            } catch (TimeoutException e) {
                System.out.println("Timed out waiting for search results, but continuing...");
            }
          
            sleep(2000);
            
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
        }
    }

    private static void applyPriceFilter(WebDriver driver, WebDriverWait wait, int minPrice) {
        System.out.println("Attempting to apply price filter (> ₹" + minPrice + ")");
        try {
            
            try {
                
                WebElement minPriceInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("low-price")));
                minPriceInput.clear();
                minPriceInput.sendKeys(String.valueOf(minPrice));
                
                WebElement priceGo = driver.findElement(By.xpath("//input[@class='a-button-input' and @type='submit']"));
                priceGo.click();
            } catch (Exception e1) {
                System.out.println("Standard price filter failed, trying alternative method...");
                
                try {
                    
                    WebElement minPriceInput = driver.findElement(By.xpath("//input[contains(@placeholder,'Min')]"));
                    minPriceInput.clear();
                    minPriceInput.sendKeys(String.valueOf(minPrice));
                    
                    WebElement priceGo = driver.findElement(By.xpath("//span[contains(text(),'Go')]/parent::*"));
                    priceGo.click();
                } catch (Exception e2) {
                    
                    System.out.println("Using JavaScript to apply price filter...");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript(
                        "let inputs = document.querySelectorAll('input[placeholder*=\"Min\"]');" +
                        "if(inputs.length > 0) {" +
                        "  inputs[0].value = '" + minPrice + "';" +
                        "  let button = document.querySelector('span:contains(\"Go\")').parentElement;" +
                        "  if(button) button.click();" +
                        "}"
                    );
                }
            }
            
            sleep(3000);
            System.out.println("Price filter applied successfully");
            
        } catch (Exception e) {
            System.err.println("All attempts to apply price filter failed: " + e.getMessage());
        }
    }

    private static void applyRatingFilter(WebDriver driver, WebDriverWait wait, int minRating) {
        System.out.println("Attempting to apply rating filter (" + minRating + " stars & up)");
        try {
            
            boolean filterApplied = false;
            
            
            try {
                List<WebElement> ratingOptions = driver.findElements(
                    By.xpath("//section[contains(@aria-label,'stars')]//i[contains(@class,'star')]"));
                
                if (!ratingOptions.isEmpty()) {
                    // Find option closest to our desired rating
                    for (WebElement option : ratingOptions) {
                        String ariaLabel = option.getAttribute("aria-label");
                        if (ariaLabel != null && ariaLabel.contains(minRating + " Stars")) {
                            System.out.println("Found rating element: " + ariaLabel);
                            scrollToElement(driver, option);
                            option.click();
                            filterApplied = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("First rating filter approach failed: " + e.getMessage());
            }
            
            
            if (!filterApplied) {
                try {
                    List<WebElement> textOptions = driver.findElements(
                        By.xpath("//span[contains(text(),'" + minRating + " Stars & Up')]"));
                    
                    if (!textOptions.isEmpty()) {
                        WebElement ratingElement = textOptions.get(0);
                        scrollToElement(driver, ratingElement);
                        ratingElement.click();
                        filterApplied = true;
                    }
                } catch (Exception e) {
                    System.out.println("Second rating filter approach failed: " + e.getMessage());
                }
            }
            
            if (!filterApplied) {
                try {
                    System.out.println("Using JavaScript to apply rating filter...");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript(
                        "let ratings = document.querySelectorAll('*');" +
                        "for(let i=0; i<ratings.length; i++) {" +
                        "  let el = ratings[i];" +
                        "  if(el.textContent && el.textContent.includes('" + minRating + " Stars & Up')) {" +
                        "    el.click();" +
                        "    return true;" +
                        "  }" +
                        "}" +
                        "return false;"
                    );
                    filterApplied = true;
                } catch (Exception e) {
                    System.out.println("JavaScript rating filter approach failed: " + e.getMessage());
                }
            }
            
            if (filterApplied) {
               
                sleep(3000);
                System.out.println("Rating filter applied successfully");
            } else {
                System.out.println("Could not apply rating filter with any method");
            }
            
        } catch (Exception e) {
            System.err.println("Error applying rating filter: " + e.getMessage());
        }
    }

    private static void displayFilteredResults(WebDriver driver, WebDriverWait wait, int maxResults) {
        System.out.println("\nGathering product information...");
        try {
            
            sleep(2000);
            scrollPage(driver);
            
            
            List<WebElement> productTitles = null;
            try {
                productTitles = driver.findElements(By.cssSelector("h2 a span"));
                if (productTitles.isEmpty()) {
                    productTitles = driver.findElements(By.cssSelector(".a-size-medium.a-color-base"));
                }
                if (productTitles.isEmpty()) {
                    productTitles = driver.findElements(By.cssSelector("[data-cy='title-recipe'] h2"));
                }
            } catch (Exception e) {
                System.out.println("Standard methods to find products failed, trying JavaScript...");
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript(
                    "return Array.from(document.querySelectorAll('h2, .a-size-medium')).map(el => { " +
                    "return {element: el, text: el.textContent.trim()};" +
                    "});"
                );
            }
            
            if (productTitles == null || productTitles.isEmpty()) {
                System.out.println("No product listings found on the page");
                // Capture page source for debugging
                System.out.println("Page title: " + driver.getTitle());
                return;
            }
            
            System.out.println("Found " + productTitles.size() + " total products on page");
            
            
            List<WebElement> filteredTitles = productTitles.stream()
                    .filter(e -> {
                        try {
                            String text = e.getText();
                            return text != null && !text.isEmpty() && 
                                text.toUpperCase().startsWith(String.valueOf(BRAND_STARTS_WITH).toUpperCase());
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            System.out.println("\nProducts starting with '" + BRAND_STARTS_WITH + "' (Top " + 
                    Math.min(maxResults, filteredTitles.size()) + "):");
            System.out.println("---------------------------------------------------");

            if (filteredTitles.isEmpty()) {
                System.out.println("No products found starting with '" + BRAND_STARTS_WITH + "'. Showing other results instead:");
                System.out.println("---------------------------------------------------");
                
                
                List<WebElement> displayTitles = productTitles.size() > maxResults ? 
                        productTitles.subList(0, maxResults) : productTitles;
                
                for (int i = 0; i < displayTitles.size(); i++) {
                    String title = displayTitles.get(i).getText();
                   
                    String price = "Price not available";
                    try {
                        
                        WebElement container = getParentContainer(driver, displayTitles.get(i));
                        if (container != null) {
                            List<WebElement> priceElements = container.findElements(
                                By.cssSelector(".a-price-whole, .a-color-price"));
                            if (!priceElements.isEmpty()) {
                                price = priceElements.get(0).getText().replaceAll("[^0-9.,]", "");
                            }
                        }
                    } catch (Exception e) {
                       
                    }
                    
                    System.out.printf("%d. %-70s ₹%s%n", (i + 1),
                            title.length() > 70 ? title.substring(0, 67) + "..." : title,
                            price);
                }
            } else {
                
                for (int i = 0; i < Math.min(maxResults, filteredTitles.size()); i++) {
                    String title = filteredTitles.get(i).getText();
                   
                    String price = "Price not available";
                    try {
                       
                        WebElement container = getParentContainer(driver, filteredTitles.get(i));
                        if (container != null) {
                            List<WebElement> priceElements = container.findElements(
                                By.cssSelector(".a-price-whole, .a-color-price"));
                            if (!priceElements.isEmpty()) {
                                price = priceElements.get(0).getText().replaceAll("[^0-9.,]", "");
                            }
                        }
                    } catch (Exception e) {
                        
                    }
                    
                    System.out.printf("%d. %-70s ₹%s%n", (i + 1),
                            title.length() > 70 ? title.substring(0, 67) + "..." : title,
                            price);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to display results: " + e.getMessage());
        }
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void scrollPage(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollBy(0, 500)");
                sleep(500);
            }
            // Scroll back to top
            js.executeScript("window.scrollTo(0, 0)");
        } catch (Exception e) {
            System.out.println("Error while scrolling: " + e.getMessage());
        }
    }
    
    private static void scrollToElement(WebDriver driver, WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            sleep(500); 
        } catch (Exception e) {
            System.out.println("Error scrolling to element: " + e.getMessage());
        }
    }
    
    private static WebElement getParentContainer(WebDriver driver, WebElement element) {
        try {
            
            WebElement container = null;
            
            
            try {
                container = element.findElement(By.xpath("./ancestor::div[contains(@class, 's-result-item') or contains(@class, 'sg-col-')]"));
            } catch (NoSuchElementException e) {
                // Second try - use JavaScript
                JavascriptExecutor js = (JavascriptExecutor) driver;
                container = (WebElement) js.executeScript(
                    "function getProductContainer(el) {" +
                    "  let current = el;" +
                    "  for (let i = 0; i < 5; i++) {" +
                    "    if (!current) return null;" +
                    "    if (current.classList && " +
                    "        (current.classList.contains('s-result-item') || " +
                    "         current.classList.contains('sg-col'))) {" +
                    "      return current;" +
                    "    }" +
                    "    current = current.parentElement;" +
                    "  }" +
                    "  return current; // Return the 5th ancestor even if no match" +
                    "}" +
                    "return getProductContainer(arguments[0]);", 
                    element);
            }
            
            return container;
        } catch (Exception e) {
            System.out.println("Error finding parent container: " + e.getMessage());
            return null;
        }
    }
}
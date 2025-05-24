package com.gvp.com.HibernateCRUDApp2;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.remote.CapabilityType;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoginProfileValidation {

    public static void main(String[] args) {
        
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(15, 0);

        if (now.isBefore(start) || now.isAfter(end)) {
            System.out.println("Test can only run between 12 PM and 3 PM. Current time: " + now);
            return;
        }

        WebDriver driver = null;
        Scanner scanner = new Scanner(System.in);

        try {
         
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.7103.49 Safari/537.36");
            options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
            options.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
            
         
            System.setProperty("webdriver.chrome.silentOutput", "true");
            System.setProperty("webdriver.chrome.verboseLogging", "false");
            
            System.out.println("Starting test - initializing browser...");
            
            
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            
            System.out.println("📌 IMPORTANT INSTRUCTIONS 📌");
            System.out.println("Due to Amazon's security measures, this test requires manual assistance.");
            System.out.println("The browser will open to Amazon's homepage.");
            System.out.println("Please follow these steps:");
            System.out.println("1. Log in manually to your Amazon account");
            System.out.println("2. Once logged in, navigate to your account page");
            System.out.println("3. Return to this console and press Enter");
            System.out.println("The test will then continue automatically to validate your profile.\n");
            
            
            System.out.println("Step 1: Opening Amazon homepage...");
            driver.get("https://www.amazon.in/");
            
            System.out.println("\n Please log in manually and press Enter when ready...");
            scanner.nextLine();
            
            System.out.println("Continuing test...");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Current page title: " + driver.getTitle());
            
            
            takeScreenshot(driver, "after_manual_login");
            
           
            boolean isLoggedIn = false;
            String profileName = "";
            
         
            try {
                WebElement accountElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("nav-link-accountList"))
                );
                
                String accountText = accountElement.getText();
                System.out.println("Account element text: " + accountText);
                
                if (accountText.contains("Hello,") || accountText.contains("Sign Out")) {
                    isLoggedIn = true;
                    
                  
                    if (accountText.contains("Hello,")) {
                        String[] parts = accountText.split("Hello,");
                        if (parts.length > 1) {
                            profileName = parts[1].trim().split("\\s+")[0].trim();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not find account list element: " + e.getMessage());
            }
            
            
            if (!isLoggedIn || profileName.isEmpty()) {
                try {
                    WebElement nameElement = driver.findElement(By.id("nav-link-accountList-nav-line-1"));
                    String nameText = nameElement.getText();
                    System.out.println("Nav line text: " + nameText);
                    
                    if (nameText.contains("Hello,")) {
                        isLoggedIn = true;
                        profileName = nameText.replace("Hello,", "").trim();
                    }
                } catch (Exception e) {
                    System.out.println("Could not find nav-line-1 element: " + e.getMessage());
                }
            }
            
            
            if (!isLoggedIn || profileName.isEmpty()) {
                try {
                    
                    WebElement accountMenu = driver.findElement(By.id("nav-link-accountList"));
                    accountMenu.click();
                    
                    Thread.sleep(1000); 
                    WebElement accountInfo = driver.findElement(By.cssSelector(".nav-panel .nav-panel-name"));
                    String accountInfoText = accountInfo.getText();
                    System.out.println("Account info text: " + accountInfoText);
                    
                    isLoggedIn = true;
                    profileName = accountInfoText.trim();
                    
                } catch (Exception e) {
                    System.out.println("Could not find account info in dropdown: " + e.getMessage());
                }
            }
            
            if (isLoggedIn) {
                System.out.println("\n✅ User is logged in successfully!");
                
                if (!profileName.isEmpty()) {
                    System.out.println("Profile Name: " + profileName);
                    
                    Pattern forbiddenPattern = Pattern.compile("[ACGILK]");
                    if (forbiddenPattern.matcher(profileName).find()) {
                        System.out.println(" Validation FAILED: Profile name contains forbidden characters (A, C, G, I, L, K).");
                    } else {
                        System.out.println(" Validation PASSED: Profile name is valid (no forbidden characters).");
                    }
                } else {
                    System.out.println("Could not extract profile name, but user appears to be logged in.");
                }
            } else {
                System.out.println("\nCould not verify if user is logged in.");
                System.out.println("Please ensure you completed the login process before pressing Enter.");
            }
            
        } catch (Exception e) {
            System.out.println("\n An unexpected error occurred:");
            e.printStackTrace();
            
           
            if (driver != null) {
                takeScreenshot(driver, "error_screenshot");
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            
            if (driver != null) {
                System.out.println("\nClosing browser...");
                driver.quit();
            }
            
            System.out.println("\n Test completed.");
        }
    }
    
    private static void takeScreenshot(WebDriver driver, String fileNamePrefix) {
        try {
            byte[] screenshotBytes = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            Path path = Paths.get(fileNamePrefix + "_" + System.currentTimeMillis() + ".png");
            Files.write(path, screenshotBytes);
            System.out.println(" Screenshot saved to: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        }
    }
}
package com.gvp.com.HibernateCRUDApp2;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ChromeDriverTest {

    public static void main(String[] args) {
        try {
            
            WebDriverManager.chromedriver().setup();

            
            WebDriver driver = new ChromeDriver();

            
            driver.get("https://www.google.com");

            System.out.println("Opened Google successfully!");

           
            Thread.sleep(2000); 
            driver.quit();  

        } catch (Exception e) {
            
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.example.Selenium.Sandro;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/web/male")
public class SeleniumSandro {

    @GetMapping("/signIn")
    public ResponseEntity<?> signIn(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "E:\\CongViec\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.addArguments("--blink-settings=imagesEnabled=false");

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://sandro.com.vn/customer/account/login/referer/aHR0cHM6Ly9zYW5kcm8uY29tLnZuL2N1c3RvbWVyL2FjY291bnQvaW5kZXgv/");

        driver.findElement(By.xpath("(//input[@id='email'])[1]")).sendKeys("lel501245@gmail.com");
        driver.findElement(By.xpath("(//input[@id='pass'])[1]")).sendKeys("Lethile19@.");
        driver.findElement(By.xpath("(//span[contains(text(),'Đăng nhập')])[2]")).click();

        driver.close();

        return ResponseEntity.ok(new String("Logged in successfully"));
    }
}

package com.example.Selenium.Package02;

import com.example.Selenium.Package03.CaptchaSolove_bot;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.example.Selenium.Package02.CheckFileName.flag_checkFileName;
import static com.example.Selenium.Package02.CheckText.flag_checkText;
import static com.example.Selenium.Package02.CheckText.notification;

@RestController
@RequestMapping("/api/web")
public class Selenium {


    @GetMapping("/ttsfree_captcha_noForLoop_thread2")
    public ResponseEntity<?> ttsfree_captcha_noForLoop_Threads(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        WebDriverWait wait;
        List<WebElement> element_solve;
        String user_name = "nam02test"; // mô phỏng tên user
        String user_password = "IUtrangmaimai02"; // mô phỏng password user
        JavascriptExecutor js;
        WebElement webElement;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "D:\\New folder\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false); // disable chrome running as automation
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // disable chrome running as automation
        options.addArguments("--blink-settings=imagesEnabled=false"); // block tất cả hình ảnh -> tăng tốc độ load website

        WebDriver driver = new ChromeDriver(options);

        CountDownLatch latch = new CountDownLatch(2);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // số giây mà 1 driver chờ để load 1 phần tử nếu không có thiết lập của wait
        driver.manage().window().maximize();

        System.out.println("-----------------------------\n" + params.get("Text") + " " + params.get("Voice") + " " + params.get("FileName"));

//        Thread checkFileName = new Thread(new CheckFileName(params.get("FileName"), latch));
//        Thread checkText = new Thread(new CheckText(params.get("Text"), latch));
//        checkFileName.start();
//        checkText.start();
        //    latch.await();

        if (flag_checkFileName == false) {
            flag_checkFileName = true;
            driver.close();
            return ResponseEntity.ok(new String("Tên File bị trùng trong dữ liệu của bạn , hãy đổi tên khác hoặc xóa file cũ của bạn"));
        }
        if (flag_checkText == false) {
            flag_checkText = true;
            driver.close();
            return ResponseEntity.ok(new String(notification));
        }

        driver.get("https://ttsfree.com/login");

        driver.findElement(By.xpath("//input[@placeholder='Username']")).sendKeys(user_name);
        driver.findElement(By.xpath("//input[@placeholder='Enter password']")).sendKeys(user_password);

        latch = new CountDownLatch(2); // thiết lập 2 Thread ( trường hợp sau khi send key password sẽ có 1 trong 2 hiển thị nên thiết lập 2 thread kiểm tra cùng 1 lúc )

        Thread threadCheckESC = new Thread(new CheckESC(driver, latch, null));
        Thread threadCheckHandAD = new Thread(new CheckHandAD(driver, latch, null));

        threadCheckESC.start();
        threadCheckHandAD.start();

        latch.await();

        driver.findElement(By.xpath("//ins[@class='iCheck-helper']")).click();
        driver.findElement(By.xpath("//input[@id='btnLogin']")).click();


        element_solve = driver.findElements(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            webElement = driver.findElement(By.xpath("//*[@id=\"frm_login\"]/div[2]/div/font"));
            String notification = webElement.getText();
            driver.close();
            return ResponseEntity.ok(new String(notification));
        } else {
            driver.get("https://ttsfree.com/vn"); //Chuyển vùng sang việt nam ( né được những bước không cần thiết như tùy chỉnh giọng nói theo nước )
        }

        js = (JavascriptExecutor) driver;

        webElement = driver.findElement(By.xpath("//*[@id=\"input_text\"]"));
        js.executeScript("arguments[0].scrollIntoView();", webElement);

        driver.findElement(By.xpath("//textarea[@id='input_text']")).sendKeys(params.get("Text"));

        if (params.get("Voice").equals("Female")) {
            driver.findElement(
                            By.xpath("//div[@id='voice_name_bin']//div[@class='form-check icheck-info text-left item_voice item_voice_selected']"))
                    .click();
        } else if (params.get("Voice").equals("Male")) {
            driver.findElement(
                            By.xpath("//div[@class='form-check icheck-info text-left item_voice']"))
                    .click();
        }

        driver.findElement(By.xpath("//a[contains(text(),'Tạo Voice')]")).click();

        /**
         * sau khi bấm nút tạo voice , sẽ có 2 quảng cáo làm che mất các element cần phải thao tác xuất hiện cùng 1 lúc -> giải quyết bằng cách
         * tạo 2 thread 1 lúc cùng bấm sẽ không chính xác vì cả 2 cùng bấm mà 1 trong 2 chưa tắt sẽ bấm đè lên nhau
         * nên giải quyết bằng cách bấm từng thằng 1
         */
        latch = new CountDownLatch(1);
        Thread threadCheckAdsTOP_ESC = new Thread(new CheckAdsTOP_ESC(driver, latch, null));
        threadCheckAdsTOP_ESC.start();
        latch.await();

        try {
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alert alert-danger alert-dismissable']"))).isDisplayed();
            System.out.println("displayed captcha");

            driver.close();
            System.out.println("driver closed");
            test(params);
            System.out.println("driver done yet");

//            SaveCaptcha_Image saveCaptchaImage = new SaveCaptcha_Image(driver, webElement, "D:\\New folder\\Captcha\\", "captcha.png");
//            saveCaptchaImage.getCaptcha();
//
//            CaptchaSolove_bot captchaSoloveBot = new CaptchaSolove_bot();
//            captchaSoloveBot.SendPhoto();

            System.out.println("done");
            Thread.sleep(2000000);

        } catch (Exception e) {
            driver.findElement(By.xpath("//*[@id=\"progessResults\"]/div[2]/center[1]/div/a")).click(); // nút tải xuống
        }

        latch = new CountDownLatch(2);
        Thread threadCheckHostAD = new Thread(new CheckHostAD(driver, latch));
        Thread threadCheckAdSpecial = new Thread(new CheckAdSpecial(driver, latch));
        threadCheckHostAD.start();
        threadCheckAdSpecial.start();
        latch.await();

        driver.close();

        /**
         * đổi tên file theo yêu cầu user ( đơn luồng thì hoạt động oke , đa luồng thì lỗi -> đang nghiên cứu login 1 lúc có request cùng đổi để đảm bảo không có lỗi xảy ra
         * đang nghiên cứu để update
         */
//        File folder = new File("E:\\New folder");
//        File[] files = folder.listFiles();
//        if (files != null && files.length > 0) {
//            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
//            File latestFile = files[0];
//            System.out.println(latestFile.getName());
//            String newFileName = params.get("FileName") + ".mp3";
//            File newFile = new File(folder, newFileName);
//            latestFile.renameTo(newFile);
//        }

        return ResponseEntity.ok(new String("Downloaded successfully"));
    }


    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam Map<String, String> params) throws InterruptedException, IOException {
        WebDriverWait wait;
        List<WebElement> element_solve;
        String user_name = "nam02test"; // mô phỏng tên user
        String user_password = "IUtrangmaimai02"; // mô phỏng password user
        JavascriptExecutor js;
        WebElement webElement;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("webdriver.chrome.driver", "D:\\New folder\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false); // disable chrome running as automation
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // disable chrome running as automation

        WebDriver driver = new ChromeDriver(options);

        CountDownLatch latch;

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // số giây mà 1 driver chờ để load 1 phần tử nếu không có thiết lập của wait
        driver.manage().window().maximize();

//        String blockImagesScript = "var images = document.getElementsByTagName('img'); " +
//                "for (var i = 0; i < images.length; i++) { " +
//                "   images[i].setAttribute('src', ''); " +
//                "}";
//        ((JavascriptExecutor) driver).executeScript(blockImagesScript);
        driver.get("https://ttsfree.com/login");


//        String[] imageURLtoBlock = {
//                "https://ad.plus/adplus-advertising.svg",
//                "https://ttsfree.com/images/lang/us.jpg",
//                "https://ttsfree.com/images/lang/us.jpg",
//                "https://ttsfree.com/images/lang/au.jpg",
//                "https://ttsfree.com/images/lang/fr.jpg",
//                "https://ttsfree.com/images/lang/it.jpg",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/lang/kr.jpg",
//                "https://ttsfree.com/images/lang/ru.jpg",
//                "https://ttsfree.com/images/lang/jp.jpg",
//                "https://ttsfree.com/images/lang/de.jpg",
//                "https://ttsfree.com/images/lang/se.jpg",
//                "https://ttsfree.com/images/lang/nl.jpg",
//                "https://ttsfree.com/images/lang/my.jpg",
//                "https://ttsfree.com/images/lang/id.jpg",
//                "https://ttsfree.com/images/lang/pt.jpg",
//                "https://ttsfree.com/images/lang/ph.jpg",
//                "https://ttsfree.com/images/lang/bg.jpg",
//                "https://ttsfree.com/images/lang/ch.jpg",
//                "https://ttsfree.com/images/lang/us.jpg",
//                "https://ttsfree.com/images/lang/au.jpg",
//                "https://ttsfree.com/images/lang/kr.jpg",
//                "https://ttsfree.com/images/lang/pt.jpg",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/lang/ru.jpg",
//                "https://ttsfree.com/images/lang/ph.jpg",
//                "https://ttsfree.com/images/lang/jp.jpg",
//                "https://sstatic1.histats.com/0.gif?4788790&101",
//                "https://ap.lijit.com/pixel?gdpr=0&gdpr_consent=&redir=https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dsovrn%26uid%3D%24UID",
//                "https://sync.1rx.io/usersync2/rmpssp?sub=adagio&redir=https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dunruly%26uid%3D%5BRX_UUID%5D",
//                "https://ssum-sec.casalemedia.com/usermatchredir?s=194558&cb=https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dindexexchange%26uid%3D",
//                "https://u.openx.net/w/1.0/cm?id=3cc4b2f6-c7e1-439a-8174-b6dbb96bcabf&r=https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dopenx%26uid%3D%7BOPENX_ID%7D",
//                "https://ib.adnxs.com/getuid?https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dappnexus%26uid%3D%24UID",
//                "https://ads.stickyadstv.com/user-matching?id=3656",
//                "https://ice.360yield.com/server_match?partner_id=1790&r=https%3A%2F%2Fu.4dex.io%2Fsetuid%3Fbidder%3Dimprovedigital%26uid%3D%7BPUB_USER_ID%7D",
//                "https://ttsfree.com/images/logo.adblock.png?t=new2"
//        };
//
//        for (String imageUrl : imageURLtoBlock) {
//            ((JavascriptExecutor) driver).executeScript("var images = document.getElementsByTagName('img'); for (var i = 0; i < images.length; i++) { if (images[i].src === '" + imageUrl + "') { images[i].style.display = 'none'; } }");
//        }

        driver.findElement(By.xpath("//input[@placeholder='Username']")).sendKeys(user_name);
        driver.findElement(By.xpath("//input[@placeholder='Enter password']")).sendKeys(user_password);

        latch = new CountDownLatch(2); // thiết lập 2 Thread ( trường hợp sau khi send key password sẽ có 1 trong 2 hiển thị nên thiết lập 2 thread kiểm tra cùng 1 lúc )

        Thread threadCheckESC = new Thread(new CheckESC(driver, latch, null));
        Thread threadCheckHandAD = new Thread(new CheckHandAD(driver, latch, null));
        threadCheckESC.start();
        threadCheckHandAD.start();

        latch.await();

        driver.findElement(By.xpath("//ins[@class='iCheck-helper']")).click();
        driver.findElement(By.xpath("//input[@id='btnLogin']")).click();
        Thread.sleep(2000);

        driver.get("https://ttsfree.com/vn"); //Chuyển vùng sang việt nam ( né được những bước không cần thiết như tùy chỉnh giọng nói theo nước )

//        String[] imageURLtoBlock1 = {
//                "https://ad.plus/adplus-advertising.svg",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/lang/us.jpg",
//                "https://ttsfree.com/images/lang/au.jpg",
//                "https://ttsfree.com/images/lang/fr.jpg",
//                "https://ttsfree.com/images/lang/it.jpg",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/lang/kr.jpg",
//                "https://ttsfree.com/images/lang/ru.jpg",
//                "https://ttsfree.com/images/lang/jp.jpg",
//                "https://ttsfree.com/images/lang/de.jpg",
//                "https://ttsfree.com/images/lang/se.jpg",
//                "https://ttsfree.com/images/lang/nl.jpg",
//                "https://ttsfree.com/images/lang/my.jpg",
//                "https://ttsfree.com/images/lang/id.jpg",
//                "https://ttsfree.com/images/lang/pt.jpg",
//                "https://ttsfree.com/images/lang/ph.jpg",
//                "https://ttsfree.com/images/lang/bg.jpg",
//                "https://ttsfree.com/images/lang/ch.jpg",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/Male.png",
//                "https://ads.stickyadstv.com/auto-user-sync?_fw_gdpr=0&_fw_gdpr_consent=",
//                "https://ads.stickyadstv.com/user-matching?id=2545&_fw_gdpr=0&_fw_gdpr_consent=",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/Female.png",
//                "https://ttsfree.com/images/lang/vn.jpg",
//                "https://ttsfree.com/images/Male.png",
//                "https://ttsfree.com/images/lang/vn.jpg",
//        };
//
//        for (String imageUrl : imageURLtoBlock1) {
//            ((JavascriptExecutor) driver).executeScript("var images = document.getElementsByTagName('img'); for (var i = 0; i < images.length; i++) { if (images[i].src === '" + imageUrl + "') { images[i].style.display = 'none'; } }");
//        }

        js = (JavascriptExecutor) driver;

        webElement = driver.findElement(By.xpath("//*[@id=\"input_text\"]"));
        js.executeScript("arguments[0].scrollIntoView();", webElement);

//        Thread.sleep(3000);
//
//        latch = new CountDownLatch(1);
//        Thread threadCheckAdsTOP_ESC = new Thread(new CheckAdsTOP_ESC(driver, latch));
//        threadCheckAdsTOP_ESC.start();
//
//        latch.await();
//
//        Thread.sleep(3000);
//
//        latch = new CountDownLatch(1);
//        threadCheckESC = new Thread(new CheckESC(driver, latch));
//        threadCheckESC.start();
//
//        latch.await();
//
//        Thread.sleep(3000);
//
//        latch = new CountDownLatch(1);
//        threadCheckHandAD = new Thread(new CheckHandAD(driver, latch));
//        threadCheckHandAD.start();
//
//        latch.await();

        Thread.sleep(1000);
        element_solve = driver.findElements(By.xpath("//ins[@data-anchor-status='displayed']"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            driver.findElement(By.xpath("(//div[@class='grippy-host'])[1]")).click();
        }

        Thread.sleep(1000);
        element_solve = driver.findElements(By.xpath("/html/body/div[1]/div[1]/small"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            driver.findElement(By.xpath("/html/body/div[1]/div[1]/small")).click();
        } else {
        }

        Thread.sleep(1000);
        element_solve = driver.findElements(By.xpath("//button[normalize-space()='×']"));
        if (element_solve.size() > 0 && element_solve.get(0).isDisplayed()) {
            driver.findElement(By.xpath("//button[normalize-space()='×']")).click();
        } else {
        }

        driver.findElement(By.xpath("//textarea[@id='input_text']")).sendKeys("test");

        driver.findElement(By.xpath("//a[contains(text(),'Tạo Voice')]")).click();


        try {
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alert alert-danger alert-dismissable']"))).isDisplayed();
            System.out.println("displayed captcha");
            SaveCaptcha_Image saveCaptchaImage = new SaveCaptcha_Image(driver, webElement, "D:\\New folder\\Captcha\\", "captcha.png");
            saveCaptchaImage.getCaptcha();
            CaptchaSolove_bot captchaSoloveBot = new CaptchaSolove_bot();
            captchaSoloveBot.SendPhoto();

            System.out.println("done image");


            try {

                int countdownDuration = 30;

                for (int second = 0; second <= countdownDuration; second++) {
                    System.out.println(countdownDuration - second);
                    Boolean flag = false;
                    String text = null;
                    Update update = new Update();
                    captchaSoloveBot.getTextUser_Reply(update);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(1000000);
            } catch (Exception e) {
                System.out.println(e);
                Thread.sleep(1000000);
            }


//            if (flag = false) {
//                return ResponseEntity.ok(new String("Text not match"));
//            } else if (flag == true) {
//                driver.findElement(By.xpath("//input[@id='captcha_input']")).sendKeys(text);
//            }


        } catch (Exception e) {
            driver.findElement(By.xpath("//*[@id=\"progessResults\"]/div[2]/center[1]/div/a")).click(); // nút tải xuống
        }
        driver.close();


        return ResponseEntity.ok(new String("Downloaded successfully"));
    }

    @GetMapping("/old")
    public ResponseEntity<?> old(@RequestParam Map<String, String> params) throws InterruptedException, IOException, TelegramApiException {
        WebDriverWait wait;
        List<WebElement> element_solve;
        String user_name = "nam02test"; // mô phỏng tên user
        String user_password = "IUtrangmaimai02"; // mô phỏng password user
        JavascriptExecutor js;
        WebElement webElement = null;

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("java.awt.headless", "false");

        //chrome.exe --remote-debugging-port=9222 --user-data-dir="E:\CongViecHocTap\ChromeData"
        System.setProperty("webdriver.chrome.driver", "E:\\CongViecHocTap\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");
        options.addArguments("disable-infobars");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-extensions");

        WebDriver driver = new ChromeDriver(options);

        System.out.println("oke");

        SaveCaptcha_Image saveCaptchaImage = new SaveCaptcha_Image(driver, webElement, "E:\\New folder\\", "captcha.png");
        saveCaptchaImage.getCaptcha();

        CaptchaSolove_bot captchaSoloveBot = new CaptchaSolove_bot();
        captchaSoloveBot.SendPhoto();

        return ResponseEntity.ok(new String("Downloaded successfully"));
    }

    private static void saveImage(File imageFile, String folderPath, String fileName) {
        try {
            File destinationFolder = new File(folderPath);

            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            File destinationFile = new File(destinationFolder, fileName);

            FileUtils.copyFile(imageFile, destinationFile);

            System.out.println("Đã lưu trữ ảnh thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lưu trữ ảnh.");
        }
    }
}

/**
 * cd C:\Program Files\Google\Chrome\Application
 * chrome.exe --remote-debugging-port=9222 --user-data-dir="E:\CongViecHocTap\ChromeData"
 * chrome://settings/content/popups
 */

class ka {
    public static void main(String[] args) throws InterruptedException {
        int countdownDuration = 30; // Độ dài bộ đếm trong giây

        for (int second = 0; second <= countdownDuration; second++) {
            System.out.println(countdownDuration - second);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Bộ đếm đã kết thúc!");
    }
}
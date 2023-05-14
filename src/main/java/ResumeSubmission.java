import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TelegramNotificationBot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author BeamStark
 * Boss直聘自动投递
 * @date 2023-05-01-04:16
 */
@Slf4j
public class ResumeSubmission {
    static boolean EnableNotifications = true;
    static Integer page = 1;
    static Integer maxPage = 10;
    static String loginUrl = "https://www.zhipin.com/web/user/?ka=header-login";
    static String baseUrl = "https://www.zhipin.com/web/geek/job?query=Java&page=";
    static ChromeDriver driver = new ChromeDriver();
    static WebDriverWait wait15s = new WebDriverWait(driver, 15000);
    static List<String> returnList = new ArrayList<>();

    public static void main(String[] args) {
        Date sdate = new Date();
        login();
        for (int i = page; i <= maxPage; i++) {
            log.info("第{}页", i);
            if (resumeSubmission(baseUrl + i) == -1) {
                log.info("今日沟通人数已达上限，请明天再试");
                break;
            }
        }
        Date edate = new Date();
        log.info("共投递{}个简历,用时{}分", returnList.size(),
                ((edate.getTime() - sdate.getTime()) / 1000) / 60);

        if (EnableNotifications) {
            new TelegramNotificationBot().sendMessageWithList("共投递" + returnList.size() + "个简历,用时" + ((edate.getTime() - sdate.getTime()) / 1000) / 60 + "分", returnList, "Boss直聘投递");
        }
        driver.close();
    }

    @SneakyThrows
    private static Integer resumeSubmission(String url) {

        driver.get(url);
        wait15s.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[class*='job-title clearfix']")));

        List<String> list = new ArrayList<>();
        driver.findElements(By.cssSelector("[class*='job-card-left']")).forEach(o -> list.add(o.getAttribute("href")));

        for (String s : list) {
            driver.get(s);

            wait15s.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[class*='btn btn-startchat']")));

            WebElement btn = driver.findElement(By.cssSelector("[class*='btn btn-startchat']"));

            if ("立即沟通".equals(btn.getText())) {
                btn.click();
                wait15s.until(ExpectedConditions.presenceOfElementLocated(
                        By.className("dialog-con")));
                String text = driver.findElement(By.className("dialog-con")).getText();
                if (text.contains("已达上限")) {
                    return -1;
                }
                String com = driver.findElements(By.className("company-info")).get(1).getText();
                String title = driver.findElement(By.className("name")).getText();
                returnList.add(com + " | " + title);
                log.info("投递{}公司 | {}一职", com, title);
                Thread.sleep(1500);
            }
        }
        return returnList.size();
    }

    @SneakyThrows
    private static void login() {
        driver.get(loginUrl);
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='btn" +
                "-sign-switch ewm-switch']")));
        driver.findElement(By.cssSelector("[class*='btn-sign-switch ewm-switch']")).click();
        log.info("等待登陆..");
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.className("resume-catalogue")));
    }
}

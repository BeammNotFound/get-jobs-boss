import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TelegramNotificationBot;

/**
 * @author BeamStark
 * Boss直聘自动投递
 * @date 2023-05-01-04:16
 */
@Slf4j
public class ResumeSubmission {
    static {
        System.setProperty("webdriver.chrome.driver", "/Users/xiaxuchen/chromedriver-mac-arm64/chromedriver");
    }

    static boolean EnableNotifications = true;
    static Integer page = 1;
    static Integer maxPage = 3;
    static String loginUrl = "https://www.zhipin.com/web/user/?ka=header-login";
    static String[] recruitUrl = new String[] {
//        "https://www.zhipin.com/web/geek/job?city=101210100&experience=102&degree=204,203&position=100101&jobType=1901&salary=406&page=",
        "https://www.zhipin.com/web/geek/job?city=101210100&experience=102&degree=204,203&position=100101&jobType=1901&salary=405&page=",
        "https://www.zhipin.com/web/geek/job?city=101020100&experience=102&degree=204,203&position=100101&jobType=1901&salary=405&page=",
        "https://www.zhipin.com/web/geek/job?city=101020100&experience=102&degree=204,203&position=100101&jobType=1901&salary=406&page=",
        "https://www.zhipin.com/web/geek/job?city=101010100&experience=102&degree=204,203&position=100101&jobType=1901&salary=406&page=",
        "https://www.zhipin.com/web/geek/job?city=101280600&experience=102&degree=204,203&position=100101&jobType=1901&salary=406&page=",
    };
    static ChromeDriver driver = new ChromeDriver();
    static WebDriverWait wait15s = new WebDriverWait(driver, 15000);
    static List<String> returnList = new ArrayList<>();

    static int minKSalary = 14;

    static int minMaxKSalary = 20;

    static int maxKSalary = 40;

    static int maxMinKSalary = 20;

    static int salaryCount = 14;

    static int graduateYear = 2024;

    public static void main(String[] args) {
        Date sdate = new Date();
        login();
        out: for (String baseUrl : recruitUrl) {
            for (int i = page; i <= maxPage; i++) {
                log.info("第{}页", i);
                if (resumeSubmission(baseUrl + i) == -1) {
                    log.info("今日沟通人数已达上限，请明天再试");
                    break out;
                }
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
            try {
                driver.get(s);
                wait15s.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[class*='btn btn-startchat']")));
                WebElement btn = driver.findElement(By.cssSelector("[class*='btn btn-startchat']"));
                List<WebElement> timeCondition = driver.findElements(By.cssSelector("p.school-job-sec span"));
                if (timeCondition.size() == 2) {
                    String graduateYearText = timeCondition.get(0).getText().trim();
                    if (!graduateYearText.startsWith("毕业时间：不限") && !graduateYearText.startsWith("毕业时间：" + graduateYear) && Arrays.stream(graduateYearText.substring("毕业时间：".length()).split("-")).noneMatch(year -> year.startsWith(graduateYear + ""))) {
                        log.warn("【毕业时间不匹配】{}", timeCondition.get(0).getText());
                    }
                    String[] splitEndTime = timeCondition.get(1).getText().trim().split("：");
                    if (splitEndTime.length != 2) {
                        log.warn("【截止日期格式错误】{}", timeCondition.get(1).getText());
                        continue;
                    } else {
                        LocalDateTime endTime = LocalDate.parse(splitEndTime[1], DateTimeFormatter.ofPattern("yyyy.MM.dd")).atStartOfDay();
                        if (endTime.isBefore(LocalDateTime.now())) {
                            log.warn("【截止日期已过期】{}", timeCondition.get(1).getText());
                            continue;
                        }
                    }
                } else {
                    log.warn("无法获取毕业时间及招聘截止时间, {}", String.join("|", timeCondition.stream().map(item -> item.getText()).collect(Collectors.toSet())));
                }

                if ("立即沟通".equals(btn.getText())) {
                    // 校验薪资
                    WebElement salary = driver.findElement(By.className("salary"));
                    List<WebElement> elements = driver.findElements(By.className("company-info"));
                    WebElement titleNode = driver.findElement(By.className("name"));
                    String[] split = salary.getText().split("K")[0].split("-");
                    // 每年多少薪，比如14薪，如果小于阈值，则跳过
                    if (split.length > 1 && Integer.parseInt(split[1].split("薪")[0]) < salaryCount) {
                        log.info("【薪次不匹配】跳过职位，薪资不匹配,{} | {}", elements.size() < 2 ? "未知" : elements.get(1).getText(), titleNode == null ? salary.getText() : titleNode.getText());
                        continue;
                    }
                    // 校验
                    if ((minKSalary > Integer.parseInt(split[0]) || minMaxKSalary < Integer.parseInt(split[0])) && (maxKSalary < Integer.parseInt(split[1]) || maxMinKSalary > Integer.parseInt(split[1]))) {
                        log.info("【月薪不匹配】跳过职位，薪资不匹配,{} | {}", elements.size() < 2 ? "未知" : elements.get(1).getText(), titleNode == null ? salary.getText() : titleNode.getText());
                        continue;
                    }
                    btn.click();
                    wait15s.until(ExpectedConditions.presenceOfElementLocated(
                        By.className("dialog-con")));
                    String text = driver.findElement(By.className("dialog-con")).getText();
                    if (text.contains("已达上限")) {
                        return -1;
                    }
                    if (elements.size() < 2) {
                        Thread.sleep(5000);
                        elements = driver.findElements(By.className("company-info"));
                    }
                    String com = elements.size() < 2 ? "未知" : elements.get(1).getText();
                    String title = titleNode == null ? salary.getText() : titleNode.getText();
                    returnList.add(com + " | " + title);
                    log.info("投递{} | {}一职", com, title);
                    Thread.sleep(1500);
                }
            } catch (Exception e) {
                log.error("投递失败", e);
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
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.className("nav-figure")));
    }
}

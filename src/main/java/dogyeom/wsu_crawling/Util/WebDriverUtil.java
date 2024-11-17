package dogyeom.wsu_crawling.Util;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class WebDriverUtil {

	private static String WEB_DRIVER_PATH;

	@Value("${download.path}")
	public static String DOWNLOAD_PATH;


	public static WebDriver getChromeDriver() {
		if (ObjectUtils.isEmpty(System.getProperty("webdriver.chrome.driver"))) {
			System.setProperty("webdriver.chrome.driver", WEB_DRIVER_PATH);
		}

		// webDriver 옵션 설정
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless");
		chromeOptions.addArguments("--lang=ko");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-dev-shm-usage");
		chromeOptions.addArguments("--disable-gpu");

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", DOWNLOAD_PATH); // 다운로드 폴더 설정
		prefs.put("plugins.always_open_pdf_externally", true); // PDF를 새 탭에서 열지 않고 다운로드
		chromeOptions.setExperimentalOption("prefs", prefs);

		WebDriver driver = new ChromeDriver(chromeOptions);
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

		return driver;
	}

	@Value(value = "#{resource['driver.chrome.driver_path']}")
	public void initDriver(String path) {
		WEB_DRIVER_PATH = path;
	}

	public static void quit(WebDriver driver) {
		if (!ObjectUtils.isEmpty(driver)) {
			driver.quit();
		}
	}

	public static void close(WebDriver driver) {
		if (!ObjectUtils.isEmpty(driver)) {
			driver.close();
		}
	}
}

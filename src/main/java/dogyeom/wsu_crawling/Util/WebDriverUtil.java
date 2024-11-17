package dogyeom.wsu_crawling.Util;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class WebDriverUtil {

	@Value("${driver.chrome.driver_path}")
	private String webDriverPath;

	@Value("${download.path}")
	private String downloadPath;

	@PostConstruct
	public void setupDriverPath() {
		System.setProperty("webdriver.chrome.driver", webDriverPath);
	}

	public WebDriver createWebDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--lang=ko", "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", downloadPath); // 다운로드 폴더 설정
		options.setExperimentalOption("prefs", prefs);

		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

		return driver;
	}

	public void quit(WebDriver driver) {
		if (driver != null) {
			driver.quit();
		}
	}
}

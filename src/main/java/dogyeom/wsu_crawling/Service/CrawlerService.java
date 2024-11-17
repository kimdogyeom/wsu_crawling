package dogyeom.wsu_crawling.Service;

import dogyeom.wsu_crawling.Repository.CrawlerRepository;
import dogyeom.wsu_crawling.Util.WebDriverUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

	private final WebDriverUtil webDriverUtil;

	public void startCrawling(String targetUrl) {
		WebDriver driver = webDriverUtil.createWebDriver();
		try {
			driver.get(targetUrl);

			// 탭 항목 추출
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			List<WebElement> ruleList = wait.until(
				ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//ul[@class='tabStyle tab6']/li")));
			log.info("총 {}개의 항목을 발견했습니다", ruleList.size());

			for (int i = 1; i < ruleList.size(); i++) { // 첫 번째 항목은 제외
				// 매 반복 시 새로 요소 가져오기
				ruleList = driver.findElements(By.xpath("//ul[@class='tabStyle tab6']/li"));
				WebElement tab = ruleList.get(i);
				processTab(driver, tab);
			}
		} catch (Exception e) {
			log.error("크롤링 중 예외 발생: ", e);
		} finally {
			webDriverUtil.quit(driver);
		}
	}

	private void processTab(WebDriver driver, WebElement tab) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			// 링크 추출 및 이동
			String href = tab.findElement(By.tagName("a")).getAttribute("href");
			driver.get(href);

			// 메인 제목 가져오기
			String mainTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article h4.tit01")))
				.getText().replaceAll(" ", "_");

			// 테이블 처리
			List<WebElement> tables = driver.findElements(By.cssSelector("article table.tbl_skin2"));
			for (WebElement table : tables) {
				processTable(table, mainTitle);
			}
		} catch (Exception e) {
			log.error("탭 처리 중 예외 발생: ", e);
		}
	}


	private void processTable(WebElement table, String mainTitle) {
		try {
			String middleTitle = extractMiddleTitle(table);
			String sectionTitle = extractSectionTitle(table);

			String baseFileName = generateFileName(mainTitle, middleTitle, sectionTitle);

			List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
			for (WebElement row : rows) {
				processTableRow(row, baseFileName);
			}
		} catch (Exception e) {
			log.error("테이블 처리 중 예외 발생: ", e);
		}
	}

	private String extractMiddleTitle(WebElement table) {
		List<WebElement> previousSiblings = table.findElements(By.xpath("preceding-sibling::*"));
		for (int i = previousSiblings.size() - 1; i >= 0; i--) {
			WebElement sibling = previousSiblings.get(i);
			if (sibling.getTagName().equals("h5")) {
				return sibling.getText().replaceAll(" ", "_");
			}
		}
		return "";
	}

	private String extractSectionTitle(WebElement table) {
		List<WebElement> previousSiblings = table.findElements(By.xpath("preceding-sibling::*"));
		for (int i = previousSiblings.size() - 1; i >= 0; i--) {
			WebElement sibling = previousSiblings.get(i);
			if (sibling.getTagName().equals("span") && sibling.getAttribute("class").contains("blt_tx")) {
				return sibling.getText().replaceAll(" ", "_");
			}
		}
		return "";
	}

	private String generateFileName(String mainTitle, String middleTitle, String sectionTitle) {
		StringBuilder fileName = new StringBuilder(mainTitle);
		if (!middleTitle.isEmpty()) {
			fileName.append("_").append(middleTitle);
		}
		if (!sectionTitle.isEmpty()) {
			fileName.append("_").append(sectionTitle);
		}
		return fileName.toString();
	}

	private void processTableRow(WebElement row, String baseFileName) {
		try {
			String ruleNumber = row.findElement(By.xpath("td[1]")).getText().replaceAll(" ", "_");
			String ruleName = row.findElement(By.xpath("td[2]")).getText().replaceAll(" ", "_");
			String fileUrl = row.findElement(By.xpath("td[3]/a")).getAttribute("href");

			String fullFileName = baseFileName + "_" + ruleNumber + "_" + ruleName + ".pdf";
			log.info("생성된 파일 이름: {}", fullFileName);
			log.info("다운로드 URL: {}", fileUrl);

			downloadFile(fileUrl, webDriverUtil.getDownloadPath() + "\\" + LocalDate.now() + "\\" + fullFileName);
		} catch (Exception e) {
			log.error("테이블 행 처리 중 예외 발생: ", e);
		}
	}

	private void ensureDirectoryExists(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void downloadFile(String url, String targetPath) {
		ensureDirectoryExists(targetPath.substring(0, targetPath.lastIndexOf("\\")));
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			FileOutputStream out = new FileOutputStream(targetPath)) {
			byte[] buffer = new byte[1024];
			int count;
			while ((count = in.read(buffer, 0, 1024)) != -1) {
				out.write(buffer, 0, count);
			}
			log.info("파일 다운로드 완료: {}", targetPath);
		} catch (IOException e) {
			log.error("파일 다운로드 중 오류 발생: ", e);
		}
	}
}

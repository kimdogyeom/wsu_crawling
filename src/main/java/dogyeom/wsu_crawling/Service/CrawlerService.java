package dogyeom.wsu_crawling.Service;

import static dogyeom.wsu_crawling.Util.WebDriverUtil.DOWNLOAD_PATH;

import dogyeom.wsu_crawling.Repository.CrawlerRepository;
import dogyeom.wsu_crawling.Util.WebDriverUtil;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

	private final CrawlerRepository crawlerRepository;

	WebDriver driver = WebDriverUtil.getChromeDriver();

	// 우송대학교 규정집 사이트
	// https://www.wsu.ac.kr/page/index.jsp?code=intro0402a

	// 크롤링 시작
	public void startCrawling(String targetUrl) {
		try {
			driver.get(targetUrl);

			// 탭 항목 추출
			List<WebElement> ruleList = driver.findElements(By.xpath("//ul[@class='tabStyle tab6']/li"));
			log.info("총 {}개의 항목을 발견했습니다", ruleList.size());

			for (int i = 1; i < ruleList.size(); i++) { // 첫 번째 항목은 제외
				processTab(ruleList.get(i));
			}
		} catch (Exception e) {
			log.error("크롤링 중 예외 발생: ", e);
		} finally {
			WebDriverUtil.quit(driver);
		}
	}

	// 각 탭 처리
	private void processTab(WebElement tab) {
		try {
			String href = tab.findElement(By.tagName("a")).getAttribute("href");
			driver.get(href);

			// 최상위 제목 추출
			String mainTitle = getMainTitle();

			// 테이블 처리
			List<WebElement> tables = driver.findElements(By.cssSelector("article table.tbl_skin2"));
			for (WebElement table : tables) {
				processTable(table, mainTitle);
			}
		} catch (Exception e) {
			log.error("탭 처리 중 예외 발생: ", e);
		}
	}

	// 최상위 제목(h4) 추출
	private String getMainTitle() {
		WebElement mainTitleElement = driver.findElement(By.cssSelector("article h4.tit01"));
		return mainTitleElement.getText().replaceAll(" ", "_");
	}

	// 테이블 처리
	private void processTable(WebElement table, String mainTitle) {
		try {
			// 테이블 제목(중제목, 소제목) 추출
			String middleTitle = "";
			String sectionTitle = "";

			List<WebElement> previousSiblings = table.findElements(By.xpath("preceding-sibling::*"));
			for (int i = previousSiblings.size() - 1; i >= 0; i--) {
				WebElement sibling = previousSiblings.get(i);
				if (sibling.getTagName().equals("span") && sibling.getAttribute("class").contains("blt_tx")) {
					sectionTitle = sibling.getText().replaceAll(" ", "_");
				} else if (sibling.getTagName().equals("h5")) {
					middleTitle = sibling.getText().replaceAll(" ", "_");
					break;
				}
			}

			// 파일명 기본 구조 생성
			String baseFileName = generateFileName(mainTitle, middleTitle, sectionTitle);

			// 테이블 데이터 추출
			List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
			for (WebElement row : rows) {
				processTableRow(row, baseFileName);
			}
		} catch (Exception e) {
			log.error("테이블 처리 중 예외 발생: ", e);
		}
	}

	// 파일명 생성
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

	// 테이블 행(row) 처리
	private void processTableRow(WebElement row, String baseFileName) {
		try {
			// 행 데이터 추출
			String ruleNumber = row.findElement(By.xpath("td[1]")).getText().replaceAll(" ", "_");
			String ruleName = row.findElement(By.xpath("td[2]")).getText().replaceAll(" ", "_");
			String fileUrl = row.findElement(By.xpath("td[3]/a")).getAttribute("href");

			// 최종 파일명 생성
			String fullFileName = baseFileName + "_" + ruleNumber + "_" + ruleName + ".pdf";
			log.info("생성된 파일 이름: {}", fullFileName);
			log.info("다운로드 URL: {}", fileUrl);

			// 파일 다운로드
			downloadFile(fileUrl, DOWNLOAD_PATH + "/" + LocalDate.now() + "/" + fullFileName);
		} catch (Exception e) {
			log.error("테이블 행 처리 중 예외 발생: ", e);
		}
	}

	// 파일 다운로드
	private void downloadFile(String fileUrl, String savePath) {
		try (InputStream inputStream = new URL(fileUrl).openStream();
			FileOutputStream outputStream = new FileOutputStream(savePath)) {

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			log.info("파일 다운로드 완료: {}", savePath);
		} catch (Exception e) {
			log.error("파일 다운로드 중 예외 발생: ", e);
		}
	}
}
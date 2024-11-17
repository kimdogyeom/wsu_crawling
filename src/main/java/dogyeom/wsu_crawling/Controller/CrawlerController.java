package dogyeom.wsu_crawling.Controller;

import dogyeom.wsu_crawling.Service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/crawl")
public class CrawlerController {

	private final CrawlerService crawlerService;


	@PostMapping("/start/{target}")
	public ResponseEntity<?> ruleCrawler(@PathVariable("target") String target) {
		crawlerService.startCrawling(target);

		return ResponseEntity.ok("크롤링 시작");
	}



}



package dogyeom.wsu_crawling.Controller;

import dogyeom.wsu_crawling.Service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/crawl")
public class CrawlerController {

	private final CrawlerService crawlerService;

	@PostMapping("/start")
	public ResponseEntity<String> ruleCrawler(@RequestParam String target) {
		crawlerService.startCrawling(target);

		return ResponseEntity.ok("크롤링 시작");
	}


}



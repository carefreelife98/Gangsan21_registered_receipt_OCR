package carefree.CarefreeOCR.controller;

import carefree.CarefreeOCR.dto.WebCrawlerDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/crawler")
public class WebCrawlerController {

    @Value("${crawling.url}")
    private String url;

    @RequestMapping("/InputForm")
    public String crawlingForm(Model model) {
        return "crawling-form";
    }
//
//    @PostMapping("/saveHomePage")
//    public String saveHomePage(@RequestParam("homeAddress") String homeAddress, Model model) {
//        // 입력받은 홈페이지 주소를 처리하는 메서드
//        // 여기에서 원하는 작업을 수행하세요.
//
//        // 예시: 입력 받은 데이터를 모델에 추가하여 다시 홈페이지로 이동
//        model.addAttribute("homeAddress", homeAddress);
//        return "redirect:/home";
//    }

    @GetMapping("/crawling")
    public String crawling(HttpServletRequest request, Model model){
        List<WebCrawlerDto> list = new ArrayList<>();

        String WEB_DRIVER_ID = "webdriver.chrome.driver";
        String WEB_DRIVER_PATH = "chromedriver-mac-arm64/chromedriver";

        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("headless");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url); // 크롤링할 사이트의 url
            for(WebElement element : driver.findElements(By.className("tableScroll"))){
                String data = element.getText();
                String data2 = element.getText();
                String data3 = element.getText();
//                log.info("name=" + data + "tel=" + data2);

//                WebElement imgs = element.findElement(By.tagName("img"));
//                String img = imgs.getAttribute("src");

                WebCrawlerDto dto = new WebCrawlerDto();
                dto.setName(data);
                dto.setTel(data2);
                dto.setTel2(data3);

                list.add(dto);
            }
            log.info(list.toString(), list.size());
            model.addAttribute("crawlResult", list); // Crawling 결과를 HTML 템플릿에 전달
            return "crawling-result";
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return "error";
        } finally {
            driver.close();
        }
    }
}

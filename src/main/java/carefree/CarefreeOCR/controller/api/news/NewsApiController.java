package carefree.CarefreeOCR.controller.api.news;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/news")
public class NewsApiController {

    @RequestMapping(value = "/get")
    public void getNews() {

    }
}

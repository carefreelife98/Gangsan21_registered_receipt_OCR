package carefree.CarefreeOCR.controller.api.news;

import carefree.CarefreeOCR.controller.api.base.BaseApiController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/news")
public class NewsApiController extends BaseApiController {

    @RequestMapping(value = "/get")
    public void getNews() {

    }
}

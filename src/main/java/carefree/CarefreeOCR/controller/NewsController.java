package carefree.CarefreeOCR.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/news")
public class NewsController {

    @RequestMapping()
    public String getNewsForm() {
        return "news/news-form";
    }
}

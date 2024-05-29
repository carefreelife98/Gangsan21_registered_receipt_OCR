package carefree.CarefreeOCR.controller.api.construct;

import carefree.CarefreeOCR.service.util.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/construct/info/molit")
public class molitController {

    @Value("${construct.molit.url}")
    private String apiUrl;

    @Value("${construct.molit.secretKey.enc}")
    private String encKey;

    @Value("${construct.molit.secretKey.dec}")
    private String decKey;

    @Autowired
    private ExcelService excelService;

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getMOLITCorpInfos(@RequestParam String pageNo, @RequestParam String numOfRows,
                                                    @RequestParam String sDate, @RequestParam String eDate,
                                                    @RequestParam(required = false) String ncrAreaName,
                                                    @RequestParam(required = false) String ncrAreaDetailName ) throws IOException {
        WebClient webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String response = webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("")
                                .queryParam("serviceKey", encKey)
                                .queryParam("pageNo", pageNo)
                                .queryParam("numOfRows", numOfRows)
                                .queryParam("sDate", sDate)
                                .queryParam("eDate", eDate)
                                .queryParam("_type", "json")
                                .queryParam("ncrAreaName", ncrAreaName)
                                .queryParam("ncrAreaDetailName", ncrAreaDetailName)
                                .build()
                )
                .retrieve()
                // 여기 전까지가 요청을 정의 한 부분. 아래부터 정의하는건 응답을 어떻게 처리할 것인지
                .bodyToMono(String.class)    // 응답의 body를 String으로 해석
                .block();    // 동기식으로 처리
        log.info(response);

        return excelService.downloadJsonAsExcel(response);
    }
}

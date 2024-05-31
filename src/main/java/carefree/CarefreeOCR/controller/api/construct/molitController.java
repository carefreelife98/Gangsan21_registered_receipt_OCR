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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        // URI 빌더를 사용하여 URI를 구성
        String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .path("")
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json")
                .queryParam("serviceKey", URLEncoder.encode(decKey, StandardCharsets.UTF_8).replace("+", "%2B"))
                .queryParam("sDate", URLEncoder.encode(sDate, StandardCharsets.UTF_8))
                .queryParam("eDate", URLEncoder.encode(eDate, StandardCharsets.UTF_8))
                .queryParam("ncrAreaName", URLEncoder.encode(ncrAreaName, StandardCharsets.UTF_8))
                .queryParam("ncrAreaDetailName", URLEncoder.encode(ncrAreaDetailName, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        log.warn("Complete URL: " + uri);

        // WebClient를 사용하여 요청을 보내고 응답을 받음
        String response = webClient
                .get()
                .uri(uriBuilder -> UriComponentsBuilder.fromHttpUrl(uri).build().toUri())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info(response);

        return excelService.downloadJsonAsExcel(response);
    }
}

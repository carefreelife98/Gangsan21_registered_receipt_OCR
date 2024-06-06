package carefree.CarefreeOCR.api.publicapi.construct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Service
public class KicaService {

    @Value("${construct.kica.url}")
    private String apiUrl;

    public String getKicaData(String size, String searchType, String searchText, String searchSido) {

        // WebClient 에서 인코딩 과정을 따로 수행하지 않도록 하기 위함 (공공 API 사용 시 API Key 값이 인코딩에 의해 자동으로 변경되는 문제 방지)
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(apiUrl);
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(defaultUriBuilderFactory)
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // WebClient를 사용하여 요청을 보내고 응답을 받음
        String response = webClient
                .get()
                .uri(builder ->
                        builder
                                .queryParam("pageNumber", "1")
                                .queryParam("size", size)
                                .queryParam("searchType", searchType)
                                .queryParam("searchText", searchText)
                                .queryParam("searchSido", searchSido)
                                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info(response);
        return response;
    }
}

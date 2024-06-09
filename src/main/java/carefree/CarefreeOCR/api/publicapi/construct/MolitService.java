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
public class MolitService {

	@Value("${construct.molit.url}")
	private String apiUrl;

	@Value("${construct.molit.secretKey.enc}")
	private String encKey;

	@Value("${construct.molit.secretKey.dec}")
	private String decKey;

	public String getMolitData(
			String numOfRows,
			String sDate, String eDate,
			String ncrAreaName, String ncrAreaDetailName
	) {

		// WebClient 에서 인코딩 과정을 따로 수행하지 않도록 하기 위함 (공공 API 사용 시 API Key 값이 인코딩에 의해 자동으로 변경되는 문제 방지)
		DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(apiUrl);
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

		// 256MB 의 스프링 내부 버퍼 크기 제한을 해제. (많은 업체 데이터 가져올 시 에러 발생함)
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
								.queryParam("pageNo", "1")
								.queryParam("numOfRows", numOfRows)
								.queryParam("_type", "json")
								.queryParam("serviceKey", encKey)
								.queryParam("sDate", sDate)
								.queryParam("eDate", eDate)
								.queryParam("ncrAreaName", ncrAreaName)
								.queryParam("ncrAreaDetailName", ncrAreaDetailName)
								.build())
				.retrieve()
				.bodyToMono(String.class)
				.block();

//        log.info(response);
		return response;
	}
}

package carefree.CarefreeOCR.api.publicapi.construct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EcicService {

    @Value("${construct.ecic.url}")
    private String baseUrl;

    public List<List<Object>> getEcicData() {
        List<List<Object>> data = new ArrayList<>();

        try {
            // HTTP GET 요청 보내기 (총 페이지 수 계산 위함)
            RestTemplate restTemplate = new RestTemplate();
            String preResponse = restTemplate.getForObject(baseUrl + "1", String.class);

            // Jsoup을 사용하여 HTML 파싱
            Document tempDoc = Jsoup.parse(preResponse);

            // 특정 클래스명을 가진 테이블 찾기
            Element total = tempDoc.selectFirst("div.count");
            int totalDataCount = Integer.parseInt(
                    Objects.requireNonNull(
                            Objects.requireNonNull(total).selectFirst("span")
                    )
                    .text()
            );

            // Pagination
            int pageNo = totalDataCount / 10;
            if ((totalDataCount % 10) != 0) {
                pageNo += 1;
            }

            for (int i = 1; i < pageNo; i++) {
                // HTTP GET 요청 보내기 (총 페이지 수 계산 위함)
                String url = baseUrl + i;
                String response = restTemplate.getForObject(url, String.class);

                // Jsoup을 사용하여 HTML 파싱
                Document doc = Jsoup.parse(response);

                // 특정 클래스명을 가진 테이블 찾기
                Element table = doc.selectFirst("table.txtC");

                if (table != null) {
                    // 테이블 내의 <tbody> 필드 추출
                    Element tbody = table.selectFirst("tbody");

                    if (tbody != null) {
                        // <tbody> 내의 필드들 추출 (여기서는 <tr> 기준으로 추출)
                        Elements rows = tbody.select("tr");
                        if (rows != null) {
                            // 추출한 <tr>들을 처리하여 데이터 리스트 생성
                            for (Element row : rows) {
                                Elements tds = row.select("td");
                                if (tds != null) {
                                    List<Object> rowData = new ArrayList<>();
                                    for (Element td : tds) {
                                        rowData.add(td.text());
                                    }
                                    data.add(rowData);
                                }
                            }
                        }
                    } else {
                        System.out.println("<tbody> 태그를 찾을 수 없습니다.");
                    }
                } else {
                    System.out.println("클래스명이 'txtC'인 테이블을 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
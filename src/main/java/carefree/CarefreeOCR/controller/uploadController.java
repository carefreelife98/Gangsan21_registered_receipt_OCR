package carefree.CarefreeOCR.controller;

import carefree.CarefreeOCR.api.GoogleSheet;
import carefree.CarefreeOCR.api.NaverOcrApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

@Controller
@Slf4j
@RequiredArgsConstructor
public class uploadController {
    @Value("${naver.service.secretKey}")
    private String secretKey;
    private final NaverOcrApi naverApi;
//    private final GoogleSheet gSheet;
    ArrayList<String> afterFmt = new ArrayList<>();
    String date = "";
    private static final String[] REGIONS = {
            "서울특별시",
            "부산광역시",
            "대구광역시",
            "인천광역시",
            "광주광역시",
            "대전광역시",
            "울산광역시",
            "세종특별자치시",
            "경기도",
            "강원특별자치도",
            "충청북도",
            "충청남도",
            "전라북도",
            "전라남도",
            "경상북도",
            "경상남도",
            "제주특별자치도"
    };


    // 파일 업로드 폼을 보여주기 위한 GET 요청 핸들러 메서드
    @GetMapping("/upload-form")
    public String uploadForm() throws Exception {
//        return "/upload-form"; // url 시작이 / 인 경우 ec2 linux 에서 인식하지 못하는 에러 존재.
        return "upload-form"; // HTML 템플릿의 이름을 반환 (upload-form.html)
    }

    // 파일 업로드 및 OCR 수행을 위한 POST 요청 핸들러 메서드
    @PostMapping("/uploadAndOcr")
    public String uploadAndOcr(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            return "error"; // 파일이 비어있을 경우 에러를 처리하는 HTML 템플릿으로 이동
        }

        String naverSecretKey = secretKey; // 본인의 네이버 Clova OCR 시크릿 키로 대체

        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(tempFile);

        // 이전 실행 내역 초기화
        if (!afterFmt.isEmpty()) {
            afterFmt.clear();
        }
        List<String> result = naverApi.callApi("POST", tempFile.getPath(), naverSecretKey, "jpeg");

        tempFile.delete(); // 임시 파일 삭제

        ListIterator<String> iter = result.listIterator();
        int total = 0;
        while (iter.hasNext()) {
            String text = iter.next();

            // 날짜 확인
            if (text.matches("\\d{4}-\\d{2}-\\d{2}")) {
//                String date = text.replaceAll("[^0-9]", "");
//                iDate = Integer.parseInt(date);
                date = text;
            }

            // 등기 번호 이면,
            if (text.matches("\\d{5}-\\d{4}-\\d{4}")) {
                total ++;
                // 총 개수 Numbering
                afterFmt.add("[" + total + "]");

                // 등기 번호 저장
                afterFmt.add(text);

                // 정규화 된 등기 번호를 사용한 각각의 우체국 등기 조회 서비스 링크.
                String findPostUrl =
                        "https://service.epost.go.kr/trace.RetrieveDomRigiTraceList.comm?sid1="
                                + text.replaceAll("[^0-9]", "")
                                + "&displayHeader=";
                afterFmt.add(findPostUrl);

                // 가격 skip, 우편 번호 저장
                iter.next();
                text = iter.next();
                afterFmt.add(text);

                // 기업 명 저장
                afterFmt.add(iter.next());

                // 수신인 저장
                text = iter.next();
                if (Arrays.asList(REGIONS).contains(text)) {
                    afterFmt.add("-");
                    iter.previous();
                }
                else {
                    if(text.length() <= 2) {
                        text = text.concat(iter.next());
                    }
                    afterFmt.add(text);
                }

                // 주소 저장
                StringBuilder adr = new StringBuilder();
                while (iter.hasNext()) {
                    text = iter.next();

                    if (text.equals("합계") || text.equals("통상") || text.equals("익일특급")) {
                        afterFmt.add(String.valueOf(adr));
                        break;
                    }

                    // 띄어 쓰기 포함 주소 문자열 concat.
                    if (iter.hasNext())
                        adr.append(" ").append(text);
                }
            }
        }
        model.addAttribute("ocrResult", afterFmt); // OCR 결과를 HTML 템플릿에 전달

        List<List<Object>> toGSheet = new ArrayList<>();
        int idx = 0;
        log.info("size: " + afterFmt.size());
        for (int i = 0; i < afterFmt.size(); i++) {
            List<Object> temp = new ArrayList<>();
            for (int j = 0; j < 7 && idx < afterFmt.size(); j++) {
                temp.add(afterFmt.get(idx));
                idx ++;
            }
            toGSheet.add(temp);
        }
        GoogleSheet.updateValues(date, "1UADUNDLfmaQLJ1woHzVs9sq2HyScmfLla4lKvjaAwy8", "A1:G1000", "RAW", toGSheet);
        return "ocr-result"; // OCR 결과를 표시하는 HTML 템플릿 이름 반환
    }
}

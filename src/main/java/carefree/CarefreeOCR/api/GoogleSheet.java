package carefree.CarefreeOCR.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class GoogleSheet {
//    private static HttpRequestInitializer requestInitializer;
//    public GoogleSheet() throws IOException {
//        this.requestInitializer = new HttpCredentialsAdapter(
//                GoogleCredentials.fromStream(new FileInputStream(secret))
//                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"))
//        );
//    }

    @Value("${google.local.secret}")
    private String secret;

    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/home/ec2-user/app/secrets/gangsan21-ocr-287fbd2bd471.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Google Sheets API")
                .build();
    }


    // UpdateValuesResponse : 기존 셀에 덮어 쓰기
    // AppendValuesResponse : 기존 셀 끝에 이어 쓰기
    // 데이터 반환 타입만 변경해주면 기능 변경 가능.
    public void updateValues(String sheetTitle,
            String spreadsheetId,
            String range,
            String valueInputOption,
            List<List<Object>> values) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        // 시트를 추가
        SheetProperties addedSheetProperties = addSheet(service, sheetTitle, spreadsheetId);

        // 시트 이름과 지정한 범위 (A1:G1000) 에 Data 저장.
        ValueRange body = new ValueRange().setValues(values);

        // spreadsheetId 와 range 를 합쳐 update 할 위치를 지정 (Spreadsheet 이름 ! 시작 셀 : 끝 셀)
        // 예: 2023-12-28!A1:G1000
        service.spreadsheets().values().append(spreadsheetId, sheetTitle + "!" + range, body)
                .setValueInputOption(valueInputOption)
                .execute();
    }

    public static SheetProperties addSheet(Sheets service, String sheetTitle, String spreadsheetId) throws IOException {
        // 시트가 이미 존재하는지 확인
        SheetProperties existingSheet = getSheet(service, sheetTitle, spreadsheetId);
        if (existingSheet != null) {
            return existingSheet; // 이미 존재하면 해당 시트의 정보를 반환
        }

        // 시트를 추가하기 위한 요청 생성
        AddSheetRequest addSheetRequest = new AddSheetRequest();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle(sheetTitle);
        addSheetRequest.setProperties(sheetProperties);

        // 스프레드시트 업데이트를 위한 요청 생성
        BatchUpdateSpreadsheetRequest updateRequest = new BatchUpdateSpreadsheetRequest();

        // 시트 추가 요청을 업데이트 요청에 추가
        updateRequest.setRequests(Collections.singletonList(
                new Request()
                        .setAddSheet(addSheetRequest)
        ));

        // 업데이트 요청을 Google Sheets API에 전송
        service.spreadsheets().batchUpdate(spreadsheetId, updateRequest).execute();
        return sheetProperties;
    }

    private static SheetProperties getSheet(Sheets service, String sheetTitle, String spreadsheetId) throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        for (Sheet sheet : sheets) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return sheet.getProperties();
            }
        }
        return null;
    }
}

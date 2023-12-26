package carefree.CarefreeOCR.api;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class GoogleSheet {
    private static HttpRequestInitializer requestInitializer;

    public GoogleSheet() throws IOException {
        this.requestInitializer = new HttpCredentialsAdapter(
                GoogleCredentials.fromStream(new FileInputStream("/home/ec2-user/app/gangsan21-ocr-6e01aae86a2f.json"))
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"))
        );
    }

    // UpdateValuesResponse : 기존 셀에 덮어 쓰기
    // AppendValuesResponse : 기존 셀 끝에 이어 쓰기
    // 데이터 반환 타입만 변경해주면 기능 변경 가능.
    public static AppendValuesResponse updateValues(String sheetTitle,
                                                    String spreadsheetId,
                                                    String range,
                                                    String valueInputOption,
                                                    List<List<Object>> values) throws IOException {
        Sheets service = new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Sheets samples")
                .build();

        // 시트를 추가
        SheetProperties addedSheetProperties = addSheet(service, sheetTitle, spreadsheetId);

        // Updates the values in the specified range.
        ValueRange body = new ValueRange().setValues(values);
        return service.spreadsheets().values().append(spreadsheetId, sheetTitle + "!" + range, body)
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

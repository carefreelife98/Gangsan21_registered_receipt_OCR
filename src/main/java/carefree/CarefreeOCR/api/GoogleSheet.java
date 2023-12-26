package carefree.CarefreeOCR.api;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
    /**
     * Sets values in a range of a spreadsheet.
     *
     * @param spreadsheetId    - Id of the spreadsheet.
     * @param range            - Range of cells of the spreadsheet.
     * @param valueInputOption - Determines how input data should be interpreted.
     * @param values           - List of rows of values to input.
     * @return spreadsheet with updated values
     * @throws IOException - if credentials file not found.
     */
    public static UpdateValuesResponse updateValues(String sheetTitle,
                                                    String spreadsheetId,
                                                    String range,
                                                    String valueInputOption,
                                                    List<List<Object>> values)
            throws IOException {
//        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
//                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                GoogleCredentials.fromStream(new FileInputStream("/home/ec2-user/app/gangsan21-ocr-6e01aae86a2f.json"))
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"))
        );

        // Create the sheets API client
        Sheets service = new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Sheets samples")
                .build();

        // 시트를 추가
        SheetProperties addedSheetProperties = addSheet(service, sheetTitle, spreadsheetId);

        // 추가된 시트의 ID 얻기
        Integer addedSheetId = addedSheetProperties.getSheetId();

//        // 확인할 범위를 얻어옴
//        String dataRange = getSheetDataRange(service, spreadsheetId, addedSheetId);
//
//        // 데이터가 있는 경우 범위를 업데이트하고 값 입력
//        if (dataRange != null && !dataRange.isEmpty()) {
//            range = dataRange + "," + range;
//        }

//        UpdateValuesResponse result = null;
        try {
            // Updates the values in the specified range.
            ValueRange body = new ValueRange()
                    .setValues(values);
            UpdateValuesResponse result = service.spreadsheets().values().update(spreadsheetId, addedSheetId.toString() + "!" + range, body)
                    .setValueInputOption(valueInputOption)
                    .execute();
            System.out.printf("%d cells updated.", result.getUpdatedCells());
            return result;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }
        return null;
    }

//    private static String getSheetDataRange(Sheets service, String spreadsheetId, int sheetId) throws IOException {
//        String range = sheetId + "!A:Z"; // Adjust the range as needed
//        try {
//            ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
//            List<List<Object>> values = response.getValues();
//            if (values == null || values.isEmpty()) {
//                return null;
//            } else {
//                int numRows = values.size();
//                int numCols = values.get(0).size();
//                return String.format("%d:%d", numRows + 1, numCols);
//            }
//        } catch (GoogleJsonResponseException e) {
//            GoogleJsonError error = e.getDetails();
//            if (error.getCode() == 404) {
//                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
//            } else {
//                throw e;
//            }
//        }
//        return null;
//    }

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
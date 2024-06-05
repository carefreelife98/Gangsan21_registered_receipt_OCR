package carefree.CarefreeOCR.service.util;

import carefree.CarefreeOCR.api.GoogleSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import java.security.GeneralSecurityException;

@Slf4j
@Service
public class ExcelService {

    @Value("${construct.molit.sheet}")
    private String sheetId;

    @Autowired
    private GoogleSheet googleSheet;

    private final List<String> FIELD = Arrays.asList(
            "등록시군구", "등록시도", "소재지",
            "등록일자", "공시내용구분", "업체명",
            "업체대표자명", "공고번호", "변경사유철회",
            "공시일자", "공시일련번호", "등록업종",
            "업종등록번호", "사업자등록번호", "전화번호"
    );

    public void uploadJsonToGoogleSheet(String jsonString) throws IOException, GeneralSecurityException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        Sheets sheetsService = googleSheet.getSheetsService();

        // 정적 데이터 삽입
        List<CellData> staticRow = new ArrayList<>();
        for (String data : FIELD) {
            staticRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(data)));
        }

        // 각 행별 구분 요소 추가 (헤더)
        List<Request> requests = new ArrayList<>();
        List<CellData> headerRow = new ArrayList<>();
        Iterator<String> fieldNames = items.get(0).fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            headerRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(fieldName)));
        }

        requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                .setStart(new GridCoordinate().setSheetId(0).setRowIndex(0).setColumnIndex(0))
                .setRows(Collections.singletonList(new RowData().setValues(headerRow)))
                .setFields("userEnteredValue")));

        // 헤더 밑의 행부터 데이터 인입.
        int rowNum = 1;
        for (JsonNode item : items) {
            List<CellData> row = new ArrayList<>();
            fieldNames = item.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode value = item.get(fieldName);
                CellData cellData = new CellData();
                if (value.isTextual()) {
                    cellData.setUserEnteredValue(new ExtendedValue().setStringValue(value.asText()));
                } else if (value.isLong()) {
                    cellData.setUserEnteredValue(new ExtendedValue().setNumberValue(value.asDouble()));
                }
                row.add(cellData);
            }
            requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                    .setStart(new GridCoordinate().setSheetId(0).setRowIndex(rowNum++).setColumnIndex(0))
                    .setRows(Collections.singletonList(new RowData().setValues(row)))
                    .setFields("userEnteredValue")));
        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(sheetId, batchUpdateRequest).execute();
    }
}

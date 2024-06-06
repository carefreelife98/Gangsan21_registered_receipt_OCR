package carefree.CarefreeOCR.service.construct;

import carefree.CarefreeOCR.api.GoogleSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@Service
public class ConstructExcelService {

    @Value("${construct.molit.sheet}")
    private String MOLIT_SHEET_ID;

    @Value("${construct.kica.sheet}")
    private String KICA_SHEET_ID;

    @Autowired
    private GoogleSheet googleSheet;

    @Autowired
    private ConstructExcelUtilService constructExcelUtilService;

    private final List<String> MOLIT_FIELD = Arrays.asList(
            "등록시군구", "등록시도", "소재지",
            "등록일자", "공시내용구분", "업체명",
            "업체대표자명", "공고번호", "변경사유철회",
            "공시일자", "공시일련번호", "등록업종",
            "업종등록번호", "사업자등록번호", "전화번호"
    );

    public void uploadMolitJsonToGoogleSheet(String jsonString, Integer numOfRows) throws IOException, GeneralSecurityException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        Sheets sheetsService = googleSheet.getSheetsService();

        // 시트 기본 행수는 999 이므로 그 이상의 데이터가 요구되면 Row 수를 늘려주어야 한다.
        if (numOfRows >= 900) {
            Integer sheetId =
                    sheetsService
                    .spreadsheets().get(MOLIT_SHEET_ID).execute()
                    .getSheets().get(0).getProperties().getSheetId();

            int addRows = numOfRows - 900;
            constructExcelUtilService.addRowsToSheet(sheetsService, MOLIT_SHEET_ID, sheetId, addRows);
        }

        // 각 행별 구분 요소 추가 (헤더)
        List<Request> requests = new ArrayList<>();
        List<CellData> headerRow = new ArrayList<>();
        Iterator<String> fieldNames = items.get(0).fieldNames();

        // 정적 데이터 삽입
        List<CellData> staticRow = new ArrayList<>();
        for (String data : MOLIT_FIELD) {
            staticRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(data)));
        }

        requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                .setStart(new GridCoordinate().setSheetId(0).setRowIndex(0).setColumnIndex(0))
                .setRows(Collections.singletonList(new RowData().setValues(staticRow)))
                .setFields("userEnteredValue")));

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            headerRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(fieldName)));
        }

        requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                .setStart(new GridCoordinate().setSheetId(0).setRowIndex(1).setColumnIndex(1))
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
        sheetsService.spreadsheets().batchUpdate(MOLIT_SHEET_ID, batchUpdateRequest).execute();
    }

    public void uploadKicaJsonToGoogleSheet(String jsonString, int size) throws IOException, GeneralSecurityException {
        Sheets sheetsService = googleSheet.getSheetsService();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode dataNode = rootNode.path("data").path("list");

        // 시트 기본 행수는 999 이므로 그 이상의 데이터가 요구되면 Row 수를 늘려주어야 한다.
        if (size >= 900) {
            Integer sheetId =
                    sheetsService
                            .spreadsheets().get(KICA_SHEET_ID).execute()
                            .getSheets().get(0).getProperties().getSheetId();

            int addRows = size - 900;
            constructExcelUtilService.addRowsToSheet(sheetsService, KICA_SHEET_ID, sheetId, addRows);
        }

        List<List<Object>> request = new ArrayList<>();
        List<Object> header = List.of("registNo", "firmNmKor", "repNmKor", "phoneNoOffice", "ranking", "addr", "expelYn", "evalAmt", "rankTot", "id");
        request.add(header);

        for (JsonNode node : dataNode) {
            List<Object> row = new ArrayList<>();
            row.add(node.path("registNo").asText());
            row.add(node.path("firmNmKor").asText());
            row.add(node.path("repNmKor").asText());
            row.add(node.path("phoneNoOffice").asText());
            row.add(node.path("ranking").asInt());
            row.add(node.path("addr").asText());
            row.add(node.path("expelYn").asText());
            row.add(node.path("evalAmt").asText().trim());
            row.add(node.path("rankTot").asInt());
            row.add(node.path("id").asLong());
            request.add(row);
        }

        ValueRange body = new ValueRange().setValues(request);
        sheetsService.spreadsheets().values().update(KICA_SHEET_ID, "KicaSheet", body)
                .setValueInputOption("RAW")
                .execute();
    }
}

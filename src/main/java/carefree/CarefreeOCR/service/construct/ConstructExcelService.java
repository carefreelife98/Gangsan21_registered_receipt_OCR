package carefree.CarefreeOCR.service.construct;

import carefree.CarefreeOCR.api.GoogleSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    @Value("${construct.default.sheet}")
    private String DEFAULT_SHEET_ID;

    @Value("${construct.molit.sheet}")
    private String MOLIT_SHEET_ID;

    @Value("${construct.molit.sheet}")
    private String MOLIT_SHEET_NM;

    @Value("${construct.kica.sheet}")
    private String KICA_SHEET_ID;

    @Value("${construct.kica.name}")
    private String KICA_SHEET_NM;

    @Value("${construct.ecic.sheet}")
    private String ECIC_SHEET_ID;

    @Value("${construct.ecic.name}")
    private String ECIC_SHEET_NM;

    @Value("${construct.ekffa.sheet}")
    private String EKFFA_SHEET_ID;

    @Value("${construct.ekffa.name}")
    private String EKFFA_SHEET_NM;

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
        final int MAX_ROWS = 10000;

        Sheets sheetsService = googleSheet.getSheetsService();
        // 시트 기본 행수는 999 이므로 그 이상의 데이터가 요구되면 Row 수를 늘려주어야 한다.
        Optional<Sheet> sheet =
                sheetsService.spreadsheets().get(DEFAULT_SHEET_ID).execute().getSheets()
                        .stream()
                        .filter(s -> MOLIT_SHEET_NM.equals(s.getProperties().getTitle()))
                        .findFirst();

        Integer sheetId = sheet.map(s -> s.getProperties().getSheetId()).orElse(null);

        constructExcelUtilService.addRowsToSheet(sheetsService, DEFAULT_SHEET_ID, sheetId, numOfRows + 5);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        int totalChunks = (int) Math.ceil((double) numOfRows / MAX_ROWS);

        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            int startRow = chunkIndex * MAX_ROWS;
            int endRow = Math.min(startRow + MAX_ROWS, numOfRows);

            JsonNode chunkItems = objectMapper.createArrayNode();
            for (int i = startRow; i < endRow; i++) {
                ((ArrayNode) chunkItems).add(items.get(i));
            }

            uploadChunkToGoogleSheet(sheetsService, chunkItems, startRow, endRow - startRow, sheetId);
        }
    }

    private void uploadChunkToGoogleSheet(Sheets sheetsService, JsonNode items, int startRowIndex, Integer numOfRows, Integer sheetId) throws IOException, GeneralSecurityException {

        List<Request> requests = new ArrayList<>();
        List<CellData> headerRow = new ArrayList<>();
        Iterator<String> fieldNames = items.get(0).fieldNames();

        // 정적 데이터 삽입
        List<CellData> staticRow = new ArrayList<>();
        for (String data : MOLIT_FIELD) {
            staticRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(data)));
        }

        requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(0).setColumnIndex(0))
                .setRows(Collections.singletonList(new RowData().setValues(staticRow)))
                .setFields("userEnteredValue")));

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            headerRow.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(fieldName)));
        }

        requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(1).setColumnIndex(1))
                .setRows(Collections.singletonList(new RowData().setValues(headerRow)))
                .setFields("userEnteredValue")));

        // 헤더 밑의 행부터 데이터 인입.
        int rowNum = startRowIndex + 2; // Start from the appropriate row index
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
                    .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowNum++).setColumnIndex(0))
                    .setRows(Collections.singletonList(new RowData().setValues(row)))
                    .setFields("userEnteredValue")));
        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(DEFAULT_SHEET_ID, batchUpdateRequest).execute();
    }


    public void uploadKicaJsonToGoogleSheet(String jsonString, int size) throws IOException, GeneralSecurityException {

        Sheets sheetsService = googleSheet.getSheetsService();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode dataNode = rootNode.path("data").path("list");

        // 시트 기본 행수는 999 이므로 그 이상의 데이터가 요구되면 Row 수를 늘려주어야 한다.
        if (size >= 900) {
            Optional<Sheet> sheet =
                    sheetsService.spreadsheets().get(DEFAULT_SHEET_ID).execute().getSheets()
                    .stream()
                            .filter(s -> KICA_SHEET_NM.equals(s.getProperties().getTitle()))
                            .findFirst();

            Integer sheetId = sheet.map(s -> s.getProperties().getSheetId()).orElse(null);

            constructExcelUtilService.addRowsToSheet(sheetsService, DEFAULT_SHEET_ID, sheetId, size + 5);
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
        sheetsService.spreadsheets().values().update(DEFAULT_SHEET_ID, KICA_SHEET_NM, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void uploadEcicJsonToGoogleSheet(List<List<Object>> values) throws IOException, GeneralSecurityException {
        Sheets sheetsService = googleSheet.getSheetsService();
        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values().update(DEFAULT_SHEET_ID, ECIC_SHEET_NM, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void uploadEkffaJsonToGoogleSheet(List<List<Object>> values) throws IOException, GeneralSecurityException {
        Sheets sheetsService = googleSheet.getSheetsService();
        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values().update(DEFAULT_SHEET_ID, EKFFA_SHEET_NM, body)
                .setValueInputOption("RAW")
                .execute();
    }
}

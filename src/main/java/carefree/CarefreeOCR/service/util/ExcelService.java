package carefree.CarefreeOCR.service.util;

import carefree.CarefreeOCR.api.GoogleSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class ExcelService {

    @Value("${construct.molit.sheet}")
    private String sheetId;

    @Autowired
    private GoogleSheet googleSheet;

    public void uploadJsonToGoogleSheet(String jsonString) throws IOException, GeneralSecurityException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        Sheets sheetsService = googleSheet.getSheetsService();

        // Create header row
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

        // Populate rows with data
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



    //    public ResponseEntity<byte[]> downloadJsonAsExcel(String jsonString) throws IOException {
//        // JSON 응답을 파싱합니다.
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode root = objectMapper.readTree(jsonString);
//        JsonNode items = root.path("response").path("body").path("items").path("item");
//
//        // Create Excel workbook and sheet
//        Workbook workbook = new XSSFWorkbook();
//        Sheet sheet = workbook.createSheet("강산21_국토교통부_키스콘_건설업체정보");
//
//        // Create header row
//        Row headerRow = sheet.createRow(0);
//        Iterator<String> fieldNames = items.get(0).fieldNames();
//        int colNum = 0;
//        while (fieldNames.hasNext()) {
//            String fieldName = fieldNames.next();
//            Cell cell = headerRow.createCell(colNum++);
//            cell.setCellValue(fieldName);
//        }
//
//        // Populate rows with data
//        int rowNum = 1;
//        for (JsonNode item : items) {
//            Row row = sheet.createRow(rowNum++);
//            colNum = 0;
//            fieldNames = item.fieldNames();
//            while (fieldNames.hasNext()) {
//                String fieldName = fieldNames.next();
//                Cell cell = row.createCell(colNum++);
//                JsonNode value = item.get(fieldName);
//                if (value.isTextual()) {
//                    cell.setCellValue(value.asText());
//                } else if (value.isInt()) {
//                    cell.setCellValue(value.asInt());
//                } else if (value.isLong()) {
//                    cell.setCellValue(value.asLong());
//                }
//            }
//        }
//
//        // Write the output to a byte array
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        workbook.write(bos);
//        workbook.close();
//
//        byte[] excelBytes = bos.toByteArray();
//
//        // Create the response entity
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDispositionFormData("attachment", "items.xlsx");
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(excelBytes);
//    }
}

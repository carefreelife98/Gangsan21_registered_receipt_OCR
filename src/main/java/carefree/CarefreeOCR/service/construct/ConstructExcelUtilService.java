package carefree.CarefreeOCR.service.construct;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Service
public class ConstructExcelUtilService {

    public void addRowsToSheet(Sheets service, String spreadsheetId, int sheetId, int newRowCount) throws IOException {
        Request request = new Request()
                .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                        .setProperties(new SheetProperties()
                                .setSheetId(sheetId)
                                .setGridProperties(new GridProperties()
                                        .setRowCount(newRowCount)))
                        .setFields("gridProperties.rowCount"));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                .setRequests(Arrays.asList(request));

        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }

}

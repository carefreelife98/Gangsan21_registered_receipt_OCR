package carefree.CarefreeOCR.controller.api.construct;

import carefree.CarefreeOCR.api.publicapi.construct.KicaService;
import carefree.CarefreeOCR.api.publicapi.construct.MolitService;
import carefree.CarefreeOCR.service.construct.ConstructExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequestMapping(value = "/construct/info/")
public class ConstructController {

    @Autowired
    private MolitService molitService;

    @Autowired
    private KicaService kicaService;

    @Autowired
    private ConstructExcelService constructExcelService;

    @RequestMapping(value = "/molit/download", method = RequestMethod.GET)
    public void getMOLITCorpInfos(
            @RequestParam String numOfRows,
            @RequestParam String sDate,
            @RequestParam String eDate,
            @RequestParam(required = false) String ncrAreaName,
            @RequestParam(required = false) String ncrAreaDetailName) throws GeneralSecurityException, IOException {

        String molitData = molitService.getMolitData(numOfRows.trim(), sDate, eDate, ncrAreaName.trim(), ncrAreaDetailName.trim());
        constructExcelService.uploadMolitJsonToGoogleSheet(molitData, Integer.parseInt(numOfRows.trim()));
    }

    @RequestMapping(value = "/kica/download", method = RequestMethod.GET)
    public void getKICACorpInfos(
            @RequestParam String size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String searchSido) throws GeneralSecurityException, IOException {

        if (searchType.length() == 1 || searchType.isEmpty()) {
            String kicaData = kicaService.getKicaData(size.trim(), searchType.trim(), searchText.trim(), searchSido.trim());
            constructExcelService.uploadKicaJsonToGoogleSheet(kicaData, Integer.parseInt(size.trim()));
        } else {
            log.error("SearchType 파라미터가 잘못 되었습니다. SearchType:[{}]", searchType);
        }
    }
}

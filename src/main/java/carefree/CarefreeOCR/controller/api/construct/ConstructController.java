package carefree.CarefreeOCR.controller.api.construct;

import carefree.CarefreeOCR.api.publicapi.construct.EcicService;
import carefree.CarefreeOCR.api.publicapi.construct.EkffaService;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/construct/info/")
public class ConstructController {

    @Autowired
    private MolitService molitService;

    @Autowired
    private KicaService kicaService;

    @Autowired
    private EcicService ecicService;

    @Autowired
    private EkffaService ekffaService;

    @Autowired
    private ConstructExcelService constructExcelService;

    @RequestMapping(value = "/molit/download", method = RequestMethod.GET)
    public void getMOLITCorpInfos(
            @RequestParam String numOfRows,
            @RequestParam String sDate,
            @RequestParam String eDate,
            @RequestParam(required = false) String ncrAreaName,
            @RequestParam(required = false) String ncrAreaDetailName) throws GeneralSecurityException, IOException {

        if (ncrAreaName != null) {
            ncrAreaName = ncrAreaName.trim();
        }

        if (ncrAreaDetailName != null) {
            ncrAreaDetailName = ncrAreaDetailName.trim();
        }

        String molitData = molitService.getMolitData(numOfRows.trim(), sDate, eDate, ncrAreaName, ncrAreaDetailName);
        constructExcelService.uploadMolitJsonToGoogleSheet(molitData, Integer.parseInt(numOfRows.trim()));
    }

    @RequestMapping(value = "/kica/download", method = RequestMethod.GET)
    public void getKICACorpInfos(
            @RequestParam String size,
            @RequestParam(required = false, defaultValue = "1") String searchType,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String searchSido) throws GeneralSecurityException, IOException {

        if (searchType.length() == 1 || searchType.isEmpty()) {
            if (searchText != null) {
                searchText = searchText.trim();
            }

            if (searchSido != null) {
                searchSido = searchSido.trim();
            }
            String kicaData = kicaService.getKicaData(size.trim(), searchType, searchText, searchSido);
            constructExcelService.uploadKicaJsonToGoogleSheet(kicaData, Integer.parseInt(size.trim()));
        } else {
            log.error("SearchType 파라미터가 잘못 되었습니다. SearchType:[{}]", searchType);
        }
    }

    @RequestMapping(value = "/ecic/download", method = RequestMethod.GET)
    public void getECICCorpInfos() throws GeneralSecurityException, IOException {
        List<List<Object>> ecicData = ecicService.getEcicData();
//        log.info(ecicData.toString());
        constructExcelService.uploadEcicJsonToGoogleSheet(ecicData);
    }

    @RequestMapping(value = "/ekffa/download", method = RequestMethod.GET)
    public void getEkffaCorpInfos() throws GeneralSecurityException, IOException {
        List<List<Object>> ekffaData = ekffaService.getEkffaData();
//        log.info(ekffaData.toString());
        constructExcelService.uploadEkffaJsonToGoogleSheet(ekffaData);
    }
}

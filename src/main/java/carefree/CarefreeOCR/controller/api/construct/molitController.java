package carefree.CarefreeOCR.controller.api.construct;

import carefree.CarefreeOCR.api.publicapi.MolitService;
import carefree.CarefreeOCR.service.util.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequestMapping(value = "/construct/info/molit")
public class molitController {

    @Autowired
    private MolitService molitService;

    @Autowired
    private ExcelService excelService;

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public void getMOLITCorpInfos(@RequestParam String pageNo, @RequestParam String numOfRows,
                                                    @RequestParam String sDate, @RequestParam String eDate,
                                                    @RequestParam(required = false) String ncrAreaName,
                                                    @RequestParam(required = false) String ncrAreaDetailName ) throws GeneralSecurityException, IOException {

        String molitData = molitService.getMolitData(pageNo, numOfRows, sDate, eDate, ncrAreaName, ncrAreaDetailName);
        excelService.uploadJsonToGoogleSheet(molitData);
    }
}

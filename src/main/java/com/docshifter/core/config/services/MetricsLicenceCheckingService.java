package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Log4j2
@Service
public class MetricsLicenceCheckingService {

    private static ILicensingService nalpeironService;
    private static Boolean isLicensed;

    @Autowired
    public MetricsLicenceCheckingService(ILicensingService nalpeironService) {
        this.nalpeironService = nalpeironService;
    }

    @Bean
    public static boolean isLicensed() {
        if (isLicensed == null) {
            try {
                long[] fid = nalpeironService.validateAndStartModule("METR", new long[]{0L});
                nalpeironService.endModule("METR", new HashMap<>(), fid);
                log.debug("OK, Metrics is licensed");
                isLicensed = true;
            } catch (DocShifterLicenseException dilly) {
                log.debug("Licence exception!", dilly);
                isLicensed = false;
            }
        }
        return isLicensed;
    }
}

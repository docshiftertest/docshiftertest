package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
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
                log.info("OK, Metrics is licensed");
                isLicensed = true;
            }
            catch (DocShifterLicenseException dilly) {
                if (dilly.getNalpErrorCode() == -1096 && "NSL entitlement not found".equalsIgnoreCase(dilly.getNalpErrorMsg())) {
                    log.info("Metrics not licensed");
                } else {
                    log.info("Metrics Licence exception", dilly);
                }
                isLicensed = false;
            }
        }
        return isLicensed;
    }
}

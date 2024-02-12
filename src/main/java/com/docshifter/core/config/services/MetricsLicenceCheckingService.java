package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class MetricsLicenceCheckingService {

    private final ILicensingService nalpeironService;
    private Boolean isLicensed;

    public MetricsLicenceCheckingService(ILicensingService nalpeironService) {
        this.nalpeironService = nalpeironService;
    }

    public boolean isLicensed() {
        if (isLicensed == null) {
            try {
                long[] fid = nalpeironService.validateAndStartModule("METR", new long[]{0L});
                nalpeironService.endModule("METR", new HashMap<>(), fid);
                log.info("OK, Metrics is licensed");
                isLicensed = true;
            }
            catch (DocShifterLicenseException dilly) {
                //https://support.nalpeiron.com/hc/en-us/articles/360051138153-Error-Code-1096
                if (dilly.getNalpErrorCode() == -1096) {
                    log.info("Metrics not licensed");
                } else {
                    log.error("Metrics Licence exception", dilly);
                }
                isLicensed = false;
            }
        }
        return isLicensed;
    }
}

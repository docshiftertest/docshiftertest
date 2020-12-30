package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NalpeironAnalyticsSender implements Runnable {

    private final NalpeironHelper nalpeironHelper;
    private final String username;

    public NalpeironAnalyticsSender(NalpeironHelper nalpeironHelper, String username) {
        this.nalpeironHelper = nalpeironHelper;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            log.info("Sending licensing analytics to remote server if connection available");
            nalpeironHelper.sendAnalyticsCache(username);
        } catch (DocShifterLicenseException ex) {
            log.debug("Error during sending of analytics.", ex);
        }
    }
}
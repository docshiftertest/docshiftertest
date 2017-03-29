package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenseException;

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
            Logger.info("Sending licensing analytics to remote server if connection available", null);
            nalpeironHelper.sendAnalyticsCache(username);
        } catch (DocShifterLicenseException ex) {
            Logger.debug("Error during sending of analytics.", ex);
        }
    }
}
package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.exceptions.DocShifterLicenseException;

public class NalpeironAnalyticsSender implements Runnable {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NalpeironAnalyticsSender.class.getName());

    private final NalpeironHelper nalpeironHelper;
    private final String username;

    public NalpeironAnalyticsSender(NalpeironHelper nalpeironHelper, String username) {
        this.nalpeironHelper = nalpeironHelper;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            logger.info("Sending licensing analytics to remote server if connection available", null);
            nalpeironHelper.sendAnalyticsCache(username);
        } catch (DocShifterLicenseException ex) {
            logger.debug("Error during sending of analytics.", ex);
        }
    }
}
package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import org.apache.commons.lang.StringUtils;

public class NalpeironLicenseValidator implements Runnable {

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NalpeironLicenseValidator.class.getName());

	private String licenseNo;
	private boolean offlineActivation;

	private final NalpeironHelper nalpeironHelper;

	public NalpeironLicenseValidator(NalpeironHelper nalpeironHelper, String licenseNo, boolean offlineActivation) {
		this.licenseNo = licenseNo;
		this.nalpeironHelper = nalpeironHelper;
		this.offlineActivation = offlineActivation;
	}

	@Override
	public final void run() {
		validateLicenseStatus();
	}

	public final void validateLicenseStatus() {
		boolean validLicense = false;
		NalpeironHelper.LicenseStatus licenseStatus;

		try {
			if (offlineActivation) {
				logger.debug("Offline activation mode has been set, will forgo all connection attempts to the server and will try to activate offline");
			}

			//test for online connection
			boolean hasConnection = true;
			if (!offlineActivation) {
				try {
					nalpeironHelper.testNalpeironLicencingConnection();
				} catch (DocShifterLicenseException e) {
					hasConnection = false;
					logger.debug("No connection to the Nalpeiron server could be established, will try offline activation");
				}
			}

			if (!offlineActivation && hasConnection) {
				// ALWAYS DO ONLINE CHECKING WHEN HAVING CONNECTION
				logger.debug("Connection to the Nalpeiron server could be established, will try online activation");

				//check current license status
				licenseStatus = nalpeironHelper.getLicenseStatus();

				// get a new heartbeat for license with the supplied license code
				NalpeironHelper.LicenseStatus newHeartbeatStatus = NalpeironHelper.LicenseStatus.PRODUNDETERMINED;
				try {
					newHeartbeatStatus = nalpeironHelper.getLicense(licenseNo, ""); //TODO define what has to happen with the XML REG INFO
				} catch (DocShifterLicenseException ex) {
					logger.debug("Could not retrieve new license heartbeat successfully. NALP ERRROCODE: " + ex.getNalpErrorCode() + " NALP ERROR MESSGAG: " + ex.getNalpErrorMsg());
				}

				//if the license status or the heartbeat has value above 0, then the current license was validated
				if (!(licenseStatus.getValue() > 0 || newHeartbeatStatus.getValue() > 0)) {
					// license activation failed.
					logger.info("The license could not be activated online, or your trial has expired");
					validLicense = false;
				} else {
					logger.info("The license has been activated online, or your trial is active");
					validLicense = true;
				}
			}

			if (!validLicense) {
				//DO OFFLINE CHECKING
				logger.debug("Trying offline activation. Either there is no connection to the Nalpeiron server, or online activation failed");

				licenseStatus = nalpeironHelper.getLicenseStatus();
				//log status
				logger.info("Current license status is: " + licenseStatus.toString());

				//if the license status ha s value below 0, then the current license could not validate, try activating or generating an offline activation request
				if (!(licenseStatus.getValue() > 0)) {
					logger.info("License not activated. Trying to activate the license");
					String activationAnswer = nalpeironHelper.resolveLicenseActivationAnswer();

					if (!StringUtils.isBlank(activationAnswer)) {
						licenseStatus = nalpeironHelper.importCertificate(licenseNo, activationAnswer);

						//if the license status ha s value below 0, then the current license could not validate, try getting a new one
						if (!(licenseStatus.getValue() > 0)) {
							// license import failed.
							logger.info("The license could not be activated offline, import of DSLicenseActivationAnswer.txt failed");
							validLicense = false;
						} else {
							logger.info("The license has been activated offline, import of DSLicenseActivationAnswer.txt successful");
							validLicense = true;
						}
					} else {
						String activationRequest = nalpeironHelper.resolveLicenseActivationRequest();

						//if the current activation request is empty, or the file does not exist create it
						if (StringUtils.isBlank(activationRequest)) {
							activationRequest = nalpeironHelper.getActivationCertificateRequest(licenseNo, ""); //TODO define what has to happen with the XML REG INFO

							nalpeironHelper.writeLicenseActivationRequest(activationRequest);
						}

						logger.info("The license needs be activated offline, or you need to have an active internet connection. Activation request code written to DSLicenseActivationRequest.txt");
						validLicense = false;
					}
				} else {
					validLicense = true;
				}
			}

			if (validLicense) {
				//process extra fields after validating license
			} else {
				// license could not be validate, close application
				int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
				logger.fatal("license could not be validated, closing application");

				System.exit(errorCode);
			}

		} catch (DocShifterLicenseException ex) {
			int errorCode = -455;//we need to exit with non zero error so yajsw will restart the service and validation will be run again
			logger.debug(" NALP ERRROCODE: " + ex.getNalpErrorCode() + " NALP ERROR MESSGAG: " + ex.getNalpErrorMsg());
			logger.fatal("Exception while trying to validate the nalpeiron license, exiting with error", ex);

			System.exit(errorCode);
		}
	}
}
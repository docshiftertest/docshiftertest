package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
public class NalpeironLicenseValidator implements Runnable {
	private final NalpeironHelper nalpeironHelper;

	public NalpeironLicenseValidator(NalpeironHelper nalpeironHelper) {
		this.nalpeironHelper = nalpeironHelper;
	}

	@Override
	public final void run() {
		validateLicenseStatus();
	}

	public final void validateLicenseStatus() {
		boolean validLicense = false;

		try {
			if (isOnlineActivation()) {
				validLicense = isValidLicenseOnline(""); //TODO define what has to happen with the XML REG INFO
			}

			if (!validLicense) {
				validLicense = isValidLicenseOffline(""); //TODO define what has to happen with the XML REG INFO
			}

			if (validLicense) {
				//process extra fields after validating license
				validLicense = isValidExtraFields(nalpeironHelper);
			}

			if (!validLicense) {
				// license could not be validate, close application
				int errorCode = 0; //TODO: we need to exit with zero or yajsw will restart the service
				log.fatal("license could not be validated, closing application");

				System.exit(errorCode);
			}

		} catch (DocShifterLicenseException ex) {
			int errorCode = -455;//we need to exit with non zero error so yajsw will restart the service and validation will be run again
			log.debug(" NALP ERRORCODE: {} NALP ERROR MESSAGE: {}", ex.getNalpErrorCode(), ex.getNalpErrorMsg());
			log.fatal("Exception while trying to validate the Nalpeiron license, exiting with error", ex);
			System.exit(errorCode);
		}
	}

	private boolean isValidExtraFields(NalpeironHelper nalpeironHelper) {
		//TODO
		//nalpeironHelper.getUDFValue("")
		return true;
	}

	private boolean isValidLicenseOffline(String xmlRegInfo) throws DocShifterLicenseException {
		NalpeironHelper.LicenseStatus licenseStatus;
		boolean validLicense = false; //DO OFFLINE CHECKING
		log.debug("Trying offline activation. Either there is no connection to the Nalpeiron server, or online activation failed");

		licenseStatus = nalpeironHelper.getLicenseStatus();
		//log status
		log.info("Current license status is: {}", licenseStatus.toString());

		//if the license status ha s value below 0, then the current license could not validate, try activating or generating an offline activation request
		if (licenseStatus.getValue() > 0) {
			validLicense = true;
		} else {
			log.info("License not activated. Trying to activate the license");
			String activationAnswer = nalpeironHelper.getLicenseActivationAnswer();

			if (!StringUtils.isBlank(activationAnswer)) {
				licenseStatus = nalpeironHelper.importCertificate(nalpeironHelper.getLicenseCode(),
						activationAnswer);

				//if the license status ha s value below 0, then the current license could not validate, try getting a new one
				if (licenseStatus.getValue() <= 0) {
					// license import failed.
					log.info("The license could not be activated offline, import of DSLicenseActivationAnswer.txt failed");
				} else {
					log.info("The license has been activated offline, import of DSLicenseActivationAnswer.txt successful");
					validLicense = true;
				}
			} else if (!nalpeironHelper.isPassiveActivation()) {
				String activationRequest = nalpeironHelper.getLicenseActivationRequest();

				//if the current activation request is empty, or the file does not exist create it
				if (StringUtils.isBlank(activationRequest)) {
					activationRequest =
							nalpeironHelper.getActivationCertificateRequest(nalpeironHelper.getLicenseCode(),
									xmlRegInfo, "");

					nalpeironHelper.writeLicenseActivationRequest(activationRequest);
				}

				log.info("The license needs be activated offline, or you need to have an active internet connection. Activation request code written to DSLicenseActivationRequest.txt");
			}
		}

		return validLicense;
	}

	private boolean isValidLicenseOnline(String xmlRegInfo) throws DocShifterLicenseException {
		NalpeironHelper.LicenseStatus licenseStatus;
		boolean validLicense;
		log.debug("Connection to the Nalpeiron server could be established, will try online activation");

		//check current license status
		licenseStatus = nalpeironHelper.getLicenseStatus();

		// get a new heartbeat for license with the supplied license code
		NalpeironHelper.LicenseStatus newHeartbeatStatus = NalpeironHelper.LicenseStatus.PRODUNDETERMINED;
		try {
			newHeartbeatStatus = nalpeironHelper.getLicense(nalpeironHelper.getLicenseCode(), xmlRegInfo);
		} catch (DocShifterLicenseException ex) {
			log.debug("Could not retrieve new license heartbeat successfully. NALP ERRORCODE: {} NALP ERROR MESSAGE: " +
					"{}", ex.getNalpErrorCode(), ex.getNalpErrorMsg());
		}

		//if the license status or the heartbeat has value above 0, then the current license was validated
		if (licenseStatus.getValue() > 0 || newHeartbeatStatus.getValue() > 0) {
			log.info("The license has been activated online, or your trial is active");
			validLicense = true;
		} else {
			// license validation and hearth beat retrieval failed.
			log.info("The license could not be activated online, or your trial has expired");
			validLicense = false;
		}

		return validLicense;
	}

	private boolean isOnlineActivation() {
		if (nalpeironHelper.isOfflineActivation()) {
			log.debug("Offline activation mode has been set, will forgo all connection attempts to the server and will try to activate offline");
		}

		//test for online connection
		boolean hasConnection = !nalpeironHelper.isOfflineActivation();
		if (!nalpeironHelper.isOfflineActivation()) {
			try {
				nalpeironHelper.testNalpeironLicencingConnection(0, 0);
			} catch (DocShifterLicenseException e) {
				hasConnection = false;
				log.debug("No connection to the Nalpeiron server could be established, will try offline activation");
			}
		}

		return hasConnection;
	}
}

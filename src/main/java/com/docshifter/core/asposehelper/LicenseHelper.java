package com.docshifter.core.asposehelper;

import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.LocaleUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Log4j2
public class LicenseHelper {

	private static final String B64_15 = "UFU0UGtSUVZLc3N0NGpIYkRCMDB6WmRpRjI5VkMyVUtoaDJCcE1mQ0FpazNRMnFDWERJWTMrbmlo";
	private static final String B64_16 = "TlJMcmZUVnE2YXpNMVZtbzdqQ2FhbkpPN21PL2M3b0NKTWdOeUQzaE9lNDRDRTNzanZpSWdYSDlh";
	private static final String B64_9 = "ZHVjdHM+DQogICAgPEVkaXRpb25UeXBlPlByb2Zlc3Npb25hbDwvRWRpdGlvblR5cGU+DQogICAg";
	private static final String B64_10 = "PFNlcmlhbE51bWJlcj40MzRjMDcwNS02NDg4LTQ3MzYtYTVmMS0wYjQwNTA2ZTcxOTM8L1Nlcmlh";
	private static final String B64_17 = "MGJmUUtuWUp1LzlhTnRXSTVnL2U1QUcyNm0vczF1T3MwZDJIZWVqVjNuckozeVRRbzhhU0V2RXVF";
	private static final String B64_2 = "VG8+RG9jU2hpZnRlcjwvTGljZW5zZWRUbz4NCiAgICA8RW1haWxUbz5nZWVydC52YW5wZXRlZ2hl";
	private static final String B64_5 = "ZWQgRGVwbG95bWVudCBMb2NhdGlvbnM8L0xpY2Vuc2VOb3RlPg0KICAgIDxPcmRlcklEPjIyMTEx";
	private static final String B64_6 = "OTAwNTgwMjwvT3JkZXJJRD4NCiAgICA8VXNlcklEPjg0MDAxNTwvVXNlcklEPg0KICAgIDxPRU0+";
	private static final String B64_7 = "VGhpcyBpcyBhIHJlZGlzdHJpYnV0YWJsZSBsaWNlbnNlPC9PRU0+DQogICAgPFByb2R1Y3RzPg0K";
	private static final String B64_8 = "ICAgICAgPFByb2R1Y3Q+QXNwb3NlLlRvdGFsIGZvciBKYXZhPC9Qcm9kdWN0Pg0KICAgIDwvUHJv";
	private static final String B64_18 = "PTwvU2lnbmF0dXJlPg0KPC9MaWNlbnNlPg==";
	private static final String B64_3 = "bUBkb2NzaGlmdGVyLmNvbTwvRW1haWxUbz4NCiAgICA8TGljZW5zZVR5cGU+RGV2ZWxvcGVyIE9F";
	private static final String B64_4 = "TTwvTGljZW5zZVR5cGU+DQogICAgPExpY2Vuc2VOb3RlPjEgRGV2ZWxvcGVyIEFuZCBVbmxpbWl0";
	private static final String B64_11 = "bE51bWJlcj4NCiAgICA8U3Vic2NyaXB0aW9uRXhwaXJ5PjIwMjMxMjAzPC9TdWJzY3JpcHRpb25F";
	private static final String B64_1 = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8TGljZW5zZT4NCiAgPERhdGE+DQogICAgPExpY2Vuc2Vk";
	private static final String B64_12 = "eHBpcnk+DQogICAgPExpY2Vuc2VWZXJzaW9uPjMuMDwvTGljZW5zZVZlcnNpb24+DQogICAgPExp";
	private static final String B64_13 = "Y2Vuc2VJbnN0cnVjdGlvbnM+aHR0cHM6Ly9wdXJjaGFzZS5hc3Bvc2UuY29tL3BvbGljaWVzL3Vz";
	private static final String B64_14 = "ZS1saWNlbnNlPC9MaWNlbnNlSW5zdHJ1Y3Rpb25zPg0KICA8L0RhdGE+DQogIDxTaWduYXR1cmU+";


	private static final String B64 =
			B64_1 + B64_2 + B64_3 + B64_4 + B64_5 + B64_6 + B64_7 + B64_8 + B64_9 +
					B64_10 + B64_11 + B64_12 + B64_13 + B64_14 + B64_15 + B64_16 + B64_17 +
					B64_18;

	private final com.aspose.words.License wordsLicense;
	private final com.aspose.cells.License cellsLicense;
	private final com.aspose.slides.License slidesLicense;
	private final com.aspose.email.License emailLicense;
	private final com.aspose.pdf.License pdfLicense;
	private final com.aspose.imaging.License imgLicense;
	private final com.aspose.tasks.License tasksLicense;
	//private final com.aspose.ocr.License ocrLicense;
	private final com.aspose.diagram.License diagramLicense;
	private final com.aspose.cad.License cadLicense;
	private final com.aspose.html.License htmlLicense;
	private final com.aspose.barcode.License barcodeLicense;

	private static final LicenseHelper licenseHelper = new LicenseHelper(Executors.newSingleThreadScheduledExecutor());

	private LicenseHelper(ScheduledExecutorService sExe) {
		byte[] licenceBytes = Base64.getDecoder().decode(B64.getBytes(StandardCharsets.UTF_8));
		String tmpFileName = UUID.randomUUID().toString();
		String tmpDir = System.getProperty("java.io.tmpdir");
		Path tmpFileFullPath = Paths.get(tmpDir, tmpFileName);
		String tmpFileFullPathStr = tmpFileFullPath.toString();
		if (log.isDebugEnabled()) {
			Locale currentLocale = Locale.getDefault();
			log.debug("Current Locale: {},  country: {}, language: {}", currentLocale, currentLocale.getCountry(), currentLocale.getLanguage());
		}
		// A bad Locale may give rise to a CultureNotFoundException
		// so if it's not an available Locale, we hack the Locale that
		// Aspose sees
		if (!LocaleUtils.isAvailableLocale(Locale.getDefault())) {
			log.info("Seems your Locale: {} may not not be valid, so defaulting to en_US", Locale.getDefault());
			Locale workingLocale = new Locale("en", "US");
			com.aspose.email.CurrentThreadSettings.setLocale(workingLocale);
			com.aspose.slides.CurrentThreadSettings.setLocale(workingLocale);
			com.aspose.pdf.LocaleOptions.setLocale(workingLocale);
			com.aspose.words.CurrentThreadSettings.setLocale(workingLocale);
			com.aspose.imaging.CurrentThreadSettings.setLocale(workingLocale);
			com.aspose.cad.CurrentThreadSettings.setLocale(workingLocale);
			log.debug("Finished setting up thread-specific Locales");
		}
		try {
			Files.write(tmpFileFullPath, licenceBytes);
		}
		catch (IOException ioe) {
			log.error("Trying to write to: {}, we hit an IOException", tmpFileFullPath, ioe);
		}
		emailLicense = new com.aspose.email.License();
		slidesLicense = new com.aspose.slides.License();
		cellsLicense = new com.aspose.cells.License();
		wordsLicense = new com.aspose.words.License();
		pdfLicense = new com.aspose.pdf.License();
		imgLicense = new com.aspose.imaging.License();
		tasksLicense = new com.aspose.tasks.License();
		// Not yet using the Aspose OCR lib as it adds 200Mb to the Installer
		//ocrLicense = new com.aspose.ocr.License();
		diagramLicense = new com.aspose.diagram.License();
		cadLicense = new com.aspose.cad.License();
		htmlLicense = new com.aspose.html.License();
		barcodeLicense = new com.aspose.barcode.License();
		try {
			log.debug("Setting the licences");
			emailLicense.setLicense(tmpFileFullPathStr);
			slidesLicense.setLicense(tmpFileFullPathStr);
			cellsLicense.setLicense(tmpFileFullPathStr);
			wordsLicense.setLicense(tmpFileFullPathStr);
			pdfLicense.setLicense(tmpFileFullPathStr);
			imgLicense.setLicense(tmpFileFullPathStr);
			tasksLicense.setLicense(tmpFileFullPathStr);
			//log.debug("About to do OCR licence");
			//ocrLicense.setLicense(tmpFileFullPathStr);
			//log.debug("OCR licence valid is: " + ocrLicense.isValid());
			cadLicense.setLicense(tmpFileFullPathStr);
			diagramLicense.setLicense(tmpFileFullPathStr);
			htmlLicense.setLicense(tmpFileFullPathStr);
			barcodeLicense.setLicense(tmpFileFullPathStr);
		}
		catch (Exception ex) {
			log.error("An error occurred while loading in an Aspose license.", ex);
		}
		finally {
			FileUtils.deletePath(sExe, tmpFileFullPath, true);
		}

		if (log.isDebugEnabled()) {
			log.debug("Following Aspose versions are in use:");
			for (String feature : AsposeVersionUtil.getSupportedFeatures()) {
				log.debug(AsposeVersionUtil.getImplementationVersions(feature));
			}
		}
	}

	public static LicenseHelper getLicenseHelper() {
		return licenseHelper;
	}
}

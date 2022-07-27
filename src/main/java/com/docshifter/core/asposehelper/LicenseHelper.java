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

	private static final String B64_15 = "b24+DQogICAgPExpY2Vuc2VJbnN0cnVjdGlvbnM+aHR0cHM6Ly9wdXJjaGFzZS5h";
	private static final String B64_16 = "c3Bvc2UuY29tL3BvbGljaWVzL3VzZS1saWNlbnNlPC9MaWNlbnNlSW5zdHJ1Y3Rp";
	private static final String B64_9 = "ICAgPFByb2R1Y3RzPg0KICAgICAgPFByb2R1Y3Q+QXNwb3NlLlRvdGFsIGZvciBK";
	private static final String B64_10 = "YXZhPC9Qcm9kdWN0Pg0KICAgIDwvUHJvZHVjdHM+DQogICAgPEVkaXRpb25UeXBl";
	private static final String B64_17 = "b25zPg0KICA8L0RhdGE+DQogIDxTaWduYXR1cmU+ZGZraGNyWU81NVhEY2xhTVNo";
	private static final String B64_2 = "PExpY2Vuc2VkVG8+RG9jU2hpZnRlcjwvTGljZW5zZWRUbz4NCiAgICA8RW1haWxU";
	private static final String B64_5 = "PExpY2Vuc2VOb3RlPjEgRGV2ZWxvcGVyIEFuZCBVbmxpbWl0ZWQgRGVwbG95bWVu";
	private static final String B64_6 = "dCBMb2NhdGlvbnM8L0xpY2Vuc2VOb3RlPg0KICAgIDxPcmRlcklEPjIxMTExOTAw";
	private static final String B64_7 = "NTY1MTwvT3JkZXJJRD4NCiAgICA8VXNlcklEPjg0MDAxNTwvVXNlcklEPg0KICAg";
	private static final String B64_8 = "IDxPRU0+VGhpcyBpcyBhIHJlZGlzdHJpYnV0YWJsZSBsaWNlbnNlPC9PRU0+DQog";
	private static final String B64_20 = "bGU5SXNtTk1jalZYWGdMT3RlWGxNNE1pdmQ2M0ozN2hHL3hLY01jcy9IZHFrUURP";
	private static final String B64_21 = "cHh6bitJK0hzPTwvU2lnbmF0dXJlPg0KPC9MaWNlbnNlPg==";
	private static final String B64_18 = "NkZZYjBPRTdkUUxJYXpKN2NPMlBabGNJOVBhSmx5MGFQb3dsaC9WS0JWZkxBZWxz";
	private static final String B64_3 = "bz5nZWVydC52YW5wZXRlZ2hlbUBkb2NzaGlmdGVyLmNvbTwvRW1haWxUbz4NCiAg";
	private static final String B64_4 = "ICA8TGljZW5zZVR5cGU+RGV2ZWxvcGVyIE9FTTwvTGljZW5zZVR5cGU+DQogICAg";
	private static final String B64_11 = "PlByb2Zlc3Npb25hbDwvRWRpdGlvblR5cGU+DQogICAgPFNlcmlhbE51bWJlcj5j";
	private static final String B64_1 = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8TGljZW5zZT4NCiAgPERhdGE+DQogICAg";
	private static final String B64_12 = "OWMxYzNlNi00OWQ2LTQxY2YtODAyYy0yYzA4NzU3NTZmODQ8L1NlcmlhbE51bWJl";
	private static final String B64_13 = "cj4NCiAgICA8U3Vic2NyaXB0aW9uRXhwaXJ5PjIwMjIxMjAzPC9TdWJzY3JpcHRp";
	private static final String B64_14 = "b25FeHBpcnk+DQogICAgPExpY2Vuc2VWZXJzaW9uPjMuMDwvTGljZW5zZVZlcnNp";
	private static final String B64_19 = "c2twdFYvNXl5c3VsSFRDdUdKMkQ4L2tpK1Nka0EzMkNGa24wckpYN2dKRFFmMkR4";


	private static final String B64 =
			B64_1 + B64_2 + B64_3 + B64_4 + B64_5 + B64_6 + B64_7 + B64_8 + B64_9 +
					B64_10 + B64_11 + B64_12 + B64_13 + B64_14 + B64_15 + B64_16 + B64_17 +
					B64_18 + B64_19 + B64_20 + B64_21;

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

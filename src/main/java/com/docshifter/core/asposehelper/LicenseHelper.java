package com.docshifter.core.asposehelper;

import com.docshifter.core.utils.FileUtils;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.LocaleUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Log4j2
public class LicenseHelper {
	private static final String B64_11 = "PlByb2Zlc3Npb25hbDwvRWRpdGlvblR5cGU+DQogICAgPFNlcmlhbE51bWJlcj4y";
	private static final String B64_5 = "PExpY2Vuc2VOb3RlPjEgRGV2ZWxvcGVyIEFuZCBVbmxpbWl0ZWQgRGVwbG95bWVu";
	private static final String B64_13 = "cj4NCiAgICA8U3Vic2NyaXB0aW9uRXhwaXJ5PjIwMjQwNjI1PC9TdWJzY3JpcHRp";
	private static final String B64_8 = "IDxPRU0+VGhpcyBpcyBhIHJlZGlzdHJpYnV0YWJsZSBsaWNlbnNlPC9PRU0+DQog";
	private static final String B64_18 = "aUF3Y1JBNTh4V3IyU01XU1ZVVlRnYnJBdXMrbW54bjgxNlJLK2tnS1pWTm9uaDR3";
	private static final String B64_10 = "YXZhPC9Qcm9kdWN0Pg0KICAgIDwvUHJvZHVjdHM+DQogICAgPEVkaXRpb25UeXBl";
	private static final String B64_3 = "bz5nZWVydC52YW5wZXRlZ2hlbUBkb2NzaGlmdGVyLmNvbTwvRW1haWxUbz4NCiAg";
	private static final String B64_1 = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8TGljZW5zZT4NCiAgPERhdGE+DQogICAg";
	private static final String B64_7 = "NTkzOTwvT3JkZXJJRD4NCiAgICA8VXNlcklEPjg0MDAxNTwvVXNlcklEPg0KICAg";
	private static final String B64_12 = "NDhiOTdjOS01ZTAyLTRjNDItYTVkYS1iOTA0MDY1NTM2NjA8L1NlcmlhbE51bWJl";
	private static final String B64_21 = "SnJhVXJiZTgwPTwvU2lnbmF0dXJlPg0KPC9MaWNlbnNlPg==";
	private static final String B64_17 = "b25zPg0KICA8L0RhdGE+DQogIDxTaWduYXR1cmU+bDFNQkV3cjl1NlFwcTEyV2s1";
	private static final String B64_9 = "ICAgPFByb2R1Y3RzPg0KICAgICAgPFByb2R1Y3Q+QXNwb3NlLlRvdGFsIGZvciBK";
	private static final String B64_15 = "b24+DQogICAgPExpY2Vuc2VJbnN0cnVjdGlvbnM+aHR0cHM6Ly9wdXJjaGFzZS5h";
	private static final String B64_2 = "PExpY2Vuc2VkVG8+RG9jU2hpZnRlcjwvTGljZW5zZWRUbz4NCiAgICA8RW1haWxU";
	private static final String B64_4 = "ICA8TGljZW5zZVR5cGU+RGV2ZWxvcGVyIE9FTTwvTGljZW5zZVR5cGU+DQogICAg";
	private static final String B64_19 = "TkJMRDhwU1hvZ1RRRUNvT21TcEZ2aXdyRGNSd2pwR3FPcmRYRUN6TkRXT0FYQ2pI";
	private static final String B64_6 = "dCBMb2NhdGlvbnM8L0xpY2Vuc2VOb3RlPg0KICAgIDxPcmRlcklEPjIzMDMxMjAw";
	private static final String B64_16 = "c3Bvc2UuY29tL3BvbGljaWVzL3VzZS1saWNlbnNlPC9MaWNlbnNlSW5zdHJ1Y3Rp";
	private static final String B64_20 = "YU5qeUF6SWZqM05ra2V2MCtSSXVSaEdQbVlUaDB5UXIwMERscU5KYmVDdVRJbjUw";
	private static final String B64_14 = "b25FeHBpcnk+DQogICAgPExpY2Vuc2VWZXJzaW9uPjMuMDwvTGljZW5zZVZlcnNp";


	private static final String B64 =
			B64_1 + B64_2 + B64_3 + B64_4 + B64_5 + B64_6 + B64_7 + B64_8 + B64_9 +
					B64_10 + B64_11 + B64_12 + B64_13 + B64_14 + B64_15 + B64_16 + B64_17 +
					B64_18 + B64_19 + B64_20 + B64_21;

	private final com.aspose.words.License wordsLicense = new com.aspose.words.License();
	private final com.aspose.cells.License cellsLicense = new com.aspose.cells.License();
	private final com.aspose.slides.License slidesLicense = new com.aspose.slides.License();
	private final com.aspose.email.License emailLicense = new com.aspose.email.License();
	private final com.aspose.pdf.License pdfLicense = new com.aspose.pdf.License();
	private final com.aspose.imaging.License imgLicense = new com.aspose.imaging.License();
	private final com.aspose.tasks.License tasksLicense = new com.aspose.tasks.License();
	// Not yet using the Aspose OCR lib as it adds 200Mb to the Installer
	//private final com.aspose.ocr.License ocrLicense = new com.aspose.ocr.License();
	private final com.aspose.diagram.License diagramLicense = new com.aspose.diagram.License();
	private final com.aspose.cad.License cadLicense = new com.aspose.cad.License();
	private final com.aspose.html.License htmlLicense = new com.aspose.html.License();
	private final com.aspose.barcode.License barcodeLicense = new com.aspose.barcode.License();

	private static final LicenseHelper licenseHelper;

	static {
		try {
			licenseHelper = new LicenseHelper(Executors.newSingleThreadScheduledExecutor());
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private LicenseHelper(ScheduledExecutorService sExe) throws Exception {
		byte[] licenceBytes = Base64.getDecoder().decode(B64.getBytes(StandardCharsets.UTF_8));
		String tmpFileName = UUID.randomUUID().toString();
		try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
			log.debug("Setting licences using the new VFS approach");
			Path licFolder = fs.getPath("/lic");
			Files.createDirectory(licFolder);
			Path vfsLicFile = licFolder.resolve(tmpFileName);
			setLicences(vfsLicFile, licenceBytes);
		}
		catch (Exception exc) {
			log.warn("We got an Exception trying to use the VFS! Switched to the old method, using tmp dir", exc);
			Path tmpLicFile = Paths.get(System.getProperty("java.io.tmpdir"), tmpFileName);
			try {
				setLicences(tmpLicFile, licenceBytes);
			}
			finally {
				FileUtils.deletePath(sExe, tmpLicFile, true);
			}
		}
	}

	/**
	 * Sets the locale on the licences where it's possible to do so
	 * This is used where for e.g.the default Locale is invalid, like en-BE or so
	 */
	private void setLocales() {
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
			com.aspose.imaging.LocaleOptions.setLocale(workingLocale);
			com.aspose.cad.CurrentThreadSettings.setLocale(workingLocale);
			log.debug("Finished setting up thread-specific Locales");
		}
	}

	/**
	 * Factored-out code to actually set the licences, given the licence file which may
	 * be fromm a VFS, or if that didn't work, some temp file...
	 * @param licFile A VFS file or a file from the temp dir to contain the licence bytes
	 * @param licenceBytes A byte[] of the actual bytes making up the Asspose licence
	 * @throws Exception if anything goes wrong setting the licences (I/O or Asspose exception)
	 */
	private void setLicences(Path licFile, byte[] licenceBytes) throws Exception {
		setLocales();
		try {
			Files.write(licFile, licenceBytes);
		}
		catch (IOException ioe) {
			log.error("Trying to write to: {}, we hit an IOException", licFile, ioe);
		}

		log.debug("Setting the licences");
		emailLicense.setLicense(Files.newInputStream(licFile));
		slidesLicense.setLicense(Files.newInputStream(licFile));
		cellsLicense.setLicense(Files.newInputStream(licFile));
		wordsLicense.setLicense(Files.newInputStream(licFile));
		pdfLicense.setLicense(Files.newInputStream(licFile));
		imgLicense.setLicense(Files.newInputStream(licFile));
		tasksLicense.setLicense(Files.newInputStream(licFile));
		//log.debug("About to do OCR licence");
		//ocrLicense.setLicense(tmpFileFullPathStr);
		//log.debug("OCR licence valid is: " + ocrLicense.isValid());
		cadLicense.setLicense(Files.newInputStream(licFile));
		diagramLicense.setLicense(Files.newInputStream(licFile));
		htmlLicense.setLicense(Files.newInputStream(licFile));
		barcodeLicense.setLicense(Files.newInputStream(licFile));
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

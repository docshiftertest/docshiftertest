package com.docshifter.core.asposehelper;

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

@Log4j2
public class LicenseHelper {

	private static final String B64_5 =  "ZW5zZVR5cGU+RGV2ZWxvcGVyIE9FTTwvTGljZW5zZVR5cGU+DQ";
	private static final String B64_P =  "RUVPN0R1N3JZUHNJTXo5aXFBeWVScjI0Rzk3WUY4SlZhODZuTi";
	private static final String B64_M =  "AgPFNpZ25hdHVyZT53YmxPWkJLZ3NRcDR3dElCZkErbFdhcTdi";
	private static final String B64_4 =  "hlbUBkb2NzaGlmdGVyLmNvbTwvRW1haWxUbz4NCiAgICA8TGlj";
	private static final String B64_2 =  "RhdGE+DQogICAgPExpY2Vuc2VkVG8+RG9jU2hpZnRlcjwvTGlj";
	private static final String B64_A =  "AgPE9FTT5UaGlzIGlzIGEgcmVkaXN0cmlidXRhYmxlIGxpY2Vu";
	private static final String B64_N =  "blRtUnc0V1VadWc2Uk1JVklRRlZSbng5SnNoTXZPVjlka3VWem";
	private static final String B64_Q =  "tuZXErVWUrUjU2elQ5RDMzTDlvUGpIb0tXekZJWmZiUmc9PC9T";
	private static final String B64_8 =  "5vdGU+DQogICAgPE9yZGVySUQ+MjAxMTIwMTE1NjI5PC9PcmRl";
	private static final String B64_I =  "AgICA8TGljZW5zZVZlcnNpb24+My4wPC9MaWNlbnNlVmVyc2lv";
	private static final String B64_O =  "JaV1RIeW5BdTU1VTdOV3Y3Y0YxRFBvbXc1alVXRVo1emZMOHJB";
	private static final String B64_6 =  "ogICAgPExpY2Vuc2VOb3RlPk9uZSBEZXZlbG9wZXIgQW5kIFVu";
	private static final String B64_H =  "eHBpcnk+MjAyMTEyMDM8L1N1YnNjcmlwdGlvbkV4cGlyeT4NCi";
	private static final String B64_L =  "c2U8L0xpY2Vuc2VJbnN0cnVjdGlvbnM+DQogIDwvRGF0YT4NCi";
	private static final String B64_1 =  "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8TGljZW5zZT4NCiAgPE";
	private static final String B64_D =  "ICAgPC9Qcm9kdWN0cz4NCiAgICA8RWRpdGlvblR5cGU+UHJvZm";
	private static final String B64_7 =  "bGltaXRlZCBEZXBsb3ltZW50IExvY2F0aW9uczwvTGljZW5zZU";
	private static final String B64_F =  "YmVyPjU4YmZmZjU2LTQ3ODQtNDBhZS1iZGM5LWE5NjJkNDRmNm";
	private static final String B64_C =  "VjdD5Bc3Bvc2UuVG90YWwgZm9yIEphdmE8L1Byb2R1Y3Q+DQog";
	private static final String B64_K =  "B1cmNoYXNlLmFzcG9zZS5jb20vcG9saWNpZXMvdXNlLWxpY2Vu";
	private static final String B64_G =  "E0NDwvU2VyaWFsTnVtYmVyPg0KICAgIDxTdWJzY3JpcHRpb25F";
	private static final String B64_9 =  "cklEPg0KICAgIDxVc2VySUQ+ODQwMDE1PC9Vc2VySUQ+DQogIC";
	private static final String B64_B =  "c2U8L09FTT4NCiAgICA8UHJvZHVjdHM+DQogICAgICA8UHJvZH";
	private static final String B64_3 =  "ZW5zZWRUbz4NCiAgICA8RW1haWxUbz5nZWVydC52YW5wZXRlZ2";
	private static final String B64_E =  "Vzc2lvbmFsPC9FZGl0aW9uVHlwZT4NCiAgICA8U2VyaWFsTnVt";
	private static final String B64_J =  "bj4NCiAgICA8TGljZW5zZUluc3RydWN0aW9ucz5odHRwczovL3";
	private static final String B64_R =  "aWduYXR1cmU+DQo8L0xpY2Vuc2U+";
	private static final String B64 =
			B64_1 + B64_2 + B64_3 + B64_4 + B64_5 + B64_6 + B64_7 + B64_8 + B64_9 +
					B64_A + B64_B + B64_C + B64_D + B64_E + B64_F + B64_G + B64_H + B64_I +
					B64_J + B64_K + B64_L + B64_M + B64_N + B64_O + B64_P + B64_Q + B64_R;

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

	private static final LicenseHelper licenseHelper = new LicenseHelper();

	private LicenseHelper() {
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
			Files.deleteIfExists(tmpFileFullPath);
		}
		catch (Exception ex) {
			log.error("An error occurred while loading in an Aspose license.", ex);
		}

		log.debug("Following Aspose versions are in use:");
		for (String feature : AsposeVersionUtil.getSupportedFeatures()) {
			log.debug(AsposeVersionUtil.getImplementationVersions(feature));
		}
	}

	public static LicenseHelper getLicenseHelper() {
		return licenseHelper;
	}
}

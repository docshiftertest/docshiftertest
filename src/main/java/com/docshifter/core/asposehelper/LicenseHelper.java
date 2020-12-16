package com.docshifter.core.asposehelper;

/**
 * Created by samnang.nop on 21/02/2017.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class LicenseHelper {

	private static final Logger logger = LoggerFactory.getLogger(LicenseHelper.class);
	private static final LicenseHelper licenseHelper = new LicenseHelper();
	private final com.aspose.words.License wordsLicense;
	private final com.aspose.cells.License cellsLicense;
	private final com.aspose.slides.License slidesLicense;
	private final com.aspose.email.License emailLicense;
	private final com.aspose.pdf.License pdfLicense;
	private final com.aspose.imaging.License imgLicense;
	private final com.aspose.tasks.License tasksLicense;
	private final com.aspose.ocr.License ocrLicense;
	private final com.aspose.diagram.License diagramLicense;
	private final com.aspose.cad.License cadLicense;


	private static final String lic = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<License>\n" +
			"<Data>\n" +
			"<LicensedTo>DocShifter</LicensedTo>\n" +
			"<EmailTo>geert.vanpeteghem@docshifter.com</EmailTo>\n" +
			"<LicenseType>Developer OEM</LicenseType>\n" +
			"<LicenseNote>Limited to 1 developer, unlimited physical locations</LicenseNote>\n" +
			"<OrderID>191125041947</OrderID>\n" +
			"<UserID>135031188</UserID>\n" +
			"<OEM>This is a redistributable license</OEM>\n" +
			"<Products>\n" +
			"<Product>Aspose.Total for Java</Product>\n" +
			"</Products>\n" +
			"<EditionType>Enterprise</EditionType>\n" +
			"<SerialNumber>9fd10bea-38f9-4030-aec6-4050242f483d</SerialNumber>\n" +
			"<SubscriptionExpiry>20201203</SubscriptionExpiry>\n" +
			"<LicenseVersion>3.0</LicenseVersion>\n" +
			"<LicenseInstructions>https://purchase.aspose.com/policies/use-license</LicenseInstructions>\n" +
			"</Data>\n" +
			"<Signature>YkLNPD+g9khxOs+7ForIVarr3pWCXoSrxA+gBtJwJJuXCAsilcDMpraRluycDmmAl0jBnFNasUgtnEokxAh9JwsDtHYkrfH5pyApnc8mixWa6YePPRmwT7ld5h5norU+2ycDmDz2s/890Vlt1qy6x/oAU3hEMtT1x8f/+UkhtUI=</Signature>\n" +
			"</License>";

	public static void main(String[] arghs) {
		if (arghs.length > 0) {
			LicenseHelper.licenseReader(Paths.get(arghs[0]));
		}
		else {
			System.out.println("You have to give a lic file path as an argument if you want to use the handy lic file reader!");
		}
	}
	
	private LicenseHelper() {

		emailLicense = new com.aspose.email.License();
		slidesLicense = new com.aspose.slides.License();
		cellsLicense = new com.aspose.cells.License();
		wordsLicense = new com.aspose.words.License();
		pdfLicense = new com.aspose.pdf.License();
		imgLicense = new com.aspose.imaging.License();
		tasksLicense = new com.aspose.tasks.License();
		ocrLicense = new com.aspose.ocr.License();
		diagramLicense = new com.aspose.diagram.License();
		cadLicense = new com.aspose.cad.License();

		try (InputStream is = new ByteArrayInputStream(lic.getBytes());
		     InputStream is2 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is3 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is4 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is5 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is6 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is7 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is8 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is9 = new ByteArrayInputStream(lic.getBytes());
		     InputStream is10 = new ByteArrayInputStream(lic.getBytes())) {

			emailLicense.setLicense(is);
			slidesLicense.setLicense(is2);
			cellsLicense.setLicense(is3);
			wordsLicense.setLicense(is4);
			pdfLicense.setLicense(is5);
			imgLicense.setLicense(is6);
			tasksLicense.setLicense(is7);
			ocrLicense.setLicense(is8);
			cadLicense.setLicense(is9);
			diagramLicense.setLicense(is10);
		} catch (Exception ex) {
			logger.error("An error occurred while loading in an Aspose license.", ex);
		}

		logger.debug("Following Aspose versions are in use:");
		for (String feature : AsposeVersionUtil.getSupportedFeatures()) {
			logger.debug(AsposeVersionUtil.getImplementationVersions(feature));
		}
	}

	public static LicenseHelper getLicenseHelper() {
		return licenseHelper;
	}
	
	public static void licenseReader(Path licFilePath) {
		List<String> licLines;
		try {
			licLines = Files.readAllLines(licFilePath);
			System.out.println("\tprivate static final String lic = \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?>\\n\" +");
			for (String licLine : licLines) {
				if (!licLine.startsWith("</License>")) {
					System.out.println("\t\t\t\"" + licLine.trim() + "\\n\" +");
				}
				else {
					System.out.println("\t\t\t\"" + licLine.trim() + "\";");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

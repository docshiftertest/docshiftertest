package com.docbyte.docshifter.util.aspose;

import java.io.File;
import java.io.FileInputStream;


public final class LicenseHelper {
	
	private static final LicenseHelper licenceHelper = new LicenseHelper();
	private final com.aspose.email.License emailLicense;
	private final com.aspose.pdf.License pdfLicense;
	
	private LicenseHelper() {
		emailLicense = new com.aspose.email.License();
		pdfLicense = new com.aspose.pdf.License();
		try {
			emailLicense.setLicense(new FileInputStream(new File("./lib/Aspose.Total.lic")));
			pdfLicense.setLicense(new FileInputStream(new File("./lib/Aspose.Total.lic")));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static LicenseHelper getLicenceHelper() {
		return licenceHelper;
	}
	
}

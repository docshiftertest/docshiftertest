package com.docbyte.docshifter.util.aspose;

import java.io.File;
import java.io.FileInputStream;


public final class LicenseHelper {
	
	private static final LicenseHelper licenceHelper = new LicenseHelper();
	private final com.aspose.email.License emailLicense;
	
	private LicenseHelper() {
		emailLicense = new com.aspose.email.License();
		try {
			emailLicense.setLicense(new FileInputStream(new File("Aspose.Total.lic")));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static LicenseHelper getLicenceHelper() {
		return licenceHelper;
	}
	
}

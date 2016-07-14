package com.docbyte.docshifter.utils.aspose;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public final class LicenseHelper {

	private static final LicenseHelper licenceHelper = new LicenseHelper();
	private final com.aspose.email.License emailLicense;
	private final com.aspose.pdf.License pdfLicense;

	private final String lic = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<License>\n" +
			"  <Data>\n" +
			"    <LicensedTo>Docbyte</LicensedTo>\n" +
			"    <EmailTo>admin@docbyte.com</EmailTo>\n" +
			"    <LicenseType>Developer OEM</LicenseType>\n" +
			"    <LicenseNote>Limited to 1 developer, unlimited physical locations</LicenseNote>\n" +
			"    <OrderID>151022111459</OrderID>\n" +
			"    <UserID>66843</UserID>\n" +
			"    <OEM>This is a redistributable license</OEM>\n" +
			"    <Products>\n" +
			"      <Product>Aspose.Total for Java</Product>\n" +
			"    </Products>\n" +
			"    <EditionType>Enterprise</EditionType>\n" +
			"    <SerialNumber>d0924a68-1356-4e8e-b5bb-e4808f2a5b69</SerialNumber>\n" +
			"    <SubscriptionExpiry>20161104</SubscriptionExpiry>\n" +
			"    <LicenseVersion>3.0</LicenseVersion>\n" +
			"    <LicenseInstructions>http://www.aspose.com/corporate/purchase/license-instructions.aspx</LicenseInstructions>\n" +
			"  </Data>\n" +
			"  <Signature>bd/lvMGHK2sWD1LIDqeRwVey9FipX0SP9E/enHSDOHqmND6Ehhm2xDY/JQ3iduSguyhTYVD2Wjn7DxzA1BNxWPOgkwtmc+XgmBqOGBZ+ZVqCuFNVFMNdfcTIiQKRbysFusQ/0NKcTO3KbzTfSTLoXM5NLyuPmBLncH6RDbWxUdc=</Signature>\n" +
			"</License>";

	private LicenseHelper() {


		emailLicense = new com.aspose.email.License();
		pdfLicense = new com.aspose.pdf.License();

		try {
			InputStream is = new ByteArrayInputStream(lic.getBytes());
			emailLicense.setLicense(is);
			InputStream is2 = new ByteArrayInputStream(lic.getBytes());
			pdfLicense.setLicense(is2);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static LicenseHelper getLicenceHelper() {
		return licenceHelper;
	}

}

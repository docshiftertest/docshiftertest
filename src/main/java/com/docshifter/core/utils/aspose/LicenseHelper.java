package com.docshifter.core.utils.aspose;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public final class LicenseHelper {

    private static final LicenseHelper licenceHelper = new LicenseHelper();

    private static final String lic = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<License>\n" +
            "  <Data>\n" +
            "    <LicensedTo>Docbyte</LicensedTo>\n" +
            "    <EmailTo>admin@docbyte.com</EmailTo>\n" +
            "    <LicenseType>Developer OEM</LicenseType>\n" +
            "    <LicenseNote>Limited to 1 developer, unlimited physical locations</LicenseNote>\n" +
            "    <OrderID>160831043939</OrderID>\n" +
            "    <UserID>66843</UserID>\n" +
            "    <OEM>This is a redistributable license</OEM>\n" +
            "    <Products>\n" +
            "      <Product>Aspose.Total for Java</Product>\n" +
            "    </Products>\n" +
            "    <EditionType>Enterprise</EditionType>\n" +
            "    <SerialNumber>1cd0a4c6-85ae-43d5-8e64-61d6391f04f6</SerialNumber>\n" +
            "    <SubscriptionExpiry>20171104</SubscriptionExpiry>\n" +
            "    <LicenseVersion>3.0</LicenseVersion>\n" +
            "    <LicenseInstructions>http://www.aspose.com/corporate/purchase/license-instructions.aspx</LicenseInstructions>\n" +
            "  </Data>\n" +
            "  <Signature>jH5BspRua7Ip0klxmDnUdVLXw0PmfnO/t11DyJNG5OIqIZXaKJl0XgPccYyUlwnVoAiOUvfagKXArApPnVjJ9ng+VUAOCXjOjPVZuk492yhtUwBvCEOz65pDJ84dnzofcsbllTi6KJ5499aXAivb/5tw6FL0yVZHKE0qlB7M07A=</Signature>\n" +
            "</License>";

    private LicenseHelper() {
    }

    public static LicenseHelper getLicenceHelper() {
        return licenceHelper;
    }

    public static String getAsposeLicense(){
        return lic;
    }

}
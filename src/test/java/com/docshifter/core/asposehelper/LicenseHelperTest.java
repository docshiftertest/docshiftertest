package com.docshifter.core.asposehelper;

import com.aspose.pdf.Document;
import com.aspose.pdf.PdfFormat;
import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.exceptions.IndexOutOfRangeException;
import lombok.extern.log4j.Log4j2;
import org.junit.Ignore;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Log4j2
public class LicenseHelperTest {

	@Test
	public void testLicenseHelper() {
		LicenseHelper helper = LicenseHelper.getLicenseHelper();
		assertNotNull(helper);
		String pdfPathStr = "target/test-classes/work/Test document 1A.pdf";
		Path pdfPath = Paths.get(pdfPathStr);
		try (Document pdf = new Document("target/test-classes/Test document.pdf")) {
			pdf.convert("Conversion_Log.xml", PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
			pdf.save(pdfPathStr);
			assertTrue("The output file must exist", Files.exists(pdfPath));
		} catch (IndexOutOfRangeException iore) {
			fail("We got an index out of range exception, meaning the Aspose licence is probably not good!");
		}
	}

	@Ignore("This test is useless if the licenseHelper already made an instance")
	@Test
	public void testLicenseHelperBadLocale() {
		Locale.setDefault(new Locale("en", "BE"));
		LicenseHelper helper = LicenseHelper.getLicenseHelper();
		assertNotNull(helper);
		String pdfPathStr = "target/test-classes/work/Test document 1A.pdf";
		Path pdfPath = Paths.get(pdfPathStr);
		try (Document pdf = new Document("target/test-classes/Test document.pdf")) {
			pdf.convert("Conversion_Log.xml", PdfFormat.PDF_A_1A, ConvertErrorAction.Delete);
			pdf.save(pdfPathStr);
			assertTrue("The output file must exist", Files.exists(pdfPath));
		} catch (IndexOutOfRangeException iore) {
			fail("We got an index out of range exception, meaning the Aspose licence is probably not good!");
		}
	}

}

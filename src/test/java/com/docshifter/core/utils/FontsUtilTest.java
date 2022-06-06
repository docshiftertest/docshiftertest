package com.docshifter.core.utils;

import com.docshifter.core.metrics.entities.Dashboard;
import com.docshifter.core.metrics.entities.DocumentFonts;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FontsUtilTest {


    @Test
    public void testExtractExcelFonts() throws Exception {

        List<DocumentFonts> fontList = null;

        fontList = FontsUtil.extractExcelFonts("target/test-classes/excelFonts.csv", true, new Dashboard());

        assertNotNull("The list of fonts should not be null" ,fontList);
        assertTrue("The list of fonts must not be empty", fontList.size() > 0);

    }

    @Test
    public void testExtractWordFonts() throws Exception {

        List<DocumentFonts> fontList = null;

        fontList = FontsUtil.extractWordFonts("target/test-classes/wordFonts.docx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertTrue("The list of fonts must not be empty", fontList.size() > 0);

    }

    @Test
    public void testExtractPPTFonts() throws Exception {
        List<DocumentFonts> fontList = null;

        fontList = FontsUtil.extractPptFonts("target/test-classes/pptFonts.pptx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertTrue("The list of fonts must not be empty", fontList.size() > 0);
    }

    @Test
    public void testExtractPdfFonts() throws Exception {
        List<DocumentFonts> fontList = null;

        fontList = FontsUtil.extractPDFFonts("target/test-classes/pdfFonts.pdf", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertTrue("The list of fonts must not be empty", fontList.size() > 0);
    }
}

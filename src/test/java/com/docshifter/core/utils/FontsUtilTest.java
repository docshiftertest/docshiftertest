package com.docshifter.core.utils;

import com.docshifter.core.asposehelper.LicenseHelper;
import com.docshifter.core.metrics.entities.Dashboard;
import com.docshifter.core.metrics.entities.DocumentFonts;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Log4j2
public class FontsUtilTest {

    @Before
    public void beforeTest() {
        LicenseHelper.getLicenseHelper();
    }

    @Test
    public void testExtractExcelFonts() {
        List<DocumentFonts> fontList;

        fontList = FontsUtil.extractExcelFonts("target/test-classes/arialOnly.xlsx", true, new Dashboard());

        assertNotNull("The list of fonts should not be null", fontList);
        assertEquals("The size of the list must be 1", 1, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", "Arial", fontList.get(0).getFontName());

        fontList = FontsUtil.extractExcelFonts("target/test-classes/4Fonts.xlsx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 4, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", 4, fontList.stream().filter(font -> "Arial/Tahoma/Times New Roman/Calibri".contains(font.getFontName())).count());

    }

    @Test
    public void testExtractWordFonts() {
        List<DocumentFonts> fontList;

        fontList = FontsUtil.extractWordFonts("target/test-classes/arialOnly.docx", true, new Dashboard());

        assertNotNull("The list of fonts should not be null", fontList);
        assertEquals("The size of the list must be 1", 1, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", "Arial", fontList.get(0).getFontName());

        fontList = FontsUtil.extractWordFonts("target/test-classes/5Fonts.docx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 5, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", 5, fontList.stream().filter(font -> "Arial/Tahoma/Times New Roman/Calibri/Forte".contains(font.getFontName())).count());

    }

    @Test
    public void testExtractPPTFonts() {
        List<DocumentFonts> fontList;

        fontList = FontsUtil.extractPptFonts("target/test-classes/arialOnly.pptx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 1, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", "Arial", fontList.get(0).getFontName());


        fontList = FontsUtil.extractPptFonts("target/test-classes/4Fonts.pptx", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 4, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", 4, fontList.stream().filter(font -> "Arial/Tahoma/Times New Roman/Calibri".contains(font.getFontName())).count());

    }

    @Test
    public void testExtractPdfFonts() {
        List<DocumentFonts> fontList;

        fontList = FontsUtil.extractPDFFonts("target/test-classes/arialOnly.pdf", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 1, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", "ArialMT", fontList.get(0).getFontName());

        fontList = FontsUtil.extractPDFFonts("target/test-classes/5Fonts.pdf", true, new Dashboard());

        assertNotNull("The list of fonts name must not be null", fontList);
        assertEquals("The list of fonts must not be empty", 5, fontList.size());
        assertEquals("The list of fonts must contains the right fonts", 5, fontList.stream().filter(font -> "ArialMT/Tahoma/TimesNewRomanPSMT/Calibri/ForteMT".contains(font.getFontName())).count());

    }
}

package com.docshifter.core.utils;

import com.aspose.pdf.Document;
import com.aspose.pdf.PdfFormat;
import com.docshifter.core.work.WorkFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PdfVersionUtilsTest {

    private WorkFolder workFolder;

    @BeforeEach
    public void before() {
        workFolder = new WorkFolder();
        workFolder.setFolder(Paths.get("target/test-classes/work"));
    }

    /**
     * Tests for the pdf output version with path as parameter
     * @param version output pdf version expected
     */
    @ParameterizedTest
    @ValueSource(strings = {"1.4", "1.5", "1.6", "1.7", "2.0", ""})
    public void testPdfOutputVersion(String version) {

        Path outFilePath = workFolder.getNewFilePath("test", "pdf");
        Document doc = new Document("target/test-classes/test_pdf.pdf");
        doc.save(outFilePath.toString());

        PdfVersionUtils.checkVersionAndConvertPdf(outFilePath, version, workFolder);

        if (version.isEmpty()) {
            version = "1.7";
        }

        assertEquals(version, getPdfVersion(outFilePath), "The version input should match");
    }

    /**
     * Tests for the pdf output version with Document as parameter
     * @param version output pdf version expected
     */
    @ParameterizedTest
    @ValueSource(strings = {"1.4", "1.5", "1.6", "1.7", "2.0", ""})
    public void testPdfOutputVersionWithDocument(String version) {

        Path outFilePath = workFolder.getNewFilePath("test", "pdf");
        Document doc = new Document("target/test-classes/test_pdf.pdf");
        doc.save(outFilePath.toString());

        doc = new Document(outFilePath.toString());

        PdfVersionUtils.checkVersionAndConvertPdf(doc, version, workFolder);
        doc.save(outFilePath.toString());
        doc.close();

        if (version.isEmpty()) {
            version = "1.7";
        }

        assertEquals(version, getPdfVersion(outFilePath), "The version input should match");
    }

    /**
     * Tests for the PdfFormat output version
     * @param postPdfOutput output pdf version expected
     */
    @ParameterizedTest
    @MethodSource("argumentsParsePdfOutputTypeWithComplianceLevel")
    public void testParsePdfOutputTypeWithComplianceLevel(String postPdfOutput, String complianceLevel, PdfFormat expectedPdfFormat) {
        PdfFormat pdfFormat = PdfVersionUtils.parsePdfOutputType(postPdfOutput, complianceLevel);
        assertEquals(expectedPdfFormat.getValue(), pdfFormat.getValue(),"The value should match");
    }

    private static Stream<Arguments> argumentsParsePdfOutputTypeWithComplianceLevel() {
        return Stream.of(
                arguments("1.4",        "",          PdfFormat.v_1_4),
                arguments("1.5",        "",          PdfFormat.v_1_5),
                arguments("1.6",        "",          PdfFormat.v_1_6),
                arguments("1.7",        "",          PdfFormat.v_1_7),
                arguments("2.0",        "",          PdfFormat.v_2_0),
                arguments("",           "",          PdfFormat.v_1_7),
                arguments("PDFA",       null,        PdfFormat.PDF_A_3B),
                arguments("PDFA",       "PDFA1A",    PdfFormat.PDF_A_1A),
                arguments("PDFA",       "1A",        PdfFormat.PDF_A_1A),
                arguments("PDFA",       "PDFA1B",    PdfFormat.PDF_A_1B),
                arguments("PDFA",       "1B",        PdfFormat.PDF_A_1B),
                arguments("PDFA",       "PDFA2A",    PdfFormat.PDF_A_2A),
                arguments("PDFA",       "2A",        PdfFormat.PDF_A_2A),
                arguments("PDFA",       "PDFA2B",    PdfFormat.PDF_A_2B),
                arguments("PDFA",       "2B",        PdfFormat.PDF_A_2B),
                arguments("PDFA",       "PDFA2U",    PdfFormat.PDF_A_2U),
                arguments("PDFA",       "2U",        PdfFormat.PDF_A_2U),
                arguments("PDFA",       "PDFA3A",    PdfFormat.PDF_A_3A),
                arguments("PDFA",       "3A",        PdfFormat.PDF_A_3A),
                arguments("PDFA",       "PDFA3U",    PdfFormat.PDF_A_3U),
                arguments("PDFA",       "3U",        PdfFormat.PDF_A_3U),
                arguments("PDFA",       "PDFAUA1",   PdfFormat.PDF_UA_1),
                arguments("PDFA",       "UA1",       PdfFormat.PDF_UA_1),
                arguments("PDFA",       "PDFAX1A",   PdfFormat.PDF_X_1A),
                arguments("PDFA",       "X1A",       PdfFormat.PDF_X_1A),
                arguments("PDFA",       "PDFAX3",    PdfFormat.PDF_X_3),
                arguments("PDFA",       "X3",        PdfFormat.PDF_X_3),
                arguments("PDFA",       "PDFA3B",    PdfFormat.PDF_A_3B),
                arguments("PDFA",       "3B",        PdfFormat.PDF_A_3B),
                arguments("PDF/A",      "3B",        PdfFormat.PDF_A_3B),
                arguments("PDF-A",      "3B",        PdfFormat.PDF_A_3B));
    }

    /**
     * Gets the version for a pdf file
     * @param resultPath path for the pdf file
     * @return the version for the pdf
     */
    private String getPdfVersion(Path resultPath) {
        try (Document doc = new Document(resultPath.toString())) {
            return doc.getVersion();
        }
    }

}
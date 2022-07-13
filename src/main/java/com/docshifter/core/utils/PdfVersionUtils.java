package com.docshifter.core.utils;

import com.aspose.pdf.ConvertErrorAction;
import com.aspose.pdf.Document;
import com.aspose.pdf.PdfFormat;
import com.docshifter.core.work.WorkFolder;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;

@Log4j2
public final class PdfVersionUtils {

    private static final int MAX_LOG_SIZE = 8096;

    /**
     * Checks and converts a pdf file to another version according to the input
     * @param outFilePath path for the converted file
     * @param postPdfOutput pdf version expected
     * @param wf folder for the task
     */
    public static void checkVersionAndConvertPdf(Path outFilePath, String postPdfOutput, WorkFolder wf) {

        try (Document doc = new Document(outFilePath.toString())) {

            String version = doc.getVersion();

            if (version.equalsIgnoreCase(postPdfOutput)) {
                log.info("The postPdfOutput [{}] choice is equal to the doc version [{}].",
                        postPdfOutput, version);
            }
            else {

                PdfFormat pdfOutputType = parsePdfOutputType(postPdfOutput);

                log.info("Trying to convert for different PDF version from {} to {}",
                        version, postPdfOutput);

                String conversionLogPath = wf.getNewFilePath("Conversion_log", "xml").toString();

                boolean converted = doc.convert(conversionLogPath, pdfOutputType, ConvertErrorAction.Delete);

                if (!converted) {

                    String conversionMessage = FileUtils.fileToString(
                            conversionLogPath,
                            "Transformation failed doing Conversion for version [" + pdfOutputType + "]: ",
                            MAX_LOG_SIZE);

                    log.warn(conversionMessage);
                }
                else {
                    log.info("The pdf was converted.");
                    doc.save(outFilePath.toString());
                }
            }
        }
    }

    /**
     * Checks and converts a pdf file to another version according to the input
     * @param doc pdf file to be checked
     * @param postPdfOutput pdf version expected
     * @param wf folder for the task
     */
    public static void checkVersionAndConvertPdf(Document doc, String postPdfOutput, WorkFolder wf) {

        String version = doc.getVersion();

        if (version.equalsIgnoreCase(postPdfOutput)) {
            log.info("The postPdfOutput [{}] choice is equal to the doc version [{}].",
                    postPdfOutput, version);
        }
        else {

            PdfFormat pdfOutputType = parsePdfOutputType(postPdfOutput);

            log.info("Trying to convert for different PDF version from {} to {}",
                    version, postPdfOutput);

            String conversionLogPath = wf.getNewFilePath("Conversion_log", "xml").toString();

            boolean converted = doc.convert(conversionLogPath, pdfOutputType, ConvertErrorAction.Delete);

            if (converted) {
                log.debug("The pdf was converted.");
            }
            else {

                String conversionMessage = FileUtils.fileToString(
                        conversionLogPath,
                        "Transformation failed doing Conversion for version [" + pdfOutputType + "]: ",
                        MAX_LOG_SIZE);

                log.warn(conversionMessage);
            }
        }
    }

    /**
     * Convenience method to figure out the PdfFormat we need to set, based on the postPdfOutput
     * param and the (optional) pdfAComplianceLevel param
     * @param postPdfOutput Choose 1.4, 1.5, 1.6, 1.7, 2.0 or PDFA
     * @param pdfAComplianceLevel Choose 1A, 1B, 2A, 2B, 2U, 3A, 3B, or 3U
     * @return PdfFormat representing the appropriate format
     */
    public static PdfFormat parsePdfOutputType(String postPdfOutput, String pdfAComplianceLevel) {
        PdfFormat result;

        if ("PDFA".equals(postPdfOutput.trim().toUpperCase().replaceAll("[/ _-]", ""))) {
            result = parsePdfAComplianceLevel(pdfAComplianceLevel);
        }
        else {
            result = parsePdfOutputType(postPdfOutput);
        }
        return result;
    }

    /**
     * Convenience method to figure out the PdfFormat we need to set, based on the postPdfOutput
     * @param postPdfOutput Choose 1.4, 1.5, 1.6, 1.7, 2.0
     * @return PdfFormat representing the appropriate format
     */
    private static PdfFormat parsePdfOutputType(String postPdfOutput) {
        PdfFormat  result = PdfFormat.v_1_7;

        switch (postPdfOutput.trim()) {
            case "1.4" -> result = PdfFormat.v_1_4;
            case "1.5" -> result = PdfFormat.v_1_5;
            case "1.6" -> result = PdfFormat.v_1_6;
            case "2.0" -> result = PdfFormat.v_2_0;
        }

        return result;
    }

    /**
     * Convenience method to get a PdfFormat based a given Compliance Level String
     * @param complianceLevel Choose 1A, 1B, 2A, 2B, 2U, 3A, 3B, or 3U
     * @return PdfFormat, default PDF/A-3b
     */
    private static PdfFormat parsePdfAComplianceLevel(String complianceLevel) {
        PdfFormat result = PdfFormat.PDF_A_3B;
        if (complianceLevel != null) {
            switch (complianceLevel.trim().toUpperCase().replaceAll("[/ _-]", "")) {
                case "PDFA1A", "1A" -> result = PdfFormat.PDF_A_1A;
                case "PDFA1B", "1B" -> result = PdfFormat.PDF_A_1B;
                case "PDFA2A", "2A" -> result = PdfFormat.PDF_A_2A;
                case "PDFA2B", "2B" -> result = PdfFormat.PDF_A_2B;
                case "PDFA2U", "2U" -> result = PdfFormat.PDF_A_2U;
                case "PDFA3A", "3A" -> result = PdfFormat.PDF_A_3A;
                case "PDFA3U", "3U" -> result = PdfFormat.PDF_A_3U;
                case "PDFAUA1", "UA1" -> result = PdfFormat.PDF_UA_1;
                case "PDFAX1A", "X1A" -> result = PdfFormat.PDF_X_1A;
                case "PDFAX3", "X3" -> result = PdfFormat.PDF_X_3;
                case "PDFA3B", "3B" -> {}
                default -> throw new UnsupportedOperationException("The following PDF compliance level is not supported: " + complianceLevel);
            }
        }

        return result;
    }

}

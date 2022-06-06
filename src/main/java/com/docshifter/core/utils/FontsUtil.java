package com.docshifter.core.utils;

import com.aspose.cells.Workbook;
import com.aspose.pdf.Font;
import com.aspose.slides.IFontData;
import com.aspose.slides.IFontsManager;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.FontInfo;
import com.aspose.words.FontInfoCollection;
import com.docshifter.core.metrics.entities.Dashboard;
import com.docshifter.core.metrics.entities.DocumentFonts;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
public class FontsUtil {

    public static List<DocumentFonts> extractDocumentFonts(String documentPath, boolean input, Dashboard dashboard) {

        String extension = FileUtils.getExtension(documentPath);

        switch (extension.toLowerCase()) {
            case "doc":
            case "docm":
            case "docx":
            case "dot":
            case "dotm":
            case "dotx":
            case "odt":
                log.debug("Identified a word document");
                return extractWordFonts(documentPath, input, dashboard);
            case "xls":
            case "xlsb":
            case "xlsm":
            case "xlsx":
            case "xlt":
            case "xltm":
            case "xltx":
            case "csv":
            case "ods":
                log.debug("Identified an excel document");
                return extractExcelFonts(documentPath, input, dashboard);
            case "ppt":
            case "pptm":
            case "pptx":
            case "pot":
            case "potm":
            case "potx":
                log.debug("Identified a presentation document");
                return extractPptFonts(documentPath, input, dashboard);
            case "pdf":
                log.debug("Identified a pdf document");
                return extractPDFFonts(documentPath, input, dashboard);
        }

        return null;
    }

    /**
     * Extracts the fonts from a ppt document
     * @param documentPath the path to the file
     * @param input if the file is input or output
     * @param dashboard the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractPptFonts(String documentPath, boolean input, Dashboard dashboard) {
        List<DocumentFonts> fontList = new ArrayList<>();

        try {
            Presentation ppt = new Presentation(documentPath);

            IFontsManager fm = ppt.getFontsManager();


            // TODO double-check with Julian

            IFontData[] fonts = fm.getEmbeddedFonts();

            if (fonts.length == 0 ){
                fonts = fm.getFonts();
            }

            for (IFontData fontInfo : fonts) {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(fontInfo.getFontName());

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);

            }
        } catch (Exception ex) {
            log.error("An exception occurred when trying to extract the presentation fonts", ex);
        }
        return fontList;

    }

    /**
     * Extracts the fonts from a word document
     * @param documentPath the path to the file
     * @param input if the file is input or output
     * @param dashboard the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractWordFonts(String documentPath, boolean input, Dashboard dashboard) {

        List<DocumentFonts> fontList = new ArrayList<>();
        try {
            Document document = new Document(documentPath);
            FontInfoCollection fonts = document.getFontInfos();

            // criar documento aqui com trycatch
            for (FontInfo fontInfo : fonts) {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(fontInfo.getName());
                dto.setAltFontName(fontInfo.getAltName());

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);

            }

        } catch (Exception e) {
            log.error("An exception occurred when trying to extract the word document fonts", e);
        }

        return fontList;

    }

    /**
     * Extracts the fonts from a pdf document
     * @param documentPath the path to the file
     * @param input if the file is input or output
     * @param dashboard the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractPDFFonts(String documentPath, boolean input, Dashboard dashboard) {

        List<DocumentFonts> fontList = new ArrayList<>();
        try {
            com.aspose.pdf.Document document = new com.aspose.pdf.Document(documentPath);

            Font[] allFc = document.getFontUtilities().getAllFonts();

            for (Font fontInfo : allFc) {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(fontInfo.getFontName());

                String decodedName = fontInfo.getFontName();
                try {
                    decodedName = fontInfo.getDecodedFontName();
                    if (!fontInfo.getFontName().equals(decodedName)) {
                        decodedName = decodedName.replace((char) 0x0, '.');
                    }
                } catch (StringIndexOutOfBoundsException sxe) {
                    log.error("An exception occurred when trying to get the decoded font name", sxe);
                    continue;
                }

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setFontName(decodedName);
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);

            }
        } catch (Exception ex) {
            log.error("An exception occurred when trying to extract the pdf document fonts", ex);
        }

        return fontList;
    }

    /**
     * Extracts the fonts from an excel document
     * @param documentPath the path to the file
     * @param input if the file is input or output
     * @param dashboard the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractExcelFonts(String documentPath, boolean input, Dashboard dashboard) {

        List<DocumentFonts> fontList = new ArrayList<>();

        try {
            Workbook document = new Workbook(documentPath);
            com.aspose.cells.Font[] fonts = document.getFonts();

            for (com.aspose.cells.Font fontInfo : fonts) {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(fontInfo.getName());

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);

            }

        } catch (Exception e) {
            log.error("An exception occurred when trying to extract the execl document fonts", e);
        }

        return fontList;
    }

}

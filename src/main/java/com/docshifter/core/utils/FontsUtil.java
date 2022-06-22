package com.docshifter.core.utils;

import com.aspose.cells.Cell;
import com.aspose.cells.FontSetting;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.pdf.Font;
import com.aspose.slides.IAutoShape;
import com.aspose.slides.IFontData;
import com.aspose.slides.IParagraph;
import com.aspose.slides.IPortion;
import com.aspose.slides.IPortionFormatEffectiveData;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Run;
import com.docshifter.core.metrics.entities.Dashboard;
import com.docshifter.core.metrics.entities.DocumentFonts;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class FontsUtil {

    public static List<DocumentFonts> extractDocumentFonts(List<String> documentPathList, boolean input, Dashboard dashboard) {


        List<DocumentFonts> totalDocumentFontsList = new ArrayList<>();

        documentPathList.forEach(documentPath -> {
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
                    totalDocumentFontsList.addAll(extractWordFonts(documentPath, input, dashboard));
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
                    totalDocumentFontsList.addAll(extractExcelFonts(documentPath, input, dashboard));
                case "ppt":
                case "pptm":
                case "pptx":
                case "pot":
                case "potm":
                case "potx":
                    log.debug("Identified a presentation document");
                    totalDocumentFontsList.addAll(extractPptFonts(documentPath, input, dashboard));
                case "pdf":
                    log.debug("Identified a pdf document");
                    totalDocumentFontsList.addAll(extractPDFFonts(documentPath, input, dashboard));
            }
        });

        return totalDocumentFontsList;
    }

    /**
     * Extracts the fonts from a ppt document
     *
     * @param documentPath the path to the file
     * @param input        if the file is input or output
     * @param dashboard    the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractPptFonts(String documentPath, boolean input, Dashboard dashboard) {
        List<DocumentFonts> fontList = new ArrayList<>();

        Presentation ppt = null;
        try {
            ppt = new Presentation(documentPath);
            Map<String, IFontData> foundFonts = new HashMap<>();

            // Goes through all slides and paragraphs to get the fonts.
            // To test 1 slide should use getSlides().get_item(index)
            ppt.getSlides().forEach(slide -> {
                for (Object shape : slide.getShapes()) {
                    if (shape instanceof IAutoShape) {
                        IAutoShape autoShape = (IAutoShape) shape;
                        for (IParagraph paragraph : autoShape.getTextFrame().getParagraphs()) {
                            // you can read paragraph format options (margin, space, tab, etc..):
//                        IParagraphFormatEffectiveData paragraphFormat = paragraph.getParagraphFormat().getEffective();

                            for (IPortion portion : paragraph.getPortions()) {
                                // you can read portion format options:

                                // Extract the font name
                                IPortionFormatEffectiveData portionFormat = portion.getPortionFormat().getEffective();
                                foundFonts.put(portionFormat.getLatinFont().getFontName(), portionFormat.getLatinFont());
                            }
                        }
                    }
                }
            });

            foundFonts.forEach((key, value) -> {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(key);

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);
            });

        } catch (Exception ex) {
            log.error("An exception occurred when trying to extract the presentation fonts", ex);
        } finally {
            if (ppt != null) {
                ppt.dispose();
            }
        }
        return fontList;
    }

    /**
     * Extracts the fonts from a word document
     *
     * @param documentPath the path to the file
     * @param input        if the file is input or output
     * @param dashboard    the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractWordFonts(String documentPath, boolean input, Dashboard dashboard) {
        Map<String, com.aspose.words.Font> foundFonts = new HashMap<>();
        Document doc = null;
        List<DocumentFonts> fontList = new ArrayList<>();

        try {
            doc = new Document(documentPath);
            NodeCollection<Run> theRuns = doc.getChildNodes(NodeType.RUN, true);

            theRuns.forEach(runny -> foundFonts.put(runny.getFont().getName(), //+
                    //runny.getFont().getNameOther() +
                    //runny.getFont().getNameFarEast(), // MAYBE use the Asian letters as the alternative name?
                    runny.getFont()));

            foundFonts.forEach((key, value) -> {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(value.getName());
                dto.setAltFontName(value.getNameOther());

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);
            });
        } catch (Exception e) {
            log.error("An exception occurred when creating the word document", e);
        }

        return fontList;
    }

    /**
     * Extracts the fonts from a pdf document
     *
     * @param documentPath the path to the file
     * @param input        if the file is input or output
     * @param dashboard    the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractPDFFonts(String documentPath, boolean input, Dashboard dashboard) {

        List<DocumentFonts> fontList = new ArrayList<>();

        try (final com.aspose.pdf.Document document = new com.aspose.pdf.Document(documentPath)) {
            Font[] allFc = document.getFontUtilities().getAllFonts();

            for (Font fontInfo : allFc) {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(fontInfo.getFontName());
                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
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
     *
     * @param documentPath the path to the file
     * @param input        if the file is input or output
     * @param dashboard    the dashboard to set the documentFont
     * @return the list of DocumentFonts
     */
    public static List<DocumentFonts> extractExcelFonts(String documentPath, boolean input, Dashboard dashboard) {
        List<DocumentFonts> fontList = new ArrayList<>();
        Map<String, com.aspose.cells.Font> foundFonts = new HashMap<>();

        Workbook document = null;

        try {
            document = new Workbook(documentPath);

            document.getWorksheets().forEach(sheet -> {
                ((Worksheet) sheet).getCells().forEach(cell -> {
                    // getCharacters can be null.
                    FontSetting[] fontSettings = ((Cell) cell).getCharacters();

                    // If found more than 1 font in a cell, got through all of them
                    if (fontSettings != null && fontSettings.length > 1) {
                        Arrays.stream(fontSettings).forEach(fontSetting -> {
                            com.aspose.cells.Font font = fontSetting.getFont();
                            foundFonts.put(font.getName(), font);
                        });
                    } // if not gets the main cell font
                    else {
                        com.aspose.cells.Font font = ((Cell) cell).getStyle().getFont();
                        foundFonts.put(font.getName(), font);
                    }
                });
            });

            foundFonts.forEach((key, value) -> {
                DocumentFonts dto = new DocumentFonts();

                dto.setFontName(key);

                dto.setDocumentName(Paths.get(documentPath).getFileName().toString());
                dto.setInput(input);
                dto.setDashboard(dashboard);
                dto.setId(UUID.randomUUID().toString());

                fontList.add(dto);
            });

        } catch (Exception e) {
            log.error("An exception occurred when trying to extract the execl document fonts", e);
        } finally {
            if (document != null) {
                document.dispose();
            }
        }
        return fontList;
    }

}

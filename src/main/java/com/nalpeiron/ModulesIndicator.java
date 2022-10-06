package com.nalpeiron;

public enum ModulesIndicator {

    // Transformation Modules
    ALL_TO_IMAGE(               "T0001", "All to Image Transformation"),
    ALL_TO_PDF(                 "T0002", "All to PDF Transformation"),
    AUDIO(                      "T0003", "Audio Transformation"),
    CELLS(                      "T0004", "Cells Transformation"),
    DIAGRAM(                    "T0005", "Diagram Transformation"),
    DOC_GEN(                    "T0006", "Document Generator for MS-WORD template file"),
    EMAIL(                      "T0007", "Email Transformation"),
    EPUB(                       "T0008", "Epub Transformation"),
    HIFI(                       "T0009", "HiFi Transformation"),
    PNG(                        "T0010", "Image to Png Transformation"),
    TIFF(                       "T0011", "Image to Tiff Transformation"),
    PDFA(                       "T0012", "PDFA Transformation"),
    INDEX(                      "T0013", "Index Transformation"),
    MERGE(                      "T0014", "Merge Transformation"),
    OCR(                        "T0015", "OCR Operation"),
    PDF(                        "T0016", "PDF Transformation"),
    PDF_OPTIMIZATION(           "T0017", "PDF Optimization"),
    PDF_OVERLAY(                "T0018", "PDF Overlay Transformation"),
    PDF_SECURITY(               "T0019", "PDF Security Transformation"),
    PREPROCESSING(              "T0020", "Preprocessing EmlToXml Transformation"),
    PROJECT(                    "T0021", "Project Transformation"),
    SLIDES(                     "T0022", "Slides Transformation"),
    THUMBNAIL(                  "T0023", "Thumbnail Transformation"),
    UNZIPPER(                   "T0024", "Unzip operation"),
    DOCUMENT_CHARACTERIZATION(  "T0025", "Document CharacterizationCheck"),
    DOCUMENT_HASH_CHECK(        "T0026", "Document HashCheck"),
    DOCUMENT_RENDITION_CHECK(   "T0027", "Document RenditionCheck"),
    VIDEO(                      "T0028", "Video Transformation"),
    WORDS(                      "T0029", "Words Transformation"),
    XML_DATA_LOAD(              "T0030", "XML Data Load"),
    XLS_TO_PDF(                 "T0031", "XLS to PDF"),
    XLST(                       "T0032", "XSLT transformation"),
    PDF_FORM_FILLER(            "T0033", "PDF Form Filler"),
    IMAGE_OVERLAY(              "T0034", "Overlay Operation"),
    SIGNING(                    "T0035", "Signing Transformation"),
    PDFA_VALIDATION(            "T0036", "PDF/A Validation"),
    COUNT_PAGES(                "T0037", "Count Pages Transformation"),
    SANTANDER_XML_FAC_XML(      "T0038", "SantanderXmlFacXmlTransform Operation"),
    NOP(                        "T0039", "No Operation"),
    TOC(                        "T0040", "Table of Contents Generator"),
    PDF_TO_TIFF(                "T0041", "PdfToTiff Transformation"),
    EP2AS(                      "T0042", "eP2AS Operation"),
    HASH_SNAPSHOT(              "T0043", "HashSnapshot"),
    IMAGE_TO_IMAGE(             "T0044", "Image to Image Transformation"),
    CAD(                        "T0045", "Cad Transformation"),
    HTML(                       "T0046", "Html Transformation"),
    SPLITTER(                   "T0047", "Splitter Transformation"),
    COVER_PAGE(                 "T0048", "Cover Page Transformation"),
    BARCODE_READER(             "T0049", "Barcode Reader Transformation"),

    // Release Modules
    DCTM_EXPORT(                "R0001", "Documentum Export Release"),
    DCTM_NEW_RENDITION(         "R0002", "Documentum New Rendition Release"),
    FS_EXPORT(                  "R0003", "FileSystem export"),
    SYNC_RETURN(                "R0004", "DocShifter Sync return module"),
    FTP_RELEASE(                "R0005", "FTP Release"),
    EMAIL_OUT(                  "R0006", "Email release"),
    VEEVA_EXPORT(               "R0007", "Veeva Export Release"),
    SHAREPOINT_RELEASE(         "R0008", "SharePoint Cloud Release"),
    REST_RELEASE(               "R0009", "Rest Release"),
    VEEVA_NEW_RENDITION(        "R0010", "Veeva New Rendition Release"),
    ;

    private final String id;
    private final String operation;

    ModulesIndicator(String id, String operation) {
        this.id = id;
        this.operation = operation;
    }

    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

}

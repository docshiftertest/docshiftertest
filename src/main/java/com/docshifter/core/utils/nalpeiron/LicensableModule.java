package com.docshifter.core.utils.nalpeiron;

/**
 * For the Mock Licensing Service, provides a way to license individual Modules
 */
public enum LicensableModule {
    OnStyle("T0090"),
    OnTrack("T0091"),
    R0010("R0010"),
    R0011("R0011"),
    R0012("R0012"),
    R0013("R0013"),
    R0014("R0014"),
    R0015("R0015"),
    R0016("R0016"),
    R0017("R0017"),
    R0018("R0018"),
    R0019("R0019"),
    R0020("R0020"),
    T0043("T0043"),
    T0044("T0044"),
    T0045("T0045"),
    T0046("T0046"),
    T0047("T0047"),
    T0048("T0048"),
    T0049("T0049"),
    T0050("T0050"),
    T0051("T0051"),
    T0052("T0052"),
    T0053("T0053"),
    T0054("T0054"),
    T0055("T0055"),
    T0056("T0056"),
    T0057("T0057"),
    T0058("T0058"),
    T0059("T0059"),
    T0060("T0060"),
    T0061("T0061"),
    T0062("T0062"),
    T0063("T0063"),
    T0064("T0064"),
    T0065("T0065"),
    T0066("T0066"),
    T0067("T0067"),
    T0068("T0068"),
    T0069("T0069"),
    T0070("T0070"),
    T0071("T0071"),
    T0072("T0072"),
    T0073("T0073"),
    T0074("T0074"),
    T0075("T0075"),
    T0076("T0076"),
    T0077("T0077"),
    T0078("T0078"),
    T0079("T0079"),
    T0080("T0080"),
    T0081("T0081"),
    T0082("T0082"),
    T0083("T0083"),
    T0084("T0084"),
    T0085("T0085"),
    T0086("T0086"),
    T0087("T0087"),
    T0088("T0088"),
    T0089("T0089"),
    T0092("T0092"),
    T0093("T0093"),
    T0094("T0094"),
    T0095("T0095"),
    T0096("T0096"),
    T0097("T0097"),
    T0098("T0098"),
    T0099("T0099"),
    T0100("T0100"),
    Custom_module("CSTM"),
    Metrics("METR"),
    Release_Sync_Return("R0004"),
    Release_Documentum_Export("R0001"),
    Release_Documentum_New_Rendition("R0002"),
    Release_Email_Release("R0006"),
    Release_Filesystem_Out("R0003"),
    Release_FTP_release("R0005"),
    Release_Sharepoint_Cloud("R0008"),
    Release_Veeva("R0007"),
    Release_Web_Service_Rest_Release("R0009"),
    Transformation_AllToImage("T0001"),
    Transformation_AllToPDF("T0002"),
    Transformation_Audio("T0003"),
    Transformation_Cells("T0004"),
    Transformation_Characterisation_Check("T0025"),
    Transformation_Count_Pages("T0037"),
    Transformation_Diagram("T0005"),
    Transformation_Document_Generation("T0006"),
    Transformation_Email("T0007"),
    Transformation_eP2AS_Validation("T0042"),
    Transformation_EPUB("T0008"),
    Transformation_Hash_Check("T0026"),
    Transformation_HiFi("T0009"),
    Transformation_Image_Overlay("T0034"),
    Transformation_Index("T0013"),
    Transformation_Merge("T0014"),
    Transformation_Nop("T0039"),
    Transformation_PDF("T0016"),
    Transformation_Pdf_Form_Filler("T0033"),
    Transformation_PDF_Optimization("T0017"),
    Transformation_PDF_Overlay("T0018"),
    Transformation_PDF_Security("T0019"),
    Transformation_PDFaValidation("T0036"),
    Transformation_PdfToTiff_Transformation("T0041"),
    Transformation_PNG("T0010"),
    Transformation_Preprocessing("T0020"),
    Transformation_Project("T0021"),
    Transformation_Rendition_Check("T0027"),
    Transformation_Santander_Custom_XML_transformation("T0038"),
    Transformation_Signing("T0035"),
    Transformation_Slides("T0022"),
    Transformation_Thumbnail("T0023"),
    // This is wrongly set up in Nalpeiron, should definitely be T0011
    //Transformation_TIFF("T0012"),
    Transformation_TIFF("T0011"),
    Transformation_ToC_Generator("T0040"),
    Transformation_Unzip("T0024"),
    Transformation_Video("T0028"),
    Transformation_Words("T0029"),
    Transformation_XML_to_PDF("T0031"),
    Transformation_XML_DATA_LOAD("T0030"),
    Transformation_XSL("T0032");
    
    private final String moduleId;

    LicensableModule(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString() {
        return this.moduleId;
    }
}
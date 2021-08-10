package com.docshifter.core.config.services;

import com.docshifter.core.utils.nalpeiron.LicensableModule;
import lombok.Getter;
import static com.docshifter.core.utils.nalpeiron.LicensableModule.*;

/**
 * For the Mock Licensing Service, provides a way to define sets of Modules for licensing, like we define Profiles in Nalpeiron
 */
@Getter
public enum MockLicensingModuleSet {
    DEV(Custom_module, Metrics,
            OnStyle, OnTrack,
            R0010, R0011, R0012, R0013, R0014, R0015, R0016, R0017, R0018, R0019, R0020,
            Release_Documentum_Export, Release_Documentum_New_Rendition, Release_Email_Release, Release_FTP_release, Release_Veeva,
            Release_Filesystem_Out, Release_Sharepoint_Cloud, Release_Sync_Return, Release_Web_Service_Rest_Release,
            Transformation_AllToImage, Transformation_AllToPDF, Transformation_Audio, Transformation_Cells,
            Transformation_Characterisation_Check, Transformation_Count_Pages, Transformation_Diagram, Transformation_Document_Generation,
            Transformation_Email, Transformation_eP2AS_Validation, Transformation_EPUB, Transformation_Hash_Check, Transformation_HiFi,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF, Transformation_Pdf_Form_Filler,
            Transformation_PDF_Optimization, Transformation_PDF_Overlay, Transformation_PDF_Security, Transformation_PdfToTiff_Transformation,
            Transformation_PDFaValidation, Transformation_PNG, Transformation_Preprocessing, Transformation_Project, Transformation_Rendition_Check,
            Transformation_Santander_Custom_XML_transformation, Transformation_Signing, Transformation_Slides, Transformation_Thumbnail,
            Transformation_TIFF, Transformation_ToC_Generator, Transformation_Unzip, Transformation_Video,
            Transformation_Words, Transformation_XML_to_PDF, Transformation_XSL,
            T0043, T0044, T0045, T0046, T0047, T0048, T0049,
            T0050, T0051, T0052, T0053, T0054, T0055, T0056, T0057, T0058, T0059,
            T0060, T0061, T0062, T0063, T0064, T0065, T0066, T0067, T0068, T0069,
            T0070, T0071, T0072, T0073, T0074, T0075, T0076, T0077, T0078, T0079,
            T0080, T0081, T0082, T0083, T0084, T0085, T0086, T0087, T0088, T0089,
            T0092, T0093, T0094, T0095, T0096, T0097, T0098, T0099, T0100),
    DocShifterBase(Release_Documentum_Export, Release_Documentum_New_Rendition, Release_Email_Release, Release_FTP_release,
            Release_Filesystem_Out, Release_Sync_Return, Release_Web_Service_Rest_Release,
            Transformation_AllToPDF,
            Transformation_Email,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF,
            Transformation_PDF_Overlay, Transformation_PDF_Security,
            Transformation_Thumbnail,
            Transformation_Unzip,
            Transformation_XML_to_PDF),
    DocShifterHiFi(Release_Documentum_Export, Release_Documentum_New_Rendition, Release_Email_Release, Release_FTP_release,
            Release_Filesystem_Out, Release_Sync_Return, Release_Web_Service_Rest_Release,
            Transformation_AllToPDF,
            Transformation_Email,
            Transformation_HiFi,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF,
            Transformation_PDF_Optimization, Transformation_PDF_Overlay, Transformation_PDF_Security,
            Transformation_PDFaValidation,
            Transformation_Thumbnail,
            Transformation_Unzip,
            Transformation_XML_to_PDF),
    DocShifterAdvanced(Custom_module,
            Release_Documentum_Export, Release_Documentum_New_Rendition, Release_Email_Release, Release_FTP_release,
            Release_Filesystem_Out, Release_Sync_Return, Release_Web_Service_Rest_Release,
            Transformation_AllToImage, Transformation_AllToPDF, Transformation_Audio, Transformation_Cells,
            Transformation_Count_Pages, Transformation_Diagram,
            Transformation_Email, Transformation_EPUB,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF,
            Transformation_PDF_Optimization, Transformation_PDF_Overlay, Transformation_PDF_Security,
            Transformation_PdfToTiff_Transformation,
            Transformation_PDFaValidation, Transformation_PNG, Transformation_Preprocessing, Transformation_Project,
            Transformation_Signing, Transformation_Slides, Transformation_Thumbnail,
            Transformation_TIFF, Transformation_Unzip, Transformation_Video,
            Transformation_Words, Transformation_XML_to_PDF, Transformation_XSL),
    NFR(Custom_module,
            Release_Documentum_Export, Release_Documentum_New_Rendition, Release_Email_Release, Release_FTP_release, Release_Veeva,
            Release_Filesystem_Out, Release_Sharepoint_Cloud, Release_Sync_Return, Release_Web_Service_Rest_Release,
            Transformation_AllToImage, Transformation_AllToPDF, Transformation_Audio, Transformation_Cells,
            Transformation_Characterisation_Check, Transformation_Count_Pages, Transformation_Diagram, Transformation_Document_Generation,
            Transformation_Email, Transformation_EPUB, Transformation_Hash_Check, Transformation_HiFi,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF, Transformation_Pdf_Form_Filler,
            Transformation_PDF_Optimization, Transformation_PDF_Overlay, Transformation_PDF_Security, Transformation_PdfToTiff_Transformation,
            Transformation_PDFaValidation, Transformation_PNG, Transformation_Preprocessing, Transformation_Project, Transformation_Rendition_Check,
            Transformation_Signing, Transformation_Slides, Transformation_Thumbnail,
            Transformation_TIFF, Transformation_ToC_Generator, Transformation_Unzip, Transformation_Video,
            Transformation_Words, Transformation_XML_to_PDF, Transformation_XSL),
    DocShifterDocGen(Release_Email_Release, Release_FTP_release,
            Release_Filesystem_Out, Release_Sync_Return,
            Transformation_AllToPDF, Transformation_Document_Generation, Transformation_Email,
            Transformation_Image_Overlay, Transformation_Index, Transformation_Merge, Transformation_Nop, Transformation_PDF,
            Transformation_PDF_Overlay, Transformation_PDF_Security,
            Transformation_Thumbnail,
            Transformation_Unzip,
            Transformation_XML_DATA_LOAD, Transformation_XML_to_PDF);

    private final LicensableModule[] licensableModules;

    MockLicensingModuleSet(LicensableModule... licensableModules) {
        this.licensableModules = licensableModules;
    }
}

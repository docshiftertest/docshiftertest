package com.docshifter.core.asposehelper.utils.pdf;


import com.aspose.pdf.Document;
import com.docshifter.core.asposehelper.LicenseHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfTocTest {
    @BeforeAll
    static void beforeAll() {
        LicenseHelper.getLicenseHelper();
    }

    /**
     * Gets Table of Contents from a pdf file
     */
    @Test
    void getTocFromPdfFile() {
        try (Document doc = new Document("target/test-classes/266-toxicology-written.pdf")) {
            PdfToc[] tocs = PdfToc.findAll(doc, "Contents", 0, 12, 60, 60)
                    .toArray(PdfToc[]::new);
            // TODO
            assertEquals(1, tocs.length);
            assertEquals(22, tocs[0].getElements().size());
        }
    }
}

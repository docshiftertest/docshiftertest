package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.Page;
import com.aspose.pdf.XYZExplicitDestination;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ExplicitDestinationTransformerTest {
    private ExplicitDestinationTransformer sut;
    private Document doc;
    private XYZExplicitDestination dest;

    @BeforeEach
    void beforeEach() {
        doc = new Document();
        Page page = doc.getPages().add();
        doc.getPages().add();
        dest = new XYZExplicitDestination(page, 50d, 100d, 1.5d);
        sut = new ExplicitDestinationTransformer(dest);
    }

    @AfterEach
    void afterEach() {
        doc.close();
    }

    @Test
    void changePage_samePage() {
        ExplicitDestination result = sut.changePage(1);
        assertEquals(dest, result);
    }

    @Test
    void changePage_differentPage() {
        ExplicitDestination result = sut.changePage(2);
        assertNotEquals(dest, result);
        assertEquals(2, result.getPageNumber());
    }
}

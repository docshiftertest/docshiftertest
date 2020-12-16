package com.docshifter.core.asposehelper;

import com.aspose.email.MailMessageSaveType;
import com.aspose.tasks.SaveFileFormat;
import com.aspose.words.SaveFormat;
import com.docshifter.core.asposehelper.SaveOptionsHelper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SaveOptionsHelperTest {

	@Test
	public void testCellsSaveOptions() {
		String optionsAsString;
		com.aspose.cells.SaveOptions saveOptions = new com.aspose.cells.DifSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.HtmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.ImageSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.OdsSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.OoxmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.PdfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.SpreadsheetML2003SaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.SvgSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.cells.TxtSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
	}
	
	@Test
		public void testDiagramSaveOptions() throws Exception {
		String optionsAsString;
		com.aspose.diagram.SaveOptions saveOptions = new com.aspose.diagram.DiagramSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.HTMLSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.ImageSaveOptions(com.aspose.diagram.SaveFileFormat.TIFF);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.PdfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.SVGSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.XAMLSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.diagram.XPSSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
	}	

	@Test
	public void testEmailSaveOptions() {
		String optionsAsString;
		com.aspose.email.SaveOptions saveOptions = new com.aspose.email.EmlSaveOptions(MailMessageSaveType.getEmlFormat());
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.email.HtmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		com.aspose.email.IcsSaveOptions icsSaveOptions = new com.aspose.email.IcsSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(icsSaveOptions);
		assertNotNull(optionsAsString);
		com.aspose.email.MapiContactSaveOptions mapiContactSaveOptions = new com.aspose.email.MapiContactSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(mapiContactSaveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.email.MhtSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.email.MsgSaveOptions(MailMessageSaveType.getOutlookMessageFormat());
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		com.aspose.email.VCardSaveOptions vCardSaveOptions = new com.aspose.email.VCardSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(vCardSaveOptions);
		assertNotNull(optionsAsString);
	}

	@Test
	public void testPdfSaveOptions() {
		String optionsAsString;
		com.aspose.pdf.SaveOptions saveOptions = new com.aspose.pdf.DocSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.EpubSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.ExcelSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.HtmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.LaTeXSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.MobiXmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.PdfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.PptxSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.SvgSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.UnifiedSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.XmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.pdf.XpsSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
	}

	@Test
	public void testTasksSaveOptions() {
		String optionsAsString;
		com.aspose.tasks.SaveOptions saveOptions = new com.aspose.tasks.HtmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.ImageSaveOptions(SaveFileFormat.BMP);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.ImageSaveOptions(SaveFileFormat.JPEG);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.ImageSaveOptions(SaveFileFormat.PNG);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.ImageSaveOptions(SaveFileFormat.TIFF);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		com.aspose.tasks.MPPSaveOptions mppSaveOptions = new com.aspose.tasks.MPPSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(mppSaveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.PdfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.PrimaveraSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.PrimaveraXmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.tasks.Spreadsheet2003SaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		com.aspose.tasks.SaveTemplateOptions saveTemplateOptions = new com.aspose.tasks.SaveTemplateOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveTemplateOptions);
		assertNotNull(optionsAsString);
	}

	@Test
	public void testWordsSaveOptions() {
		String optionsAsString;
		com.aspose.words.SaveOptions saveOptions = new com.aspose.words.DocSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.HtmlFixedSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.HtmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.ImageSaveOptions(SaveFormat.BMP);
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.OdtSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.OoxmlSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.PclSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.PdfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.PsSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.RtfSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.SvgSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.TxtSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.WordML2003SaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.XamlFixedSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.XamlFlowSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
		saveOptions = new com.aspose.words.XpsSaveOptions();
		optionsAsString = SaveOptionsHelper.dumpSaveOptions(saveOptions);
		assertNotNull(optionsAsString);
	}
}

package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.aspose.pdf.Document;
import com.aspose.pdf.FileSpecification;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.Matrix;
import com.aspose.pdf.PKCS1;
import com.aspose.pdf.Page;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.Signature;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentAbsorber;
import com.aspose.pdf.TextFragmentCollection;
import com.aspose.pdf.XImage;
import com.aspose.pdf.facades.PdfFileSignature;
import com.aspose.pdf.operators.ConcatenateMatrix;
import com.aspose.pdf.operators.Do;
import com.aspose.pdf.operators.GRestore;
import com.docshifter.core.asposehelper.LicenseHelper;
import com.docshifter.core.asposehelper.utils.image.ImageUtils;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipFile;

/**
 * Handles operations related to metrics.
 *
 * For now handles counting the incoming files and storing the counts.
 */
@Service
@Log4j2
public class DocumentCounterServiceImpl implements DocumentCounterService {

    @Autowired
    DocumentCounterRepository documentCounterRepository;

    public DocumentCounter saveDocumentCounter(DocumentCounterDTO dto) {
        // convert from Dto to Entity and save
        DocumentCounter entity = new DocumentCounter();
        entity.setTaskId(dto.getTaskId());
        entity.setCounts(dto.getCounts());

        return documentCounterRepository.save(entity);
    }

    //Uses the @Builder annotation to create the DTO
    public DocumentCounterDTO createDocumentCounterDto(String task, long counts) {
        return DocumentCounterDTO.builder().task_id(task).counts(counts).build();

    }

    /**
     * @param filename The source filename from the Task object
     * @return int count The number of counted files
     */
    public long countFiles(String filename) {
        long counts = 1;
        String extension = FileUtils.getExtension(filename);
        log.debug("The file extension is: {}", extension);

        if (StringUtils.isBlank(extension)) {
            log.warn("Extension is blank, failure might occur; file will otherwise be counted as 1");
        }

        //TODO: Handle other archive file formats (rar, 7z, tar, etc.)
        //counts all files in an archive
        //Yoda fix - compared string first avoids NullPointerExceptions from extension
        if ("zip".equalsIgnoreCase(extension)) { //
            try (ZipFile zf = new ZipFile(filename)) {
                counts = zf.size();
                return counts;
            } catch (IOException e) {
                log.error("Error with .zip file");
                e.printStackTrace();
            }
        }
        //counts all attachments and main body in an email
        else if ("eml".equalsIgnoreCase(extension) || "msg".equalsIgnoreCase(extension)) {
            MailMessage eml = MailMessage.load(filename);
            counts = eml.getAttachments().size() + 1; // counts is set to all attachments plus the e-mail body
            return counts;
        }

        return counts; //default case

    }

    // Creates the encrypted file containing the counts that should be sent to DocShifter

    public void exportCounts(String tempPath) {
        LicenseHelper.getLicenseHelper(); //aspose license helper
        log.info("Creating export PDF");

        //Retrieve the counts from the metrics database
        long count = 0L;
        long tasks = 0L;
        try {
            count = documentCounterRepository.selectTotalCounts();
            log.info("Total counts to date: {}", count);
            tasks = documentCounterRepository.selectSuccessfulWorkflows();
            log.info("Total tasks to date: {}", tasks);
        }
        catch (NullPointerException npe) {
            log.error("Could not retrieve counts; table might be empty");
        }

        // Instantiate Document object from preset PDF
        Document doc = new Document(DocumentCounterServiceImpl.class.getResourceAsStream("/export/Counts-report-template.pdf"));

        //Prepares the name of the export file, including timestamp
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            DateFormat dateFormatReadable = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mma z");
            Date date = new Date();
            String timestamp = dateFormat.format(date); //timestamp as a variable to be included in file name and content
            String readableTimestamp = dateFormatReadable.format(date);
            String name = tempPath + "/" + "counts_" + timestamp;

        //Long data for the logo
        long[] data = new long[2];
        data[0] = count;
        data[1] = tasks;

        //Data to fill the placeholders
        String[] values = new String[3];
        values[0] = String.valueOf(count);
        values[1] = String.valueOf(tasks);
        values[2] = readableTimestamp;

        // Locate placeholders on page
        TextFragmentAbsorber tfa = new TextFragmentAbsorber("$PH");
        doc.getPages().accept(tfa);
        TextFragmentCollection tfc = tfa.getTextFragments();

        if (tfc.size() != values.length) {
            //TODO: Should we fail the process or keep it as a warning?
            log.warn("Number of placeholders does not match number of values to be written");
        }

        // Loops through placeholders and replaces them with values
        int index = 0; // tracker for looping through the values[] array
        for (TextFragment textFragment : (Iterable<TextFragment>) tfc) {
            textFragment.setText(values[index] + "");
            textFragment.getTextState().setFont(FontRepository.findFont("Calibri"));
            textFragment.getTextState().setFontSize(10.5f);
            index++;
        }

        // First creates the logo and saves it as an image on the disk
        BufferedImage logo = null;
        try {
            logo = ImageUtils.getLogo(data);
        }
        catch (NullPointerException npe) {
            log.warn("Could not retrieve logo");
        }

        //Creates the temp dir to store the logo
        File tempDir = new File(tempPath + "/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File logoFile = new File(tempPath + "/logo.png");
        if (logo != null) {
            try {
                ImageIO.write(logo, "png", logoFile);
            }
            catch (Exception e) {
                logoFile.delete();
                log.error("Failed to create export file", e); //actually failed to create secrety logo
                e.printStackTrace();
            }
        }

        try {
            //Add logo to PDF:
            // Add logo to page resources
            Page page = doc.getPages().get_Item(1);
            if (logo != null) {
                page.getResources().getImages().add(logo);

                // Create Rectangle and Matrix objects
                Rectangle rectangle = new Rectangle(100, 700, 500, 850); //lower left and upper right corner coordinates for the logo
                Matrix matrix = new Matrix(new double[]{rectangle.getURX() - rectangle.getLLX(), 0, 0, rectangle.getURY() - rectangle.getLLY(), rectangle.getLLX(), rectangle.getLLY()});

                // Using ConcatenateMatrix (concatenate matrix) operator: defines how image must be placed
                page.getContents().add(new ConcatenateMatrix(matrix));
                XImage ximage = page.getResources().getImages().get_Item(page.getResources().getImages().size());
                // Using Do operator: this operator draws image
                page.getContents().add(new Do(ximage.getName()));
                // Using GRestore operator: this operator restores graphics state
                page.getContents().add(new GRestore());
            }
            // Retrieve created logo and add as attachment
            // to the document's attachment collection
            if (logo != null) {
                FileSpecification fileSpecification = new FileSpecification(tempPath + "/logo.png", "Logo");
                doc.getEmbeddedFiles().add(fileSpecification);
            }

            // Save document to Stream object for signing
            ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            doc.save(out);

            // Create PdfFileSignature instance
            PdfFileSignature pdfSignSingle = new PdfFileSignature();
            // Bind the source PDF by reading contents of Stream
            pdfSignSingle.bindPdf(new ByteArrayInputStream((out).toByteArray()));
            // Sign the PDF file using PKCS1 object
            Signature signature = new PKCS1(DocumentCounterServiceImpl.class.getResourceAsStream("/export/docshifter.pfx"), "Gre@tD@y4Thund3rB@y");
            signature.setShowProperties(false);
            pdfSignSingle.sign(1, true, new java.awt.Rectangle(100, 100, 150, 50), signature);
            // Set image for signature appearance TODO: Probably something other than the DS logo
            if (logo != null) {
                pdfSignSingle.setSignatureAppearance(tempPath + "/logo.png");
            }
            // Save final output
            pdfSignSingle.save(name + ".pdf");
            log.info("Export file created successfully!");
        }
        catch (Exception e) {
            log.error("Failed to create export file", e);
        }
        finally {
            logoFile.delete();
        }
    }

//    @Scheduled
    private void timedExport() {

    }
}

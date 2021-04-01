package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.aspose.pdf.Document;
import com.aspose.pdf.FileSpecification;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.Matrix;
import com.aspose.pdf.Operator;
import com.aspose.pdf.PKCS1;
import com.aspose.pdf.Page;
import com.aspose.pdf.Rectangle;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
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
public class DocumentCounterServiceImpl implements DocumentCounterService {
    private static final Logger logger = Logger.getLogger(new Object() {
    }.getClass().getEnclosingClass());

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
        logger.debug("The file extension is:" + extension);

        if (StringUtils.isBlank(extension)) {
            logger.warn("Extension is blank, failure might occur; file will otherwise be counted as 1");
        }

        //TODO: Handle other archive file formats (rar, 7z, tar, etc.)
        //counts all files in an archive
        //Yoda fix - compared string first avoids NullPointerExceptions from extension
        if ("zip".equalsIgnoreCase(extension)) { //
            try (ZipFile zf = new ZipFile(filename)) {
                counts = zf.size();
                return counts;
            } catch (IOException e) {
                logger.error("Error with .zip file");
                logger.error(e);
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

    /**
     * @param tempPath   The console.temp.dir value from the application.properties file; retrieved in the Controller
     * @param exportPath Similar to above, the export.dir value
     */
//    public void exportCounts(String tempPath, String exportPath) {
//        logger.info("Creating counts export file");
//
//        //Retrieving counts from the database
//        long counts = documentCounterRepository.selectTotalCounts();
//        logger.info("Total counts to date: " + counts);
//        long tasks = documentCounterRepository.selectSuccessfulWorkflows();
//        logger.info("Total tasks to date: " + tasks);
//
//        //Searches through the DocShifter install directory to find the public key
//        //which should be hidden under a non-indicative name
//        //Apparently overwrites the 'accept' method in FilenameFilter to find the file we want
//        File exportDir = new File(exportPath);
//        File[] match = exportDir.listFiles(new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                return name.startsWith("publicKey");
//            }
//        });
//
//        //Prepares the name of the export file, including timestamp
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        Date date = new Date();
//        String timestamp = dateFormat.format(date); //timestamp as a variable to be included in file name and content
//        String name = tempPath + "/" + "counts_" + timestamp;
//
//        // Creates the encrypted file:
//        File countsFile = new File(name);
//        try {
//            File dir = new File(tempPath);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            // Writes the string that will be encrypted
//            // limited to (key length in bits)/8 -11) bytes
//            // so for a 2048-bit RSA key, maximum size of the string is 245 bytes/characters
//            // Current size is 60-70 even with maximum values for the longs, so we're good
//            String padding = "Files: " + counts +
//                    "   \n" + "Tasks:" + tasks + "     \n"
//                    + "At: " + timestamp;
//            //TODO: When the metric system is expanded, add other metrics we are interested in
//
//            CountsEncryption encryptor = new CountsEncryption();
//            // Gets the public key from the file retrieved above
//            PublicKey publicKey = encryptor.getPublic(match[0].toString());
//
//            //Writes the encrypted string to a new file
//            encryptor.encryptFile(padding.getBytes(StandardCharsets.UTF_8),
//                    countsFile, publicKey);
//        } catch (Exception exception) {
//            countsFile.delete(); //Remove plaintext file in case of errors!
//            logger.error("Failed to encrypt file");
//            exception.printStackTrace();
//
//            // TODO: First send e-mail, then open e-mail client, then point them to encrypted file
//            // mailTo link, encrypt the body of the email; include license number? -> DSLicenseCode
//        }
//    }

    public void exportCounts(String tempPath, String exportPath) {
        LicenseHelper.getLicenseHelper();

        long counts = documentCounterRepository.selectTotalCounts();
        logger.info("Total counts to date: " + counts);
        long tasks = documentCounterRepository.selectSuccessfulWorkflows();
        logger.info("Total tasks to date: " + tasks);


        String dataDir = "E:/DocShifter/module-experiments/";
        // Instantiate Document object
        Document doc = new Document(dataDir + "Counts file test.pdf");

        //Find placeholder text
        long[] values = new long[2];
        values[0] = counts;
        values[1] = tasks;

        TextFragmentAbsorber tfa = new TextFragmentAbsorber("$PH");
        doc.getPages().accept(tfa);
        TextFragmentCollection tfc = tfa.getTextFragments();

        int index = 0;
        for (TextFragment textFragment : (Iterable<TextFragment>) tfc) {
            textFragment.setText(values[index] + "");
            textFragment.getTextState().setFont(FontRepository.findFont("Verdana"));
            textFragment.getTextState().setFontSize(10.5f);
//            textFragment.getTextState().setForegroundColor(Color.getBlue());
//            textFragment.getTextState().setBackgroundColor(Color.getGray());
            index++;
        }
        //Add logo to PDF
        //Commented code is the one that actually adds the logo to the file, which we probably want
        BufferedImage logo = ImageUtils.getLogo(values);
        Page page = doc.getPages().get_Item(1);
        page.getResources().getImages().add(logo);
        // Create Rectangle and Matrix objects
        Rectangle rectangle = new Rectangle(200, 500, 400, 650);
        Matrix matrix = new Matrix(new double[] { rectangle.getURX() - rectangle.getLLX(), 0, 0, rectangle.getURY() - rectangle.getLLY(), rectangle.getLLX(), rectangle.getLLY() });

        // Using ConcatenateMatrix (concatenate matrix) operator: defines how image must be placed
        page.getContents().add(new ConcatenateMatrix(matrix));
        XImage ximage = page.getResources().getImages().get_Item(page.getResources().getImages().size());

        // Using Do operator: this operator draws image
        page.getContents().add(new Do(ximage.getName()));

        // Using GRestore operator: this operator restores graphics state
        page.getContents().add(new GRestore());

//        // Save the new PDF
//        doc.save("Updated_document.pdf");

        try {
            File file = new File("E:/DocShifter/module-experiments/myimage.png");
            ImageIO.write(logo, "png", file);
        }
        catch (Exception e) {
            logger.error("Failed to create logo file");
            e.printStackTrace();
        }
        FileSpecification fileSpecification = new FileSpecification("E:/DocShifter/module-experiments/myimage.png", "Logo");

// Add an attachment to document's attachment collection
        doc.getEmbeddedFiles().add(fileSpecification);
        ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        // Save document to Stream object
        doc.save(out);

        // Create PdfFileSignature instance
        PdfFileSignature pdfSignSingle = new PdfFileSignature();
        // Bind the source PDF by reading contents of Stream
        pdfSignSingle.bindPdf(new ByteArrayInputStream((out).toByteArray()));
        // Sign the PDF file using PKCS1 object
        pdfSignSingle.sign(1, true, new java.awt.Rectangle(100, 100, 150, 50), new PKCS1(dataDir + "output.pfx", "changeit"));
        // Set image for signature appearance
        pdfSignSingle.setSignatureAppearance(dataDir + "myimage.png");
        // Save final output
        pdfSignSingle.save(dataDir + "out_PDFNEWJAVA_33311.pdf");



        }
}

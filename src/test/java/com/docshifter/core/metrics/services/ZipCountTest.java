package com.docshifter.core.metrics.services;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipCountTest extends AbstractSpringTest {
    @Autowired
    private DocumentCounterServiceImpl counterService;

    private byte[] bytesOfPoem;
    private byte[] bytesOfText;

    // make sure we create temp folder before anything else...
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    // zip file
    public File zipFile;

    // now after temp folder has been created deals with creation of a zip file and content within.
    @Before
    public void setUp() throws IOException {
        // some poem
        String poem = "Roses are red, violets are blue...";
        // some message
        String secretMessage = "Life is beautiful.";
        // turn to bytes in order to write them to zip content entries
        bytesOfPoem = poem.getBytes();
        bytesOfText = secretMessage.getBytes();
        // create a zip file zipFile.zip
        zipFile = folder.newFile("zipFile.zip");
        // open streams for writing to zip file
        OutputStream out = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(out);
        // create entry for poem
        ZipEntry poemEntry = new ZipEntry("/poem.docx");
        // set size for poem entry
        poemEntry.setSize(bytesOfPoem.length);
        // stream entry declaration
        zipOutputStream.putNextEntry(poemEntry);
        // and content within
        zipOutputStream.write(bytesOfPoem);
        ZipEntry messageEntry = new ZipEntry("/text.docx");
        messageEntry.setSize(bytesOfText.length);
        zipOutputStream.putNextEntry(messageEntry);
        zipOutputStream.write(bytesOfText);
        zipOutputStream.close();
    }

    @Test
    public void shouldCountAllFilesInzip(){
        String filename = zipFile.toString();
        long counts = counterService.countFiles(filename);
        DocumentCounterDTO metric = counterService.createDocumentCounterDto("sometask", counts);

        assertThat(metric.getCounts()).isEqualTo(2);
    }

    @After
    public void tearDown() {
        zipFile.delete();
        folder.delete();
    }
}

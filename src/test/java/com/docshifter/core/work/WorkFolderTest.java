package com.docshifter.core.work;

import com.docshifter.core.utils.FileUtils;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@Log4j2
class WorkFolderTest {

    private WorkFolder workfolder;

    @Before
    public void before() {
        workfolder = new WorkFolder();
        workfolder.setFolder(Paths.get("target/test-classes/ds/work"));
        workfolder.setErrorFolder(Paths.get("target/test-classes/ds/error"));
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtension() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

        workfolder = new WorkFolder();
        workfolder.setFolder(Paths.get("target/test-classes/ds/work"));
        workfolder.setErrorFolder(Paths.get("target/test-classes/ds/error"));

        // Then
        log.info("Creating the filepath");
        Path newFilePath = workfolder.getNewFilePath(filename, extension, true);
        File file = new File(newFilePath.toString());
        FileUtils.copyFile(testPdf, file);

        File folder = new File(workfolder.toString());
        File[] filesInWorkfolder = folder.listFiles();
        int numberOfFiles = filesInWorkfolder.length - 1;

        Assertions.assertTrue(file.exists(), "The file exists");
        Assertions.assertEquals(numberOfFiles, 1, "The number of files in the folder should be one");

        log.info("Deleting file");
        deleteFilesInFolder(folder);
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtensionForTheSameFilename() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

        workfolder = new WorkFolder();
        workfolder.setFolder(Paths.get("target/test-classes/ds/work"));
        workfolder.setErrorFolder(Paths.get("target/test-classes/ds/error"));

        // Then
        log.info("Creating the filepath for the first file");
        Path newFilePath = workfolder.getNewFilePath(filename, extension, true);
        File file = new File(newFilePath.toString());
        FileUtils.copyFile(testPdf, file);

        log.info("Creating the filepath for the second file with the same name");
        Path newFilePathInsideFolder = workfolder.getNewFilePath(filename, extension, true);
        File fileInsideFolder = new File(newFilePathInsideFolder.toString());
        FileUtils.copyFile(testPdf, fileInsideFolder);

        File folder = new File(workfolder.toString());
        File[] filesInWorkfolder = folder.listFiles();
        int numberOfFiles = filesInWorkfolder.length - 1;

        Assertions.assertTrue(file.exists(), "The file exists");
        Assertions.assertTrue(fileInsideFolder.exists(), "The fileInsideFolder exists");
        Assertions.assertEquals(numberOfFiles, 2, "The number of files in the folder should be two");

        log.info("Deleting files");
        deleteFilesInFolder(folder);
    }

    private void deleteFilesInFolder(File folder) {

        for (File fileInside : folder.listFiles()) {
            if (fileInside.isDirectory()) {
                deleteFilesInFolder(fileInside);
            }

            if (!fileInside.getName().equalsIgnoreCase(".empty")) {
                fileInside.delete();
            }
        }
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtensionForTheThreeFilesWithTheSameFilename() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

        workfolder = new WorkFolder();
        workfolder.setFolder(Paths.get("target/test-classes/ds/work"));
        workfolder.setErrorFolder(Paths.get("target/test-classes/ds/error"));

        // Then
        log.info("Creating the filepath for the first file");
        Path newFilePath = workfolder.getNewFilePath(filename, extension, true);
        File file = new File(newFilePath.toString());
        FileUtils.copyFile(testPdf, file);

        log.info("Creating the filepath for the second file with the same name");
        Path newFilePathInsideFolder = workfolder.getNewFilePath(filename, extension, true);
        File fileInsideFolder = new File(newFilePathInsideFolder.toString());
        FileUtils.copyFile(testPdf, fileInsideFolder);

        log.info("Creating the filepath for the third file with the same name");
        Path newFilePathThirdFile = workfolder.getNewFilePath(filename, extension, true);
        File thirdFile = new File(newFilePathThirdFile.toString());
        FileUtils.copyFile(testPdf, thirdFile);

        File folder = new File(workfolder.toString());
        File[] filesInWorkfolder = folder.listFiles();
        int numberOfFiles = filesInWorkfolder.length - 1;

        Assertions.assertTrue(file.exists(), "The file exists");
        Assertions.assertTrue(fileInsideFolder.exists(), "The fileInsideFolder exists");
        Assertions.assertTrue(thirdFile.exists(), "The thirdFile exists");
        Assertions.assertEquals(numberOfFiles, 3, "The number of files in the folder should be three");

        log.info("Deleting files");
        deleteFilesInFolder(folder);
    }

}
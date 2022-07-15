package com.docshifter.core.work;

import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class WorkFolderTest {

    private WorkFolder workfolder;

    @BeforeEach
    public void before() {
        workfolder = new WorkFolder();
        String subFolder = UUID.randomUUID().toString();
        Path workPath = Paths.get("target/test-classes", subFolder, "work");
        Path errorPath = Paths.get("target/test-classes", subFolder, "error");
        File folder = new File(workPath.toString());
        folder.mkdirs();
        folder = new File(errorPath.toString());
        folder.mkdirs();
        workfolder.setFolder(workPath);
        workfolder.setErrorFolder(errorPath);
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtension() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

        // Then
        Path newFilePath = workfolder.getNewFilePath(filename, extension, true);
        log.info("Created the filepath: {}", newFilePath);
        File file = new File(newFilePath.toString());
        FileUtils.copyFile(testPdf, file);

        File folder = new File(workfolder.toString());
        File[] filesInWorkFolder = folder.listFiles();
        assert filesInWorkFolder != null;
        List<String> files = Arrays.asList(filesInWorkFolder)
                .stream().map(x->x.getName())
                .collect(Collectors.toList());
        log.debug("testGetNewFilePathWithFilenameAndExtension() - " +
                "Files in Work Folder: {}", String.join(", ", files));

        Assertions.assertTrue(file.exists(), "The file exists");
        if (files.contains(".empty")) {
            assertEquals(2, files.size(), "The number of files in the folder should be two (including .empty");
        }
        else {
            assertEquals(1, files.size(), "The number of files in the folder should be one");
        }

        log.info("Deleting files in folder: {}", folder);
        deleteFilesInFolder(folder);
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtensionForTheSameFilename() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

        // Then
        log.info("Creating the filepath for the first file");
        Path newFilePath = workfolder.getNewFilePath(filename, extension, true);
        log.debug("New file path is: {}", newFilePath);
        File file = new File(newFilePath.toString());
        FileUtils.copyFile(testPdf, file);

        log.info("Creating the filepath for the second file with the same name");
        Path newFilePathInsideFolder = workfolder.getNewFilePath(filename, extension, true);
        log.debug("New file path inside folder is: {}", newFilePathInsideFolder);
        File fileInsideFolder = new File(newFilePathInsideFolder.toString());
        FileUtils.copyFile(testPdf, fileInsideFolder);

        File folder = new File(workfolder.toString());
        File[] filesInWorkFolder = folder.listFiles();
        assert filesInWorkFolder != null;
        List<String> files = Arrays.asList(filesInWorkFolder)
                .stream().map(x->x.getName())
                .collect(Collectors.toList());
        log.debug("testGetNewFilePathWithFilenameAndExtensionForTheSameFilename() - " +
                "Files in Work Folder: {}", String.join(", ", files));
        log.debug("Number of files: {} in folder: {}", files.size(), folder);

        Assertions.assertTrue(file.exists(), "The file exists");
        Assertions.assertTrue(fileInsideFolder.exists(), "The fileInsideFolder exists");
        if (files.contains(".empty")) {
            assertEquals(3, files.size(), "The number of files in the folder should be three (including .empty");
        }
        else {
            assertEquals(2, files.size(), "The number of files in the folder should be two");
        }

        log.info("Deleting files in folder: {}", folder);
        deleteFilesInFolder(folder);
    }

    private void deleteFilesInFolder(File folder) {

        log.debug("Deleting files in folder: {}", folder);

        for (File fileInside : Objects.requireNonNull(folder.listFiles())) {
            if (fileInside.isDirectory()) {
                deleteFilesInFolder(fileInside);
            }

            if (!fileInside.getName().equalsIgnoreCase(".empty")) {
                if (!fileInside.delete()) {
                    log.warn("Could not delete fileInside: {}", fileInside);
                }
            }
        }
    }

    @Test
    public void testGetNewFilePathWithFilenameAndExtensionForTheThreeFilesWithTheSameFilename() throws IOException {

        // Given
        File testPdf = new File("target/test-classes/test.pdf");
        String filename = FileUtils.getNameWithoutExtension(testPdf.getName());
        String extension = FileUtils.getExtension(testPdf.getName());

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
        File[] filesInWorkFolder = folder.listFiles();
        assert filesInWorkFolder != null;
        List<String> files = Arrays.asList(filesInWorkFolder)
                .stream().map(x->x.getName())
                .collect(Collectors.toList());
        log.debug("testGetNewFilePathWithFilenameAndExtensionForTheThreeFilesWithTheSameFilename() - " +
                "Files in Work Folder: {}", String.join(", ", files));

        Assertions.assertTrue(file.exists(), "The file exists");
        Assertions.assertTrue(fileInsideFolder.exists(), "The fileInsideFolder exists");
        Assertions.assertTrue(thirdFile.exists(), "The thirdFile exists");
        if (files.contains(".empty")) {
            assertEquals(4, files.size(), "The number of files in the folder should be four (including .empty");
        }
        else {
            assertEquals(3, files.size(), "The number of files in the folder should be three");
        }

        log.info("Deleting files from folder: {}", folder);
        deleteFilesInFolder(folder);
    }

}
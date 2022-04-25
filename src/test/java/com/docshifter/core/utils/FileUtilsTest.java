package com.docshifter.core.utils;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class FileUtilsTest {

    @Test
    public void testCreateJsonFile() {
        String path = "target/test-classes/";

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("key", "value");

        assertTrue(FileUtils.writeJsonFile(jsonMap, path  + "/test-file.json"));

        File newFile = new File(path + "/test-file.json");

        assertTrue(newFile.exists());
    }
}

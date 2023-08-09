package com.docshifter.core.config.jobs;

import com.docshifter.core.config.InstallationType;
import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
@Log4j2
public class CleanDumpFilesScheduleJob {

    private final ScheduledExecutorService scheduler;

    @Value("${docshifter.component.home}")
    private String docShifterComponentPath;

    @Value("${jvm_logs_dir}")
    private String jvmLogsDir;

    private final InstallationType installationType;

    public CleanDumpFilesScheduleJob(ScheduledExecutorService scheduler, InstallationType installationType) {
        this.installationType = installationType;
        this.scheduler = scheduler;
    }

    @Scheduled(cron = "${docshifter.cleanup.dump.schedule:-}")
    public void cleanDumpFiles() {
        if(installationType.isContainerized()){
            clean(Path.of(jvmLogsDir));
        } else {
            clean(Path.of(docShifterComponentPath));
        }
    }

    /**
     * Cleans the files inside a specific path
     * @param path the path that contains the file
     */
    private void clean(Path path) {
        log.info("Starting scheduled cleanup of files in path {}", path);

        // Gets all the files and apply the filters
        List<Path> files = new ArrayList<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path walkingFilePath, BasicFileAttributes attrs) {
                    if (!attrs.isDirectory() && FileUtils.getExtension(walkingFilePath).equalsIgnoreCase("dmp") || FileUtils.getExtension(walkingFilePath).equalsIgnoreCase("trc")) {
                        files.add(walkingFilePath);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Wasn't possible to walk in the file tree {}", path);
        }

        for(Path filePath: files) {
            FileUtils.deletePath(scheduler, filePath, true);
        }
    }
}

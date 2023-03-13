package com.docshifter.core.config.jobs;

import com.docshifter.core.config.Constants;
import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
@Log4j2
public class CleanDumpFilesScheduleJob {

    private static final Clock SYSTEM_CLOCK = Clock.systemDefaultZone();

    @Value("${docshifter.receiver.cleanup.dump.10080:1440}")
    private int dumpFilesLifeTimeMinutes;

    private final ScheduledExecutorService scheduler;

    @Value("${docshifter.home}")
    private String dsPath;

    public CleanDumpFilesScheduleJob(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Scheduled(cron = "${docshifter.receiver.cleanup.dump.schedule:-}")
    public void cleanDumpFiles() {

        for (String componentFolder : Constants.DUMP_FILES_FOLDER_LIST) {
            clean(Path.of(dsPath + "/" + componentFolder), dumpFilesLifeTimeMinutes);
        }

    }

    /**
     * Cleans the files inside a specific path
     * @param path the path that contains the file
     * @param lifetimeMinutes The age of the files
     */
    private void clean(Path path, int lifetimeMinutes) {
        log.info("Starting scheduled cleanup of files older than {} minutes in path {}", lifetimeMinutes, path);

        // Gets all the files and apply the filters
        File[] files = FileUtil.listFiles(new File(path.toString()), pathname -> {
            // The file can't be a directory
            if(pathname.isDirectory()){
                return false;
            }

            // the file needs to be dmp or trc
            if(!FileUtils.getExtension(pathname.getPath()).equalsIgnoreCase("dmp")){
                return false;
            }

            try {
                Instant lastModified = Files.getLastModifiedTime(Path.of(pathname.getPath())).toInstant();
                if (lastModified.equals(Instant.EPOCH)) {
                    log.warn("{} cannot be checked for its lifetime! Please delete it manually when " +
                            "necessary.", pathname.getPath());
                    return false;
                }
                return lastModified.plus(lifetimeMinutes, ChronoUnit.MINUTES).isBefore(SYSTEM_CLOCK.instant());
            } catch (IOException ioe) {
                log.warn("{} cannot be checked for its lifetime! Please delete it manually when " +
                        "necessary.", pathname.getPath(), ioe);
                return false;
            }
        });

        for(File file: files) {
            FileUtils.deleteFile(scheduler, Path.of(file.getPath()), 3);
        }
    }
}

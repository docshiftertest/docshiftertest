package com.docshifter.core.config.jobs;

import com.docshifter.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
@Log4j2
public class CleanDumpFilesScheduleJob {

    private final ScheduledExecutorService scheduler;

    @Value("${docshifter.component.home}")
    private String docShifterComponentPath;

    public CleanDumpFilesScheduleJob(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Scheduled(cron = "${docshifter.cleanup.dump.schedule:-}")
    public void cleanDumpFiles() {
        clean(Path.of(docShifterComponentPath));
    }

    /**
     * Cleans the files inside a specific path
     * @param path the path that contains the file
     */
    private void clean(Path path) {
        log.info("Starting scheduled cleanup of files in path {}", path);

        // Gets all the files and apply the filters
        File[] files = FileUtil.listFiles(new File(path.toString()), pathname -> {
            // The file can't be a directory
            if(pathname.isDirectory()){
                return false;
            }

            // the file needs to be dmp or trc
            return FileUtils.getExtension(pathname.getPath()).equalsIgnoreCase("dmp") || FileUtils.getExtension(pathname.getPath()).equalsIgnoreCase("trc");
        });

        for(File file: files) {
            FileUtils.deleteFile(scheduler, Path.of(file.getPath()), 3);
        }
    }
}

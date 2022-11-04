package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.FileSizeDistributionSample;
import com.docshifter.core.metrics.samples.FileTypeDistributionSample;
import com.docshifter.core.metrics.entities.DashboardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Created by Juan Marques on 23/08/2021
 */
public interface DashboardFileRepository extends JpaRepository<DashboardFile,Long> {
    @Query(value = """
SELECT CASE WHEN processed.file_name = processed.ext THEN NULL ELSE processed.ext END as extension, count(*)
FROM (SELECT dbf.file_name,
             substring(dbf.file_name, '[^.]*$') as ext
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))) processed
GROUP BY extension""", nativeQuery = true)
    List<FileTypeDistributionSample> findAllFileTypeDistribution(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT CASE WHEN processed.file_name = processed.ext THEN NULL ELSE processed.ext END as extension, count(*)
FROM (SELECT dbf.file_name,
             substring(dbf.file_name, '[^.]*$') as ext
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) processed
GROUP BY extension""", nativeQuery = true)
    List<FileTypeDistributionSample> findAllFileTypeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") Long startDate,
                                                                 @Param("endDate") Long endDate);

    @Query(value = """
SELECT t.range, count(*)
FROM (SELECT dbf.file_name, CASE WHEN dbf.file_size < 100 THEN 0
                             WHEN dbf.file_size < 250 THEN 1
                             WHEN dbf.file_size < 500 THEN 2
                             WHEN dbf.file_size < 1000 THEN 3
                             WHEN dbf.file_size < 5000 THEN 4
                             WHEN dbf.file_size < 10000 THEN 5
                             WHEN dbf.file_size < 25000 THEN 6
                             ELSE 7 END as range
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))) t
GROUP BY t.range""", nativeQuery = true)
    List<FileSizeDistributionSample> findAllFileSizeDistribution(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT t.range, count(*)
FROM (SELECT dbf.file_name, CASE WHEN dbf.file_size < 100 THEN 0
                             WHEN dbf.file_size < 250 THEN 1
                             WHEN dbf.file_size < 500 THEN 2
                             WHEN dbf.file_size < 1000 THEN 3
                             WHEN dbf.file_size < 5000 THEN 4
                             WHEN dbf.file_size < 10000 THEN 5
                             WHEN dbf.file_size < 25000 THEN 6
                             ELSE 7 END as range
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) t
GROUP BY t.range""", nativeQuery = true)
    List<FileSizeDistributionSample> findAllFileSizeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") Long startDate,
                                                                 @Param("endDate") Long endDate);
}

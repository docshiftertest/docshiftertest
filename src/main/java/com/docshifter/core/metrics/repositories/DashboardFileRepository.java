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
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) processed
GROUP BY extension""", nativeQuery = true) // Needs to be native because subqueries can't be used in HQL FROM clauses
                                           // (yet)
    List<FileTypeDistributionSample> findAllFileTypeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") long startDate,
                                                                 @Param("endDate") long endDate);

    @Query(value = """
SELECT t.range, count(*)
FROM (SELECT dbf.file_name, CASE WHEN dbf.file_size < 100 THEN 'Smaller than 0.1 MB'
                             WHEN dbf.file_size < 250 THEN '0.1-0.25 MB'
                             WHEN dbf.file_size < 500 THEN '0.25-0.5 MB'
                             WHEN dbf.file_size < 1000 THEN '0.5-1 MB'
                             WHEN dbf.file_size < 5000 THEN '1-5 MB'
                             WHEN dbf.file_size < 10000 THEN '5-10 MB'
                             WHEN dbf.file_size < 25000 THEN '10-25 MB'
                             ELSE '25+ MB' END as range
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) t
GROUP BY t.range""", nativeQuery = true) // Needs to be native because subqueries can't be used in HQL FROM clauses
                                         // (yet)
    List<FileSizeDistributionSample> findAllFileSizeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") long startDate,
                                                                 @Param("endDate") long endDate);
}

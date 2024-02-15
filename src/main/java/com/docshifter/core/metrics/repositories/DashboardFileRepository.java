package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.FilePageCountDistributionSample;
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
             AND (dbf.page_count > 0 OR dbf.page_count IS NULL)
             AND dbf.input = :isInput
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) processed
GROUP BY extension
ORDER BY extension""", nativeQuery = true) // Needs to be native because subqueries can't be used in HQL FROM clauses
                                           // (yet)
    List<FileTypeDistributionSample> findAllFileTypeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") long startDate,
                                                                 @Param("endDate") long endDate,
                                                                 @Param("isInput") boolean isInput);

    @Query(value = """
SELECT t.range[2], count(*)
FROM (SELECT dbf.file_name, CASE WHEN dbf.file_size < 100 THEN array['0', 'Smaller than 0.1 MB']
                             WHEN dbf.file_size < 250 THEN array['1', '0.1-0.25 MB']
                             WHEN dbf.file_size < 500 THEN array['2', '0.25-0.5 MB']
                             WHEN dbf.file_size < 1000 THEN array['3', '0.5-1 MB']
                             WHEN dbf.file_size < 5000 THEN array['4', '1-5 MB']
                             WHEN dbf.file_size < 10000 THEN array['5', '5-10 MB']
                             WHEN dbf.file_size < 25000 THEN array['6', '10-25 MB']
                             ELSE array['7', '25+ MB'] END as range
      FROM metrics.dashboard_file dbf
      INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
             AND (dbf.page_count > 0 OR dbf.page_count IS NULL)
             AND dbf.input = :isInput
             AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
             AND db.on_message_hit BETWEEN :startDate AND :endDate) t
GROUP BY t.range
ORDER BY t.range""", nativeQuery = true) // Needs to be native because subqueries can't be used in HQL FROM clauses
                                         // (yet)
                                         // t.range is an array so the first element can be used to maintain a correct
                                         // ordering, second element is the actual label to be displayed for the
                                         // group (hence SELECT t.range[2], and PostgreSQL uses one-based indexing for
                                         // arrays)
    List<FileSizeDistributionSample> findAllFileSizeDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                 @Param("startDate") long startDate,
                                                                 @Param("endDate") long endDate,
                                                                 @Param("isInput") boolean isInput);

    @Query(value = """
SELECT
    CASE WHEN processed.file_name = processed.ext THEN NULL
        ELSE processed.ext
        END as extension, 
    sum(processed.page_count) AS count
FROM (SELECT dbf.file_name,
             substring(dbf.file_name, '[^.]*$') as ext,
             dbf.page_count as page_count
      FROM metrics.dashboard_file dbf
          INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
      WHERE db.is_licensed = true
        AND (dbf.page_count > 0)
        AND dbf.input = :isInput
        AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
        AND db.on_message_hit BETWEEN :startDate AND :endDate) processed
GROUP BY extension
ORDER BY count DESC""", nativeQuery = true)
    List<FilePageCountDistributionSample> findAllFilePageCountDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                           @Param("startDate") long startDate,
                                                                           @Param("endDate") long endDate,
                                                                           @Param("isInput") boolean isInput);

    @Query(value = """
WITH filtered_dash AS (
    -- Filtering licensed, 
    -- the input file or the output with page count > 0
    -- by dates and workflow name
    SELECT
        dbf.task_id,
        substring(dbf.file_name, '[^.]*$') AS ext,
        dbf.page_count as page_count,
        dbf.input,
        CASE WHEN dbf.parent_dashboard_file IS NULL THEN true
            ELSE false
            END AS parent
    FROM metrics.dashboard_file dbf
    INNER JOIN metrics.dashboard db ON dbf.task_id = db.task_id
    WHERE db.is_licensed = true
    AND (dbf.input = true OR dbf.page_count > 0)
    AND (db.workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
    AND db.on_message_hit BETWEEN :startDate AND :endDate
), processed AS (
    SELECT
        input_files_parent.task_id,
        input_files_parent.ext AS input_extension,
        output_files.ext AS output_extension,
        CASE WHEN input_files_parent.ext IN ('CONTAINER', 'zip', 'eml', 'msg') THEN input_files_child.extensions
            END AS input_extensions,
        output_files.page_count AS page_count
    FROM (
        -- Getting only the parent files and for folders using the extension as CONTAINER
        SELECT
                CASE WHEN filtered_dash.ext LIKE '%\\\\%' THEN 'CONTAINER'
                     WHEN filtered_dash.ext LIKE '%/%' THEN 'CONTAINER'
                     ELSE filtered_dash.ext
                    END AS ext,
                filtered_dash.task_id
            FROM filtered_dash
            WHERE input = true
            AND parent = true
        ) input_files_parent
    LEFT JOIN (
        -- Getting the child files
        -- Using left join as a parent file may not have child files 
        SELECT
            filtered_dash_distinct.task_id,
            string_agg(filtered_dash_distinct.ext, ', ') AS extensions
        FROM (
            -- Getting the distinct to show the input extensions only once
            SELECT DISTINCT
                task_id,
                ext
            FROM filtered_dash
            WHERE input = true
            AND parent = false
        ) filtered_dash_distinct
        GROUP BY task_id
    ) input_files_child
    ON input_files_parent.task_id = input_files_child.task_id
    INNER JOIN (
        -- Linking the output files
        SELECT *
        FROM filtered_dash
        WHERE input = false
    ) output_files
    ON input_files_parent.task_id = output_files.task_id
)
SELECT
    processed.input_extension || ' -> ' || processed.output_extension AS extension,
    processed.input_extensions AS inputExtensions,
    sum(processed.page_count) AS count
FROM processed
GROUP BY extension, input_extensions
ORDER BY count DESC""", nativeQuery = true)
    List<FilePageCountDistributionSample> findAllFilePageCountDistribution(@Param("workflowNameList") Set<String> workflowNameList,
                                                                           @Param("startDate") long startDate,
                                                                           @Param("endDate") long endDate);

}

package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.Sample.DocumentFontsSample;
import com.docshifter.core.metrics.Sample.ErrorLogDistributionSample;
import com.docshifter.core.metrics.Sample.ProcessedTasksSample;
import com.docshifter.core.metrics.Sample.TasksDistributionSample;
import com.docshifter.core.metrics.Sample.TasksStatisticsSample;
import com.docshifter.core.metrics.entities.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Julian Isaac on 02.08.2021
 */
public interface DashboardRepository extends JpaRepository<Dashboard, String> {

    @Query("select dash.finishTimestamp as finishTimestamp from Dashboard dash where dash.isLicensed = TRUE AND dash.onMessageHit BETWEEN :startDate AND :endDate AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksDistributionSample> findAllFinishTimestamp(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("workflowNameList") List<String> workflowNameList);

    @Query(value = "select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND dash.onMessageHit BETWEEN :startDate AND :endDate AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksStatisticsSample> findAllProcessingDurationBetweenDates(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("workflowNameList") List<String> workflowNameList);

    @Query("select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksStatisticsSample> findAllProcessingDuration(@Param("workflowNameList") List<String> workflowNameList);

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success, dash.workflowName from Dashboard dash where dash.isLicensed = TRUE AND (:overall = true OR (dash.onMessageHit BETWEEN :startDate AND :endDate)) AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<ProcessedTasksSample> findOnMessageHitAndSuccess(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("overall") boolean overall, @Param("workflowName") String workflowName);

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success, dash.workflowName from Dashboard dash where dash.isLicensed = TRUE AND (:overall = true OR (dash.onMessageHit BETWEEN :startDate AND :endDate)) AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<ProcessedTasksSample> findAllOnMessageHitAndSuccess(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("overall") boolean overall, @Param("workflowNameList") List<String> workflowNameList);

    @Query("select distinct dash.workflowName as workflowName from Dashboard dash where dash.isLicensed = TRUE")
    List<String> findAllDistinctDashboardWorkflowName();

    @Query(value = "select (REGEXP_REPLACE(dsf.file_name, '^.+([/\\\\])', '')) AS fileName, " +
            "ds.task_id            AS taskId, " +
            "ds.receiver_host_name AS receiverHostName, " +
            "ds.sender_host_name AS senderHostName, " +
            "ds.workflow_name      AS workflowName, " +
            "to_char(to_timestamp(ds.on_message_hit/1000), 'dd-MM-yyyy HH:mm') AS processDate, " +
            "ds.on_message_hit AS processDateEpoch, " +
            "coalesce(trim(substring(dtm.task_message, position(':' in dtm.task_message) + 1)), 'Unexpected error, please check the logs') AS taskMessage " +
            "from (select ds.task_id, " +
            "ds.receiver_host_name, " +
            "ds.sender_host_name, " +
            "ds.workflow_name, " +
            "ds.on_message_hit AS on_message_hit " +
            "from metrics.dashboard ds " +
            " where  ds.success = :success AND (ds.on_message_hit between :startDate and :endDate)) ds " +
            "left join metrics.dashboard_file dsf on ds.task_id = dsf.task_id " +
            "inner join metrics.dashboard_task_message dtm on ds.task_id = dtm.task_id", nativeQuery = true)
    List<ErrorLogDistributionSample> findAllBySuccess(@Param("success") Boolean success, @Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = "select coalesce(font_name, alt_font_name) AS fontName , " +
            "count(coalesce(font_name, alt_font_name)) AS fontCount " +
            "from metrics.document_fonts df " +
            "inner join lateral ( " +
            "select ds.task_id " +
            "from metrics.dashboard ds " +
            "where 1 = 1 " +
            "AND ds.task_id = df.task_id " +
            "AND ds.is_licensed = TRUE " +
            "AND (:overall = true OR (ds.on_message_hit BETWEEN :startDate AND :endDate)) " +
            "AND (ds.workflow_name in (:workflowNameList) or 'ALL' in (:workflowNameList)) " +
            "AND 1 = 1) ds on 1 = 1 " +
            "group by coalesce(font_name, alt_font_name) " +
            "order by coalesce(font_name, alt_font_name) ", nativeQuery = true)
    List<DocumentFontsSample> findAllDocumentFonts(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("overall") boolean overall, @Param("workflowNameList") List<String> workflowNameList);

}

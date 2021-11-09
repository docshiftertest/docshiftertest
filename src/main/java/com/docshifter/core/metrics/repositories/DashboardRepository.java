package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.Sample.TasksDistributionSample;
import com.docshifter.core.metrics.Sample.ProcessedTasksSample;
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

    @Query("select dash.finishTimestamp as finishTimestamp from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<TasksDistributionSample> findAllFinishTimestamp(@Param("workflowName") String workflowName);

    @Query(value = "select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND dash.onMessageHit BETWEEN :startDate AND :endDate AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<TasksStatisticsSample> findAllProcessingDurationBetweenDates(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("workflowName") String workflowName);

    @Query("select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<TasksStatisticsSample> findAllProcessingDuration(@Param("workflowName") String workflowName);

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success, dash.workflowName from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<ProcessedTasksSample> findAllOnMessageHitAndSuccess(@Param("workflowName") String workflowName);

    List<Dashboard> findAllBySuccess(Boolean success);

}

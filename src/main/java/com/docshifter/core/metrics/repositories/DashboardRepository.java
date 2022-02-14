package com.docshifter.core.metrics.repositories;

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

    @Query("select dash.finishTimestamp as finishTimestamp from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksDistributionSample> findAllFinishTimestamp(@Param("workflowNameList") List<String> workflowNameList);

    @Query(value = "select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND dash.onMessageHit BETWEEN :startDate AND :endDate AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksStatisticsSample> findAllProcessingDurationBetweenDates(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("workflowNameList") List<String> workflowNameList);

    @Query("select dash.processingDuration as processingDuration from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<TasksStatisticsSample> findAllProcessingDuration(@Param("workflowNameList") List<String> workflowNameList);

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success, dash.workflowName from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName = :workflowName or :workflowName = 'ALL')")
    List<ProcessedTasksSample> findOnMessageHitAndSuccess(@Param("workflowName") String workflowName);

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success, dash.workflowName from Dashboard dash where dash.isLicensed = TRUE AND (dash.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<ProcessedTasksSample> findAllOnMessageHitAndSuccess(@Param("workflowNameList") List<String> workflowNameList);

    @Query("select distinct dash.workflowName as workflowName from Dashboard dash where dash.isLicensed = TRUE")
    List<String> findAllDistinctDashboardWorkflowName();

    @Query("select dash from Dashboard dash where dash.success = :success AND dash.onMessageHit BETWEEN :startDate AND :endDate")
    List<Dashboard> findAllBySuccess(@Param("success") Boolean success, @Param("startDate") Long startDate, @Param("endDate") Long endDate);

}

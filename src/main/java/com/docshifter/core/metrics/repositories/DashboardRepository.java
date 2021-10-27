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

    @Query("select dash.finishTimestamp as finishTimestamp from Dashboard dash where isLicensed = 1")
    List<TasksDistributionSample> findAllFinishTimestamp();

    @Query(value = "select dash.processingDuration as processingDuration from Dashboard dash where dash.onMessageHit BETWEEN :startDate AND :endDate AND isLicensed = 1")
    List<TasksStatisticsSample> findAllProcessingDurationBetweenDates(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query("select dash.processingDuration as processingDuration from Dashboard dash where isLicensed = 1")
    List<TasksStatisticsSample> findAllProcessingDuration();

    @Query("select dash.onMessageHit as onMessageHit , dash.success as success from Dashboard dash where isLicensed = 1")
    List<ProcessedTasksSample> findAllOnMessageHitAndSuccess();

    List<Dashboard> findAllBySuccess(Boolean success);

}

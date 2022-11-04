package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.ProcessedTasksSample;
import com.docshifter.core.metrics.samples.TasksDistributionSample;
import com.docshifter.core.metrics.samples.TasksStatisticsSample;
import com.docshifter.core.metrics.entities.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

/**
 * Created by Julian Isaac on 02.08.2021
 */
public interface DashboardRepository extends JpaRepository<Dashboard, String> {
    @Query("""
SELECT dash.success, count(dash)
FROM Dashboard dash
WHERE dash.isLicensed = true
       AND (dash.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
GROUP BY dash.success""")
    List<ProcessedTasksSample> getProcessedTasksSamples(@Param("workflowNameList") Set<String> workflowNameList);

    @Query("""
SELECT dash.success, count(dash)
FROM Dashboard dash
WHERE dash.isLicensed = true
       AND (dash.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND dash.onMessageHit BETWEEN :startDate AND :endDate
GROUP BY dash.success""")
    List<ProcessedTasksSample> getProcessedTasksSamples(@Param("workflowNameList") Set<String> workflowNameList,
                                                        @Param("startDate") Long startDate,
                                                        @Param("endDate") Long endDate);

    @Query(value = """
SELECT count(*),
       sum(processing_duration) as sum,
       min(processing_duration) as minimum,
       max(processing_duration) as maximum,
       percentile_cont(0.01) within group (order by processing_duration) as firstPercentile,
       percentile_cont(0.05) within group (order by processing_duration) as fifthPercentile,
       percentile_cont(0.25) within group (order by processing_duration) as firstQuartile,
       percentile_cont(0.5) within group (order by processing_duration) as median,
       percentile_cont(0.75) within group (order by processing_duration) as thirdQuartile,
       percentile_cont(0.95) within group (order by processing_duration) as ninetyFifthPercentile,
       percentile_cont(0.99) within group (order by processing_duration) as ninetyNinthPercentile,
       stddev_pop(processing_duration) as standardDeviation,
       var_pop(processing_duration) as variance,
       avg(processing_duration) as average
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
FROM metrics.dashboard""", nativeQuery = true)
    TasksStatisticsSample getProcessingSummaryStatistics(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT count(*),
       sum(processing_duration) as sum,
       min(processing_duration) as minimum,
       max(processing_duration) as maximum,
       percentile_cont(0.01) within group (order by processing_duration) as firstPercentile,
       percentile_cont(0.05) within group (order by processing_duration) as fifthPercentile,
       percentile_cont(0.25) within group (order by processing_duration) as firstQuartile,
       percentile_cont(0.5) within group (order by processing_duration) as median,
       percentile_cont(0.75) within group (order by processing_duration) as thirdQuartile,
       percentile_cont(0.95) within group (order by processing_duration) as ninetyFifthPercentile,
       percentile_cont(0.99) within group (order by processing_duration) as ninetyNinthPercentile,
       stddev_pop(processing_duration) as standardDeviation,
       var_pop(processing_duration) as variance,
       avg(processing_duration) as average
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
FROM metrics.dashboard""", nativeQuery = true)
    TasksStatisticsSample getProcessingSummaryStatistics(@Param("workflowNameList") Set<String> workflowNameList,
                                                         @Param("startDate") Long startDate,
                                                         @Param("endDate") Long endDate);

    @Query("""
SELECT dash.workflowName, dash.success, count(dash)
FROM Dashboard dash
WHERE dash.isLicensed = true
       AND (dash.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
GROUP BY dash.workflowName, dash.success""")
    List<ProcessedTasksSample> getWorkflowTasksSamples(@Param("workflowNameList") Set<String> workflowNameList);

    @Query("""
SELECT dash.workflowName, dash.success, count(dash)
FROM Dashboard dash
WHERE dash.isLicensed = true
       AND (dash.workflowName in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND dash.onMessageHit BETWEEN :startDate AND :endDate
GROUP BY dash.workflowName, dash.success""")
    List<ProcessedTasksSample> getWorkflowTasksSamples(@Param("workflowNameList") Set<String> workflowNameList,
                                                        @Param("startDate") Long startDate,
                                                        @Param("endDate") Long endDate);

    @Query(value = """
SELECT extract(:dateUnit FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
GROUP BY dateValue""", nativeQuery = true)
    List<TasksDistributionSample> getTasksDistributionSamples(@Param("workflowNameList") Set<String> workflowNameList,
                                                              @Param("dateUnit") String dateUnit);

    @Query(value = """
SELECT extract(:dateUnit FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
GROUP BY dateValue""", nativeQuery = true)
    List<TasksDistributionSample> getTasksDistributionSamples(@Param("workflowNameList") Set<String> workflowNameList,
                                                              @Param("dateUnit") String dateUnit,
                                                              @Param("startDate") Long startDate,
                                                              @Param("endDate") Long endDate);

    @Query("""
SELECT DISTINCT dash.workflowName as workflowName
FROM Dashboard dash
WHERE dash.isLicensed = true""")
    Set<String> findAllDistinctDashboardWorkflowName();

    @Query(value = """
SELECT *
FROM metrics.getErrorLogData(:success, :startDate, :endDate)""", nativeQuery = true)
    // @QueryHints({
    //         @QueryHint(name = HINT_FETCH_SIZE, value = "500"),
    //         @QueryHint(name = HINT_CACHEABLE, value = "false"),
    //         @QueryHint(name = HINT_READONLY, value = "true"),
    //         @QueryHint()
    // })
    Stream<String> findAllLogsBySuccess(@Param("success") Boolean success,
                                        @Param("startDate") Long startDate,
                                        @Param("endDate") Long endDate);
}

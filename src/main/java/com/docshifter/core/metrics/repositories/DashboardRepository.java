package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.samples.ProcessedTasksSample;
import com.docshifter.core.metrics.samples.TasksDistributionSample;
import com.docshifter.core.metrics.samples.TasksStatisticsSample;
import com.docshifter.core.metrics.entities.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by Julian Isaac on 02.08.2021
 */
public interface DashboardRepository extends JpaRepository<Dashboard, String> {
    @Query(value = """
SELECT success, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
GROUP BY success""", nativeQuery = true) // Non-native didn't appear to work here for some reason. All fields on
                                         // each ProcessedTasksSample were null?
    List<ProcessedTasksSample> getProcessedTasksSamples(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT success, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND (on_message_hit BETWEEN :startDate AND :endDate OR success is NULL)
GROUP BY success""", nativeQuery = true) // Non-native didn't appear to work here for some reason. All fields on
                                         // each ProcessedTasksSample were null?
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
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND success is not NULL""", nativeQuery = true) // Needs to be native because many statistical functions (such
                                                       // as percentile_cont) are missing in non-native HQL.
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
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
       AND success is not NULL""", nativeQuery = true) // Needs to be native because many statistical functions (such
                                                       // as percentile_cont) are missing in non-native HQL.
    TasksStatisticsSample getProcessingSummaryStatistics(@Param("workflowNameList") Set<String> workflowNameList,
                                                         @Param("startDate") Long startDate,
                                                         @Param("endDate") Long endDate);

    @Query(value = """
SELECT workflow_name, success, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
GROUP BY workflow_name, success""", nativeQuery = true) // Non-native didn't appear to work here for some reason. All
                                                        // fields on each ProcessedTasksSample were null?
    List<ProcessedTasksSample> getWorkflowTasksSamples(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT workflow_name, success, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
GROUP BY workflow_name, success""", nativeQuery = true) // Non-native didn't appear to work here for some reason. All
                                                        // fields on each ProcessedTasksSample were null?
    List<ProcessedTasksSample> getWorkflowTasksSamples(@Param("workflowNameList") Set<String> workflowNameList,
                                                       @Param("startDate") Long startDate,
                                                       @Param("endDate") Long endDate);

    @Query(value = """
SELECT extract(HOUR FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByHour(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT extract(HOUR FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByHour(@Param("workflowNameList") Set<String> workflowNameList,
                                                              @Param("startDate") Long startDate,
                                                              @Param("endDate") Long endDate);

    @Query(value = """
SELECT extract(WEEK FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByWeek(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT extract(WEEK FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByWeek(@Param("workflowNameList") Set<String> workflowNameList,
                                                                    @Param("startDate") Long startDate,
                                                                    @Param("endDate") Long endDate);

    @Query(value = """
SELECT extract(MONTH FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByMonth(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT extract(MONTH FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByMonth(@Param("workflowNameList") Set<String> workflowNameList,
                                                                    @Param("startDate") Long startDate,
                                                                    @Param("endDate") Long endDate);

    @Query(value = """
SELECT extract(YEAR FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByYear(@Param("workflowNameList") Set<String> workflowNameList);

    @Query(value = """
SELECT extract(YEAR FROM to_timestamp(on_message_hit / 1000)) as dateValue, count(*)
FROM metrics.dashboard
WHERE is_licensed = true
       AND (workflow_name in (:workflowNameList) OR 'ALL' in (:workflowNameList))
       AND on_message_hit BETWEEN :startDate AND :endDate
       AND success is not NULL
GROUP BY dateValue
ORDER BY dateValue""", nativeQuery = true) // Needs to be native because we don't have as many time unit keywords to
                                           // choose from in a non-native HQL extract function.
    List<TasksDistributionSample> getTasksDistributionSamplesByYear(@Param("workflowNameList") Set<String> workflowNameList,
                                                                    @Param("startDate") Long startDate,
                                                                    @Param("endDate") Long endDate);

    @Query("""
SELECT DISTINCT dash.workflowName as workflowName
FROM Dashboard dash
WHERE dash.isLicensed = true""")
    Set<String> findAllDistinctDashboardWorkflowName();

    // TODO: Implement backend (instead of solely frontend) pagination?
    @Query(value = """
SELECT *
FROM metrics.getErrorLogData(:success, :startDate, :endDate)""", nativeQuery = true) // Needs to be native as we are
                                                                                     // calling into a custom PL/pgSQL
                                                                                     // function.
    List<String> findAllLogsBySuccess(@Param("success") Boolean success,
                                        @Param("startDate") Long startDate,
                                        @Param("endDate") Long endDate);
}

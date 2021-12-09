package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.Sample.FileTypeDistributionSample;
import com.docshifter.core.metrics.entities.DashboardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Created by Juan Marques on 23/08/2021
 */
public interface DashboardFileRepository extends JpaRepository<DashboardFile,Long> {

    @Query("select file.fileName as filename,file.dashboard.onMessageHit as onMessaeHit,file.fileSize as fileSize from DashboardFile file where (file.dashboard.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<FileTypeDistributionSample> findAllFileTypeDistribution(@Param("workflowNameList") List<String> workflowNameList);

    @Query("select file.fileName as filename,file.dashboard.onMessageHit as onMessaeHit,file.fileSize as fileSize from DashboardFile file where file.dashboard.onMessageHit BETWEEN :startDate AND :endDate AND (file.dashboard.workflowName in (:workflowNameList) or 'ALL' in (:workflowNameList))")
    List<FileTypeDistributionSample> findAllFileTypeDistributionBetweenDates(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("workflowNameList") List<String> workflowNameList);
}

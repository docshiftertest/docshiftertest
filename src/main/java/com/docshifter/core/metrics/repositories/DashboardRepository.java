package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.dtos.TasksDistributionSample;
import com.docshifter.core.metrics.entities.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Julian Isaac on 02.08.2021
 */
public interface DashboardRepository extends JpaRepository<Dashboard, String> {

    List<TasksDistributionSample> getAllBy();

}

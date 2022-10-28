package com.docshifter.core.config.services;

import com.docshifter.core.config.InstallationType;
import com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO;
import com.docshifter.core.utils.NetworkUtils;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Service
@Log4j2
public class DefaultServiceHeatbeatDTOSupplier implements Supplier<Set<ServiceHeartbeatDTO>> {
	private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	private final String applicationName;
	private final ApplicationAvailability applicationAvailability;
	private final DataSourceHealthIndicator dataSourceHealthIndicator;
	private final InstallationType installationType;
	private final HealthManagementService healthManagementService;
	private final BooleanSupplier primaryInstanceSupplier;
	private final FileStore currFileStore;

	public DefaultServiceHeatbeatDTOSupplier(@Value("${spring.application.name:unnamed}") String applicationName,
											 ApplicationAvailability applicationAvailability,
											 DataSourceHealthIndicator dataSourceHealthIndicator,
											 InstallationType installationType,
											 HealthManagementService healthManagementService,
											 @Qualifier("primaryInstanceSupplier") Optional<BooleanSupplier> primaryInstanceSupplier) throws IOException {
		this.applicationName = applicationName;
		this.applicationAvailability = applicationAvailability;
		this.dataSourceHealthIndicator = dataSourceHealthIndicator;
		this.installationType = installationType;
		this.healthManagementService = healthManagementService;
		this.primaryInstanceSupplier = primaryInstanceSupplier.orElse(() -> false);
		this.currFileStore = Files.getFileStore(Path.of(""));

	}

	@Override
	public Set<ServiceHeartbeatDTO> get() {
		Long diskTotal = null, diskUsed = null;
		try {
			diskTotal = currFileStore.getTotalSpace();
			diskUsed = diskTotal - currFileStore.getUsableSpace();
		} catch (IOException ex) {
			log.error("Could not fetch disk info for current file store", ex);
		}

		ServiceHeartbeatDTO.DataPoints instance = new ServiceHeartbeatDTO.DataPoints(
				NetworkUtils.getLocalHostName(),
				osBean.getCpuLoad(),
				osBean.getTotalMemorySize() - osBean.getFreeMemorySize(),
				osBean.getTotalMemorySize(),
				diskUsed,
				diskTotal,
				null,
				null
		);

		ServiceHeartbeatDTO.DataPoints jvmComponent = new ServiceHeartbeatDTO.DataPoints(
				applicationName,
				osBean.getProcessCpuLoad(),
				Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
				Runtime.getRuntime().maxMemory(),
				diskUsed,
				diskTotal,
				null,
				null
		);

		return Set.of(new ServiceHeartbeatDTO(
				ZonedDateTime.now(),
				ServiceHeartbeatDTO.InstallationType.mapFrom(installationType),
				ServiceHeartbeatDTO.Status.mapFrom(healthManagementService.isAppReady(),
						applicationAvailability.getLivenessState()),
				dataSourceHealthIndicator.health().getStatus() == Status.UP,
				primaryInstanceSupplier.getAsBoolean(),
				instance,
				jvmComponent));
	}
}

package com.docshifter.core.metrics.dtos;

import com.docshifter.core.utils.NetworkUtils;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.boot.availability.LivenessState;

import java.time.ZonedDateTime;

public record ServiceHeartbeatDTO(ZonedDateTime timestamp,
								  InstallationType installationType,
								  Status status,
								  boolean dbConnection,
								  boolean isPrimary,
								  DataPoints instance,
								  DataPoints jvmComponent) {
	/**
	 * Generates a friendly component ID by combining the name of a component with the one of an instance. This should
	 * form a unique identifier across all the different DocShifter components and machines present in an installation.
	 */
	public static String generateComponentId(String componentName, String instanceName) {
		return componentName + " (" + instanceName + ")";
	}

	/**
	 * Generates a friendly component ID by combining the name of a component with the one of the current instance.
	 * This should form a unique identifier across all the different DocShifter components and machines present in an
	 * installation.
	 */
	public static String generateComponentId(String componentName) {
		return generateComponentId(componentName, NetworkUtils.getLocalHostName());
	}

	/**
	 * Gets a friendly component ID for this heartbeat. It should be unique across all the different DocShifter components
	 * and machines present in an installation.
	 */
	public String getComponentId() {
		return generateComponentId(jvmComponent().name(), instance().name());
	}

	public enum InstallationType {
		CLASSICAL,
		CONTAINERIZED;

		public static InstallationType mapFrom(com.docshifter.core.config.InstallationType installationType) {
			switch (installationType) {
				case CLASSICAL -> {
					return CLASSICAL;
				}
				case CONTAINERIZED_GENERIC, CONTAINERIZED_KUBERNETES -> {
					return CONTAINERIZED;
				}
				default -> throw new IllegalArgumentException("No mapping for " + installationType + " found.");
			}
		}
	}

	public enum Status {
		STARTING,
		UP,
		UNHEALTHY,
		DOWN,
		GONE;

		/**
		 * Indicates whether this component is running/working/active.
		 */
		public boolean isActive() {
			return this != DOWN && this != GONE;
		}

		/**
		 * Indicates whether this component is active AND has fully started up, and therefore is ready to perform useful
		 * work.
		 */
		public boolean isReady() {
			return this == UP || this == UNHEALTHY;
		}

		public static Status mapFrom(boolean isAppReady, LivenessState livenessState) {
			if (!isAppReady) {
				return STARTING;
			}

			if (livenessState != LivenessState.CORRECT) {
				return UNHEALTHY;
			}

			return UP;
		}

		public static Status mapFrom(InstanceInfo.InstanceStatus eurekaStatus) {
			return switch (eurekaStatus) {
				case UP -> UP;
				case DOWN, OUT_OF_SERVICE, UNKNOWN -> DOWN;
				case STARTING -> STARTING;
			};
		}
	}

	public record DataPoints(String name,
							 Double cpuUsage,
							 Long memoryUsage,
							 Long memoryMax,
							 Long diskUsage,
							 Long diskMax,
							 Double diskPressure,
							 Long networkUsage) {
	}
}

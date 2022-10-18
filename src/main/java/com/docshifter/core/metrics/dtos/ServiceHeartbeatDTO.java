package com.docshifter.core.metrics.dtos;

import com.netflix.appinfo.InstanceInfo;
import org.springframework.boot.availability.LivenessState;

import java.time.ZonedDateTime;

public record ServiceHeartbeatDTO(ZonedDateTime timestamp,
								  InstallationType installationType,
								  Status status,
								  boolean dbConnection,
								  DataPoints instance,
								  DataPoints jvmComponent) {
	/**
	 * Gets a friendly component ID for this heartbeat. It should be unique across all the different DocShifter components
	 * and machines present in an installation.
	 */
	public String getComponentId() {
		return jvmComponent().name() + " (" + instance.name() + ")";
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
							 Double networkPressure) {
	}
}

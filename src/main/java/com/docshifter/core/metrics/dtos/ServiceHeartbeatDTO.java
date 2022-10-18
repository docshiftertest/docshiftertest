package com.docshifter.core.metrics.dtos;

import org.springframework.boot.availability.LivenessState;

import java.time.ZonedDateTime;

public record ServiceHeartbeatDTO(ZonedDateTime timestamp,
								  InstallationType installationType,
								  Status status,
								  boolean dbConnection,
								  DataPoints instance,
								  DataPoints jvmComponent) {
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

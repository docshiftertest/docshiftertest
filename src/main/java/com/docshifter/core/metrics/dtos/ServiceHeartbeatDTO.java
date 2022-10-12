package com.docshifter.core.metrics.dtos;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.LivenessState;

import java.time.ZonedDateTime;

public record ServiceHeartbeatDTO(ZonedDateTime timestamp,
								  InstallationType installationType,
								  Status status,
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
		UP_NO_DATABASE,
		UNHEALTHY,
		DOWN,
		GONE;

		public static Status mapFrom(boolean isAppReady, LivenessState livenessState, Health dbHealth) {
			if (!isAppReady) {
				return STARTING;
			}

			if (livenessState != LivenessState.CORRECT) {
				return UNHEALTHY;
			}

			return dbHealth.getStatus() == org.springframework.boot.actuate.health.Status.UP ? UP : UP_NO_DATABASE;
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

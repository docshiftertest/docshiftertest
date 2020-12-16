package com.docshifter.core.monitoring;

import com.docshifter.core.monitoring.entities.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocshifterMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocshifterMonitoringApplication.class, args);

		Configuration config = new Configuration();
		config.setName("test1");

	}
}

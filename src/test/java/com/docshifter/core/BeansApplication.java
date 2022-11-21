package com.docshifter.core;

import com.docshifter.core.config.DocShifterConfiguration;
import com.docshifter.datasource.config.audit.ConfigurationAuditDB;
import com.docshifter.datasource.config.docshifter.ConfigurationDocshifterDB;
import com.docshifter.datasource.config.metrics.ConfigurationMetricsDB;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Created by Juan Marques on 09/03/2021
 */
@SpringBootApplication(scanBasePackageClasses = {DocShifterConfiguration.class,ConfigurationDocshifterDB.class, ConfigurationMetricsDB.class})
public class BeansApplication {
}

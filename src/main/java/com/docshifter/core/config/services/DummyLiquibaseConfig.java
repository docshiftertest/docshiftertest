package com.docshifter.core.config.services;

import org.springframework.context.annotation.Configuration;

/**
 * Needed to control bean initialization order. The real Liquibase config (in MQ) needs to run before we try to run
 * any queries on our database in other config/service classes.
 */
@Configuration("liquibaseConfig")
public class DummyLiquibaseConfig {
}

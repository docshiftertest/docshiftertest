package com.docshifter.core.config.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;

/**
 * Needed to control bean initialization order. The real Liquibase config (in MQ) needs to run before we try to run
 * any queries on our database in other config/service classes.
 */
@Configuration("liquibaseConfig")
@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
public class DummyLiquibaseConfig {
}

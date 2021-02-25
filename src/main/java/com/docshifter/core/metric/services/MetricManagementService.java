package com.docshifter.core.metric.services;

import java.sql.Connection;

public interface MetricManagementService {

    Connection connect();

    int successfulWfs();
}

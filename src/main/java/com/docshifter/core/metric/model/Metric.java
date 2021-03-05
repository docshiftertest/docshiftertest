package com.docshifter.core.metric.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(schema="metrics")
public class Metric {

@Id
private String id;

private int counts;
}

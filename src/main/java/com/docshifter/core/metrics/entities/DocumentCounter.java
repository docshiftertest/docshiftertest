package com.docshifter.core.metrics.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(schema="metrics")
@Getter
@Setter
@NoArgsConstructor
public class DocumentCounter {

@Id
private String id;

private int counts;

}

package com.docshifter.core.metrics.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;


@Entity(name="DocumentCounter")
@Table(schema="METRICS", name="DOCUMENT_COUNTER")
@Getter
@Setter // these do Gets and Sets automatically
@NoArgsConstructor
public class DocumentCounter {

@Id
private String task_id;

private BigInteger counts;

//public void setId(String id) { this.task_id = id;}
//
//public String getId() { return this.task_id;}
//
//public void setCounts(int counts) { this.counts=counts;}
//
//public int getCounts() { return this.counts;}

}

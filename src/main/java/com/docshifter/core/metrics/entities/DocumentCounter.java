package com.docshifter.core.metrics.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity(name="DocumentCounter")
@Getter
@Setter // these do Gets and Sets automatically
@NoArgsConstructor
public class DocumentCounter {

@Id
private String task_id;

private long counts;

//non-fugly Camel_Case_Underscore methods; @Getter and @Setter create these automatically otherwise
public void setTaskId(String taskId){ this.task_id = taskId;}

public String getTaskId() { return this.task_id;}

}

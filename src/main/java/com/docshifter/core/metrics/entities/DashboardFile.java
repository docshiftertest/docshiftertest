package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by Julian Isaac on 02.08.2021
 */
@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
public class DashboardFile {

    @Id
    private Long id;
    private long fileSize;
    @Column(length = 8192)
    private Long childFileCount;
    private String fileName;
    @ManyToOne
    @JoinColumn(name="task_id")
    private Dashboard dashboard;
}

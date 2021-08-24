package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dashboard_file_generator")
    @SequenceGenerator(name="dashboard_file_generator", sequenceName = "dashboard_file_seq")
    private Long id;
    private long fileSize;
    @Column(length = 8192)
    private Long childFileCount;
    private String fileName;
    @ManyToOne
    @JoinColumn(name="task_id")
    private Dashboard dashboard;
}

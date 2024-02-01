package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Julian Isaac on 02.08.2021
 */
@Builder
@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
public class DashboardFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long fileSize;

    @Column(length = 8192)
    private Long childFileCount;

    private String fileName;

    private long pageCount;

    private boolean input;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Dashboard dashboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parentDashboardFile")
    private DashboardFile parentDashboardFile;

    @OneToMany(mappedBy = "parentDashboardFile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DashboardFile> childDashboardFiles;
}

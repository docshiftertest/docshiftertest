package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

/**
 * Created by Julian Isaac on 02.08.2021
 */
@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
public class Dashboard {

    @Id
    private String taskId;
    private String senderHostName;
    private String receiverHostName;
    private Long senderPickedUp;
    private String workflowName;
    private Long onMessageHit;
    private Long processingDuration;
    private Long finishTimestamp;
    private Boolean success;
    private Boolean isLicensed;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardTaskMessage> taskMessages;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardFile> taskFiles;
}

package com.docshifter.core.metrics.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
    private String fileName;
    private Long onMessageHit;
    private Long processingDuration;
    private Long finishTimestamp;
    private Long fileSize;
    private Boolean success;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardTaskMessage> taskMessages;
}

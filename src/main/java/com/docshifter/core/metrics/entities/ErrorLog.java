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

@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
public class ErrorLog {

    @Id
    private String taskId;
    private String senderHostName;
    private String receiverHostName;
    private Long onMessageHit;
    private String workflowName;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardTaskMessage> taskMessages;
    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardFile> taskFiles;
}

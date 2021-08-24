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

/**
 * Created by Julian Isaac on 02.08.2021
 */
@Entity
@Getter
@Setter // these do Gets and Sets automatically
@AllArgsConstructor
@NoArgsConstructor
public class DashboardTaskMessage {
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 8192)
    private String taskMessage;
    @ManyToOne()
    @JoinColumn(name="task_id")
    private Dashboard dashboard;

    public void setTaskMessage(String message) {
        // Db column for storing the message is varchar(8192) so make sure we don't hit SQL right-truncation error when saving...
        if (message != null && message.length() > 8192) {
            taskMessage = message.substring(0, 8192);
        }
        else {
            taskMessage = message;
        }
    }
}
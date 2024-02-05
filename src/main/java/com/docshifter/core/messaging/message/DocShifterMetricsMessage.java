package com.docshifter.core.messaging.message;

import com.docshifter.core.work.WorkFolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Julian Isaac on 29.07.2021
 */
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class DocShifterMetricsMessage implements Serializable {
    private String taskId;
    private String hostName;
    private List<String> documentPathList;
    private WorkFolder workFolder;

    public abstract MessageSource getMessageSource();
}

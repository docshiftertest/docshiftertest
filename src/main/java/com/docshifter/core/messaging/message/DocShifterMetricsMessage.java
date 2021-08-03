package com.docshifter.core.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

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

    public abstract MessageSource getMessageSource();
}

package com.docshifter.core.metrics.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskMessage implements Serializable {
   private String message;

    public void setMessage(String message) {
        // Db column for storing the message is varchar(8192) so make sure we don't hit SQL right-truncation error when saving...
        if (message != null && message.length() > 8192) {
            this.message = message.substring(0, 8192);
        }
        else {
            this.message = message;
        }
    }
}

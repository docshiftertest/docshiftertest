package com.docshifter.core.audit.entities;

public interface AuditInfo {


    String getUsername();
    void setUsername(String username);

    long getEventDateTime();
    void setEventDateTime(long eventDateTime);

}

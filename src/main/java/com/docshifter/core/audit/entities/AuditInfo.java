package com.docshifter.core.audit.entities;

public interface AuditInfo {


    String getUsername();
    void setUsername(String username);

    long getEventDateTime();
    void setEventDateTime(long eventDateTime);

    String getOldValue();
    void setOldValue(String oldValue);

    String getNewValue();
    void setNewValue(String newValue);

    String getPropertyName();
    void setPropertyName(String propertyName);

}

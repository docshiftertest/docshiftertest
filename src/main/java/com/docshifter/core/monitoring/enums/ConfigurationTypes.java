package com.docshifter.core.monitoring.enums;


/**
 * Created by blazejm on 12.05.2017.
 */
public enum ConfigurationTypes {
    mail(com.docshifter.core.monitoring.enums.ConfigurationTypes.MAIL),
    webhook(com.docshifter.core.monitoring.enums.ConfigurationTypes.WEBHOOK),
    snmp(com.docshifter.core.monitoring.enums.ConfigurationTypes.SNMP),
    db(com.docshifter.core.monitoring.enums.ConfigurationTypes.DB);

    public static final String MAIL = "mail";
    public static final String WEBHOOK = "webhook";
    public static final String SNMP = "snmp";
    public static final String DB = "db";

    private final String text;

    ConfigurationTypes(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;
import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by blazejm on 11.05.2017.
 */
public class MailConfigurationItemDto extends AbstractConfigurationItemDto {
    private String host;
    private int port;
    private String username;
    private String password;
    private String encryptedPassword;
    private String fromAddress;
    private boolean ssl;

    private String toAddresses;

    private String templateTitle;
    private String templateBody;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
    	
    	if(StringUtils.isBlank(password)) {
    		return SecurityUtils.decryptMessage(this.getEncryptedPassword(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
    				SecurityProperties.SECRET.getValue(), this.getClass());
    	}
    	
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.setEncryptedPassword();
    }

    public String getEncryptedPassword() {
		return encryptedPassword;
	}

	private void setEncryptedPassword() {
		this.encryptedPassword = SecurityUtils.encryptMessage(this.getPassword(), SecurityProperties.DEFAULT_ALGORITHM.getValue(),
				SecurityProperties.SECRET.getValue(), this.getClass());
	}

	public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getToAddresses() {
        return toAddresses;
    }

    public void setToAddresses(String toAddresses) {
        this.toAddresses = toAddresses;
    }

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }

    public String getTemplateBody() {
        return templateBody;
    }

    public void setTemplateBody(String templateBody) {
        this.templateBody = templateBody;
    }

    @Override
    public ConfigurationTypes getType() {
        return ConfigurationTypes.mail;
    }

    public List<String> getToAddressList() {
        if (toAddresses == null) {
            return null;
        }
        return Arrays.stream(toAddresses.split("[\\s;,]"))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }
}

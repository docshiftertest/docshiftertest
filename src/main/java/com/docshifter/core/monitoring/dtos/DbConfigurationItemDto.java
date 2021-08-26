package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

/**
 * Created by blazejm on 29.09.2017.
 */
@ToString
public class DbConfigurationItemDto extends AbstractConfigurationItemDto {
    private String driver;
    private String connection;
    private String dbUser;
    @ToString.Exclude
    private String password;
    private String encryptedPassword;
    private String tableName;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public ConfigurationTypes getType() {
        return ConfigurationTypes.db;
    }
}

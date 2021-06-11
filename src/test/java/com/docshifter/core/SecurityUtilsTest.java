package com.docshifter.core;

import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Created by Juan Marques on 11/06/2021
 */
@RunWith(JUnit4.class)
@Log4j2
public class SecurityUtilsTest {

    /**
     * Validates if the message won't be double encrypted
     * The input message should match the output message from encrypt method.
     */
    @Test
    public void isAlreadyEncrypted() {
        log.info("Running isAlreadyEncrypted...");

        String message = "stoopidEncryptor";

        log.info("Message to be encrypted {} ", message);
        // I can't pass the message already encrypted due different JVM and that will make encryptor to fail if we build in CB
        String encryptedMessage = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        log.info("Encrypted message {} ", encryptedMessage);

        String doubleEncryptedMessage = SecurityUtils.encryptMessage(encryptedMessage, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        log.info("Message after going through encryptMessage again....{}", doubleEncryptedMessage);

        Assert.assertEquals(encryptedMessage, doubleEncryptedMessage);
    }

    /**
     * Validates if the message is encrypted
     * AND if the input message matches with decrypted password
     */
    @Test
    public void shouldEncryptThenDecrypt() {
        log.info("Running shouldEncryptThenDecrypt...");

        String message = "DOCSHIFTER";

        String encryptedMessage = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        Assert.assertNotEquals(message, encryptedMessage);

        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        Assert.assertEquals(message, decryptedMessage);
    }
}

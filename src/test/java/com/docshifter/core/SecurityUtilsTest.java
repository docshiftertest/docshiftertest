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

        String message = "WzBZMDgGCSqGSIb3DQEFDDArBBTgb0i0adEOKHa4aaz8tGBajfRDBAICEAACASAwDAYIKoZIhvcNAgoFADAdBglghkgBZQMEASoEEADU+dk1C+rh6E+Sl9GJajKYCZc6s4OxW82hX56kjfX3dkJpb7SnfXdPhhPKk8oKlXN8LwrjrsXh2cBpyBTCib4=";

        String encryptedMessage = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        Assert.assertEquals(message, encryptedMessage);
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

package com.docshifter.core;

import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Created by Juan Marques on 11/06/2021
 */
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

        assertEquals(encryptedMessage, doubleEncryptedMessage);
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

        assertNotEquals(message, encryptedMessage);

        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        assertEquals(message, decryptedMessage);
    }

    private static Stream<Arguments> encryptionStrings() {
        return Stream.of(
                arguments("banana", "banana"),
                arguments("banana", "WzBZMDgGCSqGSIb3DQEFDDArBBSfE8LKnYX/wRE17zhb0KL5vVb2UAICEAACASAwDAYIKoZIhvcNAgoFADAdBglghkgBZQMEASoEEHB0F7LCwe83TZ97NK5rSQaAGIc9jNc3+kL53l6rOZKt"),
                arguments("CHjz32A4R7ndJu", "CHjz32A4R7ndJu"),
                arguments("CHjz32A4R7ndJu", "6ROWM6KAT9l7t/B/2C9yrvQeRH4eZdrMscjzDDYmEIMO+FfuXYCZOCKtPdJPxoNX")
        );
    }

    @ParameterizedTest
    @MethodSource("encryptionStrings")
    public void shouldDecrypt(String decrypted, String encrypted) {
        log.info("Running shouldDecrypt...");
        String decryptedMessage = SecurityUtils.decryptMessage(encrypted, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertEquals(decrypted, decryptedMessage, "Message: [" + decrypted + "] should be equal to decryptedMessage: [" + decryptedMessage + "]!");
    }

    @Test
    public void shouldDecryptEmpty() {
        log.info("Running shouldDecryptEmpty...");
        String message = "";
        String encryptedMessage = "";
        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertEquals(decryptedMessage, message, "Message: [" + message + "] should be equal to decryptedMessage: [" + decryptedMessage + "] when empty!");
    }

    @Test
    public void shouldDecryptNull() {
        log.info("Running shouldDecryptNull...");
        String decryptedMessage = SecurityUtils.decryptMessage(null, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertNull(decryptedMessage, "Decrypted message should be null");
    }
}

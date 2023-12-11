package com.docshifter.core;

import com.docshifter.core.security.utils.SecurityProperties;
import com.docshifter.core.security.utils.SecurityUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Created by Juan Marques on 11/06/2021
 */
@Log4j2
class SecurityUtilsTest {

    /**
     * Validates if the message won't be double encrypted
     * The input message should match the output message from encrypt method.
     */
    @Test
    void isAlreadyEncrypted() {
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
    void shouldEncryptThenDecrypt() {
        log.info("Running shouldEncryptThenDecrypt...");

        String message = "DOCSHIFTER";

        String encryptedMessage = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        assertNotEquals(message, encryptedMessage);

        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);

        assertEquals(message, decryptedMessage);
    }

    @Test
    void shouldDecrypt() {
        log.info("Running shouldDecrypt...");
        String message = "banana";
        String encryptedMessage = "WzBZMDgGCSqGSIb3DQEFDDArBBSfE8LKnYX/wRE17zhb0KL5vVb2UAICEAACASAwDAYIKoZIhvcNAgoFADAdBglghkgBZQMEASoEEHB0F7LCwe83TZ97NK5rSQaAGIc9jNc3+kL53l6rOZKt";
        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertEquals(message, decryptedMessage, "Message: [" + message + "] should be equal to decryptedMessage: [" + decryptedMessage + "]!");
    }

    @Test
    void shouldDecryptEmpty() {
        log.info("Running shouldDecryptEmpty...");
        String message = "";
        String encryptedMessage = "";
        String decryptedMessage = SecurityUtils.decryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertEquals(message, decryptedMessage, "Message: [" + message + "] should be equal to decryptedMessage: [" + decryptedMessage + "] when empty!");
    }

    @Test
    void shouldDecryptNull() {
        log.info("Running shouldDecryptNull...");
        String decryptedMessage = SecurityUtils.decryptMessage(null, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertNull(decryptedMessage, "Decrypted message should be null");
    }

    private static Stream<Arguments> provideEncryptionMessages() {
        return Stream.of(
                Arguments.of("A83pIT+pTC1gXFCR"),
                Arguments.of("A83pIT"),
                Arguments.of("Abc"),
                Arguments.of("A"),
                Arguments.of("Ab"),
                Arguments.of("A83"),
                Arguments.of("Bbc"),
                Arguments.of("A83pIT+pTC1gXFCRqqqqqqqqqqqqq"),
                Arguments.of("A83pIT+pTC1gXFCRqqqqqqqqqqqq"),
                Arguments.of("B83pIT+pTC1gXFCR")
        );
    }

    @ParameterizedTest
    @MethodSource("provideEncryptionMessages")
    void shouldEncrypt(String message) {
        String encrypted = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        String result = SecurityUtils.decryptMessage(encrypted, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertNotEquals(message, encrypted);
        assertEquals(message, result);
    }

    @Test
    void ensureNoDoubleEncryption() {
        final String message = "WzBZMDgGCSqGSIb3DQEFDDArBBRsG3zFKsdNmCfZ1RrgWXvwT5qApwICEAACASAwDAYIKoZIhvcNAgoFADAdBglghkgBZQMEASoEEHPuyeO8kXHhJ6SpLJJGmrjgZpsqxfHn0rTOVKwT2hT0lUff9qKPT1i5cAAI/XM5JA==";
        String result = SecurityUtils.encryptMessage(message, SecurityProperties.DEFAULT_ALGORITHM.getValue(),
                SecurityProperties.SECRET.getValue(), this);
        assertEquals(message, result);
    }
}

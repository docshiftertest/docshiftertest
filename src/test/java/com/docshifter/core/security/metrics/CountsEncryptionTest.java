package com.docshifter.core.security.metrics;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class CountsEncryptionTest {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private CountsEncryption encryptor;
    private Cipher cipher;

    private File encrypted;
    private File decrypted;

    private static final Logger logger = Logger.getLogger(CountsEncryptionTest.class);

    @Before
    public void setUp() {
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(1024);
            gk.createKeys();
            gk.writeToFile("target/test-classes/publicKey", gk.getPublicKey().getEncoded());
            gk.writeToFile("target/test-classes/privateKey", gk.getPrivateKey().getEncoded());

            this.encrypted = new File("target/test-classes/encrypted");
            this.decrypted = new File ("target/test-classes/decrypted");

            this.cipher = Cipher.getInstance("RSA");
            this.encryptor = new CountsEncryption();
            publicKey = encryptor.getPublic("target/test-classes/publicKey");
            privateKey = this.getPrivate("target/test-classes/privateKey");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @After
    public void tearDown() {
        File puk = new File ("target/test-classes/publicKey");
        File prk = new File ("target/test-classes/privateKey");
        File encrypted = new File("target/test-classes/encrypted");
        File decrypted = new File ("target/test-classes/decrypted");
        puk.delete();
        prk.delete();
        encrypted.delete();
        decrypted.delete();
    }

    public void decryptFile(byte[] input, File output, PrivateKey key)
            throws IOException, GeneralSecurityException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        encryptor.writeToFile(output, this.cipher.doFinal(input));
    }

    public PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    @Test
    public void encryptFileSuccessfully() {

        String toEncrypt = "How are you now";
        try {
            encryptor.encryptFile(toEncrypt.getBytes(StandardCharsets.UTF_8), new File("target/test-classes/encrypted"), publicKey);
            decryptFile(encryptor.getFileInBytes(new File("target/test-classes/encrypted")),
                    new File("target/test-classes/decrypted"), privateKey);

            Scanner myReader = new Scanner(decrypted);
            String data = myReader.nextLine();
            logger.info(data);
            assertThat(toEncrypt).isEqualTo(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void canEncryptLargeStrings(){
        String toEncrypt = "";
        for (int i = 0; i < 129; i++) {
            toEncrypt = toEncrypt + "a";
        }
        logger.info(toEncrypt.getBytes(StandardCharsets.UTF_8).length);


        try {
            encryptor.encryptFile(toEncrypt.getBytes(StandardCharsets.UTF_8), new File("target/test-classes/encrypted"), publicKey);
            decryptFile(encryptor.getFileInBytes(new File("target/test-classes/encrypted")),
                    new File("target/test-classes/decrypted"), privateKey);

            Scanner myReader = new Scanner(decrypted);
            String data = myReader.nextLine();
            logger.info(data);
            assertThat(toEncrypt).isEqualTo(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
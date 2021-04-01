package com.docshifter.core.security.metrics;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

public class CountsEncryptionTest {
    // Global variables for the tests
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private CountsEncryption encryptor;
    private Cipher cipher;

    private File encrypted;
    private File decrypted;

    private static final Logger logger = Logger.getLogger(CountsEncryptionTest.class);

    @Before
    public void setUp() {
        //Generates the security keys and target files for the tests
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(2048);
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
        // Deletes the security key files and target files
        File puk = new File ("target/test-classes/publicKey");
        File prk = new File ("target/test-classes/privateKey");
        File encrypted = new File("target/test-classes/encrypted");
        File decrypted = new File ("target/test-classes/decrypted");
        puk.delete();
        prk.delete();
        encrypted.delete();
        decrypted.delete();
    }

    //Decrypts an encrypted file
    public void decryptFile(byte[] input, File output, PrivateKey key)
            throws IOException, GeneralSecurityException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        encryptor.writeToFile(output, this.cipher.doFinal(input));
    }

    //Retrives the private key from a file
    public PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    @Test
    @Ignore
    public void encryptFileSuccessfully() {
    //Encrypts a sample file
        String toEncrypt = "How are you now";
        try {
            encryptor.encryptFile(toEncrypt.getBytes(StandardCharsets.UTF_8), encrypted, publicKey);
            decryptFile(encryptor.getFileInBytes(encrypted), decrypted, privateKey);

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
    @Ignore
    public void canEncryptLargeStrings(){
        //Encrypts a large file
        //NOTE: RSA encryption can only encrypt up to ((key length in bits)/8 - 11) bytes
        // For the 2048-bit keys used in the tests and delivered to customers
        // this maximum length is 245 bytes
        String toEncrypt = "";
        for (int i = 0; i < 245; i++) {
            toEncrypt = toEncrypt + "a";
        }
        logger.info(toEncrypt.getBytes(StandardCharsets.UTF_8).length);
        String data = "";

        try {
            encryptor.encryptFile(toEncrypt.getBytes(StandardCharsets.UTF_8), encrypted, publicKey);
            decryptFile(encryptor.getFileInBytes(encrypted), decrypted, privateKey);

            Scanner myReader = new Scanner(decrypted);
            data = myReader.nextLine();
            logger.info(data);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(toEncrypt).isEqualTo(data);
    }

    @Test
    @Ignore
    public void canEncryptSmallStrings(){
        // Another length text, this one with the encrypted string closer to the actual
        // file that DocShifter receives, with the maximum lengths of the variable values
        long counts = 9223372036854775807L;
        long tasks = 9223372036854775807L;
        long failed = 9223372036854775807L;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String timestamp = dateFormat.format(date); //timestamp as a variable to be included in file name and content

        String toEncrypt = "Files: " + counts +
                "   \n" + "Tasks:" + tasks + "     \n"
                + "At: " + timestamp + failed + "\n";

        StringBuilder data = new StringBuilder();

        logger.info("Size:" + toEncrypt.getBytes(StandardCharsets.UTF_8).length);
        try {
            encryptor.encryptFile(toEncrypt.getBytes(StandardCharsets.UTF_8), encrypted, publicKey);
            decryptFile(encryptor.getFileInBytes(encrypted), decrypted, privateKey);

            Scanner myReader = new Scanner(decrypted);

            while (myReader.hasNext()){
                data.append(myReader.nextLine());
                data.append("\n");
            }
            logger.info(data.toString());

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        assertThat(toEncrypt).isEqualTo(data.toString());
    }

}
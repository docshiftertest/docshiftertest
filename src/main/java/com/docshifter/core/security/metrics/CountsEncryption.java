package com.docshifter.core.security.metrics;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
/**
 * Class to handle the task of encrypting the counts file.
 *
 * @author Boyan Dunev
 *  Following the guide at: https://mkyong.com/java/java-asymmetric-cryptography-example/
 *
 */
public class CountsEncryption {
    private Cipher cipher;

    private static final Logger logger = Logger.getLogger(new Object() {
    }.getClass().getEnclosingClass());

    public CountsEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA");
    }

    //Retrives the public key from its file
    public PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    //Encrypts a file using the public key
    public void encryptFile(byte[] input, File output, PublicKey key)
            throws IOException, GeneralSecurityException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        writeToFile(output, this.cipher.doFinal(input));
    }

    //Writes a byte array to a file
    public void writeToFile(File output, byte[] toWrite)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
                FileOutputStream fos = new FileOutputStream(output)){
            baos.write(toWrite);
            fos.write(baos.toByteArray());
        }
    }

    //Returns a file in bytes (used to get the unencrypted file bytes for the writeToFile method
    public byte[] getFileInBytes(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        byte[] fbytes = new byte[(int) f.length()];
        fis.read(fbytes);
        fis.close();
        return fbytes;
    }
}
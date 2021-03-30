package com.docshifter.core.security.metrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
/**
 * Class to handle the task of encrypting the counts file.
 *
 * @author Boyan Dunev, 25-03-2021
 *  Following the guide at: https://mkyong.com/java/java-asymmetric-cryptography-example/
 *
 */
public class CountsEncryption {
    private Cipher cipher;

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
            throws  IOException {
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
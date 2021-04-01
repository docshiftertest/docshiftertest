package com.docshifter.core.security.metrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class GenerateKeys {

    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public GenerateKeys(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keylength);
    }

    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static void main(String[] args) {
        // Run to generate a public and private key to be used by the DocShifter processes;
        // command-line arguments determine the location where they will be saved
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(2048);
//            gk.createKeys();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream("C:/Users/bvlad/sender_keystore.p12"), "changeit".toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("senderKeyPair", "changeit".toCharArray());
//            PublicKey publicKey = (PublicKey) keyStore.getKey("senderKeyPair", "changeit".toCharArray());
//            gk.writeToFile(args[0], publicKey.getEncoded()); //first CL argument is the path and file name for the public key
           System.out.println(Arrays.toString(privateKey.getEncoded()));
            gk.writeToFile(args[1], privateKey.getEncoded()); //second CL argument is the path and file name for the private key
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


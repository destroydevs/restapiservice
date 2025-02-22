package ru.destroy.restapi.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Optional;

public class AESCrypt {

    private SecretKey secretKey;
    private static AESCrypt crypt = null;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static AESCrypt getCrypt() {
        if (crypt != null) return crypt;
        crypt = new AESCrypt();
        return crypt;
    }

    private AESCrypt() {
        generateKey();
    }

    private void generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
            keyGen.init(256);
            this.secretKey = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Hex.toHexString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Optional<String> decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Hex.decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return Optional.of(new String(decryptedBytes));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String getKey() {
        return Hex.toHexString(secretKey.getEncoded());
    }

    public void setKey(String hexKey) {
        byte[] decodedKey = Hex.decode(hexKey);
        this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}

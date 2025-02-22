package ru.destroy.restapi.crypto;

import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;

public class SCrypt {

    public static String hashPassword(String password, Salt salt) {
        try {
            byte[] derived = org.bouncycastle.crypto.generators.SCrypt.generate(
                    password.getBytes(),
                    salt.data(),
                    16384, // cost
                    8, // blocksize
                    1, // xz
                    salt.length()
            );
            return Hex.toHexString(derived);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static Salt generateSalt(int length) {
        byte[] salt = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return new Salt(salt,length);
    }

    public record Salt(byte[] data, int length) {
        public String saltToString() {
            return Hex.toHexString(data);
        }

        public static byte[] saltToByteArray(String salt) {
            return Hex.decode(salt);
        }

    }
}

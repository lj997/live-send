package com.livesend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 16;

    @Value("${app.encryption.key:LiveSend-Secret-Key-2024-For-Email-Password}")
    private String masterKey;

    private Argon2PasswordEncoder passwordEncoder;

    public EncryptionService() {
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 1 << 14, 2);
    }

    // ============================================
    // 单向加密 - 用于用户登录密码（不可逆）
    // ============================================

    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return plainPassword;
        }
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        if (!isHashed(hashedPassword)) {
            return plainPassword.equals(hashedPassword);
        }
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    public boolean isHashed(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.startsWith("$argon2");
    }

    // ============================================
    // 双向加密 - 用于邮件密码（可解密）
    // ============================================

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        if (isEncrypted(plainText)) {
            return plainText;
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            SecretKey key = deriveKey(masterKey, salt);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[SALT_LENGTH + IV_LENGTH + encrypted.length];
            System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
            System.arraycopy(iv, 0, result, SALT_LENGTH, IV_LENGTH);
            System.arraycopy(encrypted, 0, result, SALT_LENGTH + IV_LENGTH, encrypted.length);

            return "ENC:" + Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        if (!isEncrypted(encryptedText)) {
            return encryptedText;
        }
        try {
            String base64Data = encryptedText.substring(4);
            byte[] data = Base64.getDecoder().decode(base64Data);

            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(data, 0, salt, 0, SALT_LENGTH);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(data, SALT_LENGTH, iv, 0, IV_LENGTH);
            byte[] encrypted = new byte[data.length - SALT_LENGTH - IV_LENGTH];
            System.arraycopy(data, SALT_LENGTH + IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKey key = deriveKey(masterKey, salt);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.startsWith("ENC:");
    }

    // ============================================
    // 辅助方法
    // ============================================

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                KEY_ITERATION_COUNT,
                KEY_LENGTH
        );
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }
}

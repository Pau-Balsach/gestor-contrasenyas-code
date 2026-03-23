package com.mycompany.gestorcontrasenyas.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class Encriptacion {

    private static final int IV_SIZE = 16;

    public static String encrypt(byte[] keyBytes, String value) {
        try {
            // Generar IV aleatorio en cada cifrado
            byte[] ivBytes = new byte[IV_SIZE];
            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));

            // Concatenar IV + datos cifrados y codificar en Base64
            // Formato: [16 bytes IV][resto = datos cifrados]
            byte[] combined = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            System.out.println("Error encrypt: " + ex.getMessage());
            return null;
        }
    }

    public static String decrypt(byte[] keyBytes, String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // Extraer IV (primeros 16 bytes) y datos cifrados (el resto)
            byte[] ivBytes = new byte[IV_SIZE];
            byte[] encrypted = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, 0, ivBytes, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(encrypted);

            return new String(original, "UTF-8");
        } catch (Exception ex) {
            System.out.println("Error decrypt: " + ex.getMessage());
            return null;
        }
    }
}
package com.kael.kina.tools;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public final class DES {

    private DES(){}

    @NonNull public static byte[] encrypt(byte[] data, String key) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = factory.generateSecret(desKeySpec);
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
            return cipher.doFinal(data);
        } catch (Exception e) {
            Logger.error("Des encrypt Exception", e);
            return new byte[0];
        }
    }

    @NonNull public static byte[] decrypt(byte[] source, String key) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = factory.generateSecret(desKeySpec);
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
            return cipher.doFinal(source);
        } catch (Exception e) {
            Logger.error("Des encrypt Exception", e);
            return new byte[0];
        }
    }
}

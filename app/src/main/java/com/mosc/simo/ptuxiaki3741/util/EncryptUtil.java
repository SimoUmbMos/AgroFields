package com.mosc.simo.ptuxiaki3741.util;

import android.os.Build;
import android.util.Log;

import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
    public static final String
            TAG = "EncryptUtil",
            divider = "::encoded::";
    public static String convert4digit(long id){
        Random random = new Random();
        random.setSeed((id*4)/2);
        return String.format(Locale.getDefault(),"%04d", random.nextInt(10000));
    }
    public static String encryptString(String s){
        if(s == null)
            return null;
        try {
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            KeyGenerator keygen = KeyGenerator.getInstance("BLOWFISH");
            keygen.init(256);
            SecretKey key = keygen.generateKey();
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE,key);
            byte[] cipherText = cipher.doFinal(data);
            String keyString = encodeToString(keyToString(key));
            if( keyString != null){
                return encodeToString(cipherText) + divider + keyString;
            }
        } catch (Exception e) {
            Log.e(TAG, "encryptString: ",e);
        }
        return s;
    }
    public static String encryptPassword(String password){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            String hex;
            for (byte b : encodedHash) {
                hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }catch (Exception e){
            Log.e("UserClass", "encryptPassword: ", e);
        }
        return password;
    }
    public static void encryptCurrUser(User u){
        if(u.getEmail() != null){
            if(!u.getEmail().contains(divider)){
                u.setEmail(encryptString(u.getEmail()));
            }
        }
        if(u.getPhone() != null){
            if(!u.getPhone().contains(divider)){
                u.setPhone(encryptString(u.getPhone()));
            }
        }
    }
    public static User encrypt(User u) {
        User result = u;
        try{
            if(u != null){
                result = (User) u.clone();
                encryptCurrUser(result);
            }
        }catch (Exception e){
            Log.e(TAG, "encrypt: ",e);
        }
        return result;
    }
    public static User encryptWithPassword(User u) {
        User encryptedUser = encrypt(u);
        if(encryptedUser != null){
            if(encryptedUser.getPassword() != null){
                String password = encryptedUser.getPassword();
                encryptedUser.setPassword(encryptPassword(password));
            }
        }
        return encryptedUser;
    }

    public static String decryptString(String s){
        if(s == null)
            return null;
        if(!s.contains(divider))
            return s;
        try {
            int index = s.indexOf(divider);
            String encodedCipherText = s.substring(0,index);
            String encodedKeyText = s.substring(index+divider.length());
            SecretKey key = getKey(decode(encodedKeyText));
            byte[] data = decode(encodedCipherText);
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE,key);
            return new String(cipher.doFinal(data),StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "decryptString: ",e);
        }
        return s;
    }
    public static void decryptCurrUser(User u){
        if(u.getEmail() != null){
            if(u.getEmail().contains(divider)){
                u.setEmail(decryptString(u.getEmail()));
            }
        }
        if(u.getPhone() != null){
            if(u.getPhone().contains(divider)){
                u.setPhone(decryptString(u.getPhone()));
            }
        }
    }
    public static User decrypt(User u) {
        User result = u;
        try{
            if(u != null){
                result = (User) u.clone();
                decryptCurrUser(result);
            }
        }catch (Exception e){
            Log.e(TAG, "encrypt: ",e);
        }
        return result;
    }
    public static List<User> decryptAll(List<User> temp) {
        List<User> result = new ArrayList<>();
        for(User tempUser:temp){
            result.add(decrypt(tempUser));
        }
        return result;
    }

    //cipher getter
    private static Cipher getCipher(int mode,SecretKey key) throws Exception{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            return getCipherChaCha20(mode,key);
        }else{
            return getCipherBlowfish(mode,key);
        }
    }
    private static Cipher getCipherChaCha20(int mode,SecretKey key) throws Exception{
        Cipher cipher = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            byte[] nonceBytes = new byte[12];
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");
            cipher = Cipher.getInstance("ChaCha20");
            cipher.init(mode, keySpec, ivParameterSpec);
        }
        return cipher;
    }
    private static Cipher getCipherBlowfish(int mode,SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("BLOWFISH");
        cipher.init(mode, key);
        return cipher;
    }

    //helper
    private static String encodeToString(byte[] input){
        if(input == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return Base64.getEncoder().encodeToString(input);
        else
            return android.util.Base64.encodeToString(input,0);
    }
    private static byte[] decode(String input){
        if(input == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getDecoder().decode(
                    input
            );
        }else{
            return android.util.Base64.decode(
                    input,
                    0
            );
        }
    }
    private static byte[] keyToString(SecretKey secretKey){
        if (secretKey != null) {
            return secretKey.getEncoded();
        }

        return null;
    }
    private static SecretKey getKey(byte[] key){
        return new SecretKeySpec(key, 0, key.length, "ChaCha20");
    }
}

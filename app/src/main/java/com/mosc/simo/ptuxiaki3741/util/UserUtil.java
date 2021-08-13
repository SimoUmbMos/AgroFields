package com.mosc.simo.ptuxiaki3741.util;

import android.util.Log;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {
    public static List<User> getAllSenderUsers(
            List<UserRelationship> relationships,
            RoomDatabase db
    ){
        List<User> users = new ArrayList<>();
        User temp;
        for(UserRelationship relationship : relationships){
            temp = db.userDao().getUserById(relationship.getSenderID());
            if(temp != null)
                users.add(temp);
        }
        return users;
    }

    public static List<User> getAllReceiverUsers(
            List<UserRelationship> relationships,
            RoomDatabase db
    ){
        List<User> users = new ArrayList<>();
        User temp;
        for(UserRelationship relationship : relationships){
            temp = db.userDao().getUserById(relationship.getReceiverID());
            if(temp != null)
                users.add(temp);
        }
        return users;
    }
    public static String hashPassword(String password){
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
        }catch (NoSuchAlgorithmException e){
            Log.e("UserClass", "encryptPassword: ", e);
        }
        return password;
    }
}

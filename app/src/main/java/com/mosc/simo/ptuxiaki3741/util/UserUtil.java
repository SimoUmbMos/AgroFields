package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserRelationship;

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
}

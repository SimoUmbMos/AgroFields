package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.repositorys.interfaces.UserRepository;

public final class UserTestUtil {
    private UserTestUtil(){}

    public static User createMockUser(String username){
        return new User(username);
    }
}

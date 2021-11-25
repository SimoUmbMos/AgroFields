package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.entities.LandData;
import com.mosc.simo.ptuxiaki3741.models.entities.User;

import java.util.ArrayList;

public final class LandTestUtil {
    private LandTestUtil(){}

    public static Land createMockLand(String landName, User user){
        LandData data = new LandData(
                false,
                user.getId(),
                landName,
                new ArrayList<>(),
                new ArrayList<>()
        );
        return new Land(data);
    }
}

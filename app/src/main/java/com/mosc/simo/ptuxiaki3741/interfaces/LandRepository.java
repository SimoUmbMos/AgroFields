package com.mosc.simo.ptuxiaki3741.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.User;

import java.util.List;

public interface LandRepository {
    Land getLand(long lid);
    List<Land> searchLandsByUser(User user);
    Land saveLand(Land land);
    void deleteLand(Land land);
}

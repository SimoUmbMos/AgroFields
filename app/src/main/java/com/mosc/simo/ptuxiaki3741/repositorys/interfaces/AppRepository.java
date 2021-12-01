package com.mosc.simo.ptuxiaki3741.repositorys.interfaces;

import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.List;

public interface AppRepository {
    List<Land> getLands();
    List<LandZone> getLandZones();
    List<LandDataRecord> getLandRecords();
    List<Contact> getContacts();

    List<LandZone> getLandZonesByLID(long lid);
    List<Contact> userSearch(String search, int page);
    int searchUserMaxPage(String search);

    void saveLand(Land land);
    void saveZone(LandZone zone);
    void saveLandRecord(LandDataRecord landRecord);
    Contact saveContact(Contact contact);

    void deleteLand(Land land);
    void deleteZone(LandZone zone);
    void deleteZonesByLandID(long lid);
    void deleteContact(Contact contact);
}

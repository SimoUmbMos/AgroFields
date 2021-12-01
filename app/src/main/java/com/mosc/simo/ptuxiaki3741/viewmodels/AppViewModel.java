package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.LandDBAction;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.LandZone;
import com.mosc.simo.ptuxiaki3741.models.entities.Contact;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.AppRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppViewModel extends AndroidViewModel {
    public static final String TAG = "LandViewModel";

    private final AppRepositoryImpl appRepository;

    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<List<Land>> lands = new MutableLiveData<>();
    private final MutableLiveData<List<LandZone>> landZones = new MutableLiveData<>();
    private final MutableLiveData<List<LandDataRecord>> landsHistory = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        appRepository = new AppRepositoryImpl(db);
    }

    public LiveData<List<Contact>> getContacts(){
        return contacts;
    }
    public LiveData<List<Land>> getLands(){
        return lands;
    }
    public LiveData<List<LandZone>> getLandZones(){
        return landZones;
    }
    public LiveData<List<LandDataRecord>> getLandsHistory() {
        return landsHistory;
    }

    public void init(){
        AsyncTask.execute(this::populateLists);
    }

    private void populateLists() {
        populateContacts();
        populateLands();
        populateLandZones();
        populateLandsRecords();
    }
    private void populateContacts(){
        List<Contact> contactList;
        if(contacts.getValue() != null){
            contactList = contacts.getValue();
            contactList.clear();
            contactList.addAll(appRepository.getContacts());
        }else{
            contactList = new ArrayList<>(appRepository.getContacts());
        }
        contacts.postValue(contactList);
    }
    private void populateLands() {
        List<Land> landList;
        if(lands.getValue() != null){
            landList = lands.getValue();
            landList.clear();
            landList.addAll(appRepository.getLands());
        }else{
            landList = new ArrayList<>(appRepository.getLands());
        }
        lands.postValue(landList);
    }
    private void populateLandZones(){
        List<LandZone> zoneList;
        if(landZones.getValue() != null){
            zoneList = landZones.getValue();
            zoneList.clear();
            zoneList.addAll(appRepository.getLandZones());
        }else{
            zoneList = new ArrayList<>(appRepository.getLandZones());
        }
        landZones.postValue(zoneList);
    }
    private void populateLandsRecords() {
        List<LandDataRecord> landsHistoryList;
        if(landsHistory.getValue() != null){
            landsHistoryList = landsHistory.getValue();
            landsHistoryList.clear();
            landsHistoryList.addAll(appRepository.getLandRecords());
        }else{
            landsHistoryList = new ArrayList<>(appRepository.getLandRecords());
        }
        landsHistory.postValue(landsHistoryList);
    }

    public void saveLand(Land land){
        if(land != null){
            AsyncTask.execute(()->{
                LandDBAction action;
                if(land.getData().getId() != -1){
                    action = LandDBAction.UPDATE;
                }else{
                    action = LandDBAction.CREATE;
                }
                appRepository.saveLand(land);
                LandDataRecord landRecord = new LandDataRecord(
                        land.getData(),
                        action,
                        new Date()
                );
                appRepository.saveLandRecord(landRecord);
                populateLists();
            });
        }
    }
    public void restoreLand(Land land) {
        if(land != null){
            AsyncTask.execute(()->{
                if(land.getData().getId() != -1){
                    LandDBAction action = LandDBAction.RESTORE;
                    appRepository.saveLand(land);
                    LandDataRecord landRecord = new LandDataRecord(
                            land.getData(),
                            action,
                            new Date()
                    );
                    appRepository.saveLandRecord(landRecord);
                }

                populateLists();
            });
        }
    }
    public void removeLand(Land land) {
        if(land != null){
            AsyncTask.execute(()-> {
                LandDataRecord landRecord = new LandDataRecord(
                        land.getData(),
                        LandDBAction.DELETE,
                        new Date()
                );
                appRepository.deleteZonesByLandID(land.getData().getId());
                appRepository.deleteLand(land);
                appRepository.saveLandRecord(landRecord);
                populateLists();
            });
        }
    }
    public void removeLands(List<Land> lands) {
        if(lands != null){
            AsyncTask.execute(()-> {
                LandDataRecord landRecord;
                for(Land land:lands){
                    landRecord = new LandDataRecord(
                            land.getData(),
                            LandDBAction.DELETE,
                            new Date()
                    );
                    appRepository.deleteZonesByLandID(land.getData().getId());
                    appRepository.deleteLand(land);
                    appRepository.saveLandRecord(landRecord);
                }
                populateLists();
            });
        }
    }

    public void saveZone(LandZone zone) {
        if(zone != null){
            appRepository.saveZone(zone);
            populateLists();
        }
    }
    public void removeZone(LandZone zone) {
        if(zone != null){
            AsyncTask.execute(()-> {
                appRepository.deleteZone(zone);
                populateLists();
            });
        }
    }
    public void removeZones(List<LandZone> zones) {
        if(zones != null){
            AsyncTask.execute(()-> {
                for(LandZone zone:zones){
                    appRepository.deleteZone(zone);
                }
                populateLists();
            });
        }
    }
}

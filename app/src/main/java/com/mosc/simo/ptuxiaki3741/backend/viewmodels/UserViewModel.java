package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.database.AppDatabase;
import com.mosc.simo.ptuxiaki3741.backend.interfaces.LandRepository;
import com.mosc.simo.ptuxiaki3741.models.Land;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private static final String sharedPreferenceKey = "currUser";
    private static final long sharedPreferenceDefault = -1;
    private final MutableLiveData<User> currUser = new MutableLiveData<>();
    private final UserRepositoryImpl userRepository;
    private final LandRepository landRepository;
    private SharedPreferences sharedPref;

    public UserViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = MainActivity.getDb(application.getApplicationContext());
        userRepository = new UserRepositoryImpl(db);
        landRepository = new LandRepositoryImpl(db);
    }
    public void saveUser(User user){
        AsyncTask.execute(()->userRepository.saveUser(user));
    }
    public void saveUserAndLogin(User user){
        AsyncTask.execute(()->{
            User newUser = userRepository.saveUser(user);
            singIn(newUser.getId());
        });
    }
    public void removeUser(User user){
        AsyncTask.execute(()->{
            List<Land> lands = landRepository.searchLandsByUser(user);
            for(Land land:lands){
                landRepository.deleteLand(land);
            }
            userRepository.deleteUser(user);
        });
    }

    public LiveData<User> getCurrUser(){
        return currUser;
    }
    public void singIn(long uid) {
        User user = userRepository.searchUserByID(uid);
        if(user != null){
            putUidToMemory(uid);
        }
        currUser.postValue(user);
    }
    public void logout() {
        currUser.postValue(null);
    }

    private void clearUidFromMemory(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(sharedPreferenceKey);
        editor.apply();
    }
    private long getUidFromMemory() {
        return sharedPref.getLong(sharedPreferenceKey, sharedPreferenceDefault);
    }
    private void putUidToMemory(long id){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(sharedPreferenceKey, id);
        editor.apply();
    }

    public void init(SharedPreferences sharedPref){
        this.sharedPref = sharedPref;
        AsyncTask.execute(this::initCurrUser);
    }
    private void initCurrUser() {
        currUser.postValue(loadCurrUser());
    }
    private User loadCurrUser() {
        long uid = getUidFromMemory();
        User user = null;
        if(uid != sharedPreferenceDefault){
            user = userRepository.searchUserByID(uid);
            if(user == null){
                clearUidFromMemory();
            }
        }
        //TODO: TO REMOVE
        user = getMockUser();
        return user;
    }

    //TODO: TO REMOVE
    private User getMockUser() {
        return new User(1,"4200","makos");
    }
}

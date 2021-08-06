package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.database.roomserver.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;

public class UserViewModel extends AndroidViewModel {
    private static final String sharedPreferenceKey = "currUser";
    private static final long sharedPreferenceDefault = -1;

    private final MutableLiveData<User> currUser = new MutableLiveData<>();

    private final UserRepositoryImpl userRepository;
    private final LandRepositoryImpl landRepository;
    private SharedPreferences sharedPref;

    public UserViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        userRepository = new UserRepositoryImpl(db);
        landRepository = new LandRepositoryImpl(db);
    }
    public void init(SharedPreferences sharedPref){
        this.sharedPref = sharedPref;
        initCurrUser();
    }
    private void initCurrUser() {
        AsyncTask.execute(()->{
            User user = loadCurrUser();
            currUser.postValue(user);
        });
    }
    private User loadCurrUser() {
        long uid = getUidFromMemory();
        User user = userRepository.searchUserByID(uid);
        if(user == null){
            clearUidFromMemory();
        }
        return user;
    }

    public LiveData<User> getCurrUser(){
        return currUser;
    }

    private void clearUidFromMemory(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(sharedPreferenceKey);
        editor.apply();
    }
    private void putUidToMemory(long id){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(sharedPreferenceKey, id);
        editor.apply();
    }
    private long getUidFromMemory() {
        return sharedPref.getLong(sharedPreferenceKey, sharedPreferenceDefault);
    }

    public User saveNewUser(User user){
        return userRepository.saveNewUser(user);
    }
    public void editUser(User user){
        userRepository.editUser(user);
    }
    public void deleteUser(User user){
        landRepository.deleteLandsByUser(user);
        userRepository.deleteUser(user);
    }

    public User checkCredentials(String username, String password) {
        User user = userRepository.searchUserByUserName(username);
        if(user != null){
            if(user.getPassword().equals(password)){
                return user;
            }
        }
        return null;
    }
    public void singIn(User user) {
        if(user != null){
            putUidToMemory(user.getId());
        }else{
            clearUidFromMemory();
        }
        currUser.postValue(user);
    }
    public void logout() {
        singIn(null);
    }
}

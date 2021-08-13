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

import java.util.ArrayList;
import java.util.List;

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
        if(user != null){
            boolean isCurrUser = user.equals(currUser.getValue());
            userRepository.editUser(user);
            if(isCurrUser){
                this.currUser.postValue(user);
            }
        }
    }
    public void deleteUser(User user){
        if(user != null){
            boolean isCurrUser = user.equals(currUser.getValue());
            landRepository.deleteLandsByUser(user);
            userRepository.deleteUser(user);
            if(isCurrUser){
                logout();
            }
        }
    }

    public User checkCredentials(String username, String password) {
        return userRepository.searchUserByUserNameAndPassword(username, password);
    }
    public User checkUserNameCredentials(String username) {
        return userRepository.searchUserByUserName(username);
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

    public List<User> searchUser(String search){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.userSearch(currUser.getValue(), search);
        }
        return result;
    }

    public List<User> getFriends(){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.getUserFriendList(currUser.getValue());
        }
        return result;
    }
    public List<User> getFriendRequests(){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.getUserFriendRequestList(currUser.getValue());
        }
        return result;
    }
    public List<User> getBlocks(){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.getUserBlockList(currUser.getValue());
        }
        return result;
    }

    public boolean sendFriendRequest(User user){
        if(currUser.getValue() != null && user != null){
            return userRepository.sendFriendRequest(currUser.getValue(),user);
        }
        return false;
    }
    public boolean acceptRequest(User user){
        if(currUser.getValue() != null && user != null){
            return userRepository.acceptFriendRequest(currUser.getValue(),user);
        }
        return false;
    }
    public boolean declineRequest(User user){
        if(currUser.getValue() != null && user != null){
            return userRepository.declineFriendRequest(currUser.getValue(),user);
        }
        return false;
    }
    public boolean deleteFriend(User user){
        if(currUser.getValue() != null && user != null){
            return userRepository.deleteFriend(currUser.getValue(),user);
        }
        return false;
    }
    public void blockUser(User user){
        if(currUser.getValue() != null && user != null){
            userRepository.blockUser(currUser.getValue(),user);
        }
    }
}

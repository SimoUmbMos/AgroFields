package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

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
                User decryptedUser = EncryptUtil.decrypt(
                        userRepository.searchUserByID(user.getId())
                );
                this.currUser.postValue(decryptedUser);
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

    public User checkCredentials(User user) {
        return userRepository.searchUserByUserNameAndPassword(
                user.getUsername(),
                user.getPassword()
        );
    }
    public User checkUserNameCredentials(User user) {
        return userRepository.searchUserByUserName(
                user.getUsername()
        );
    }
    public void singIn(User user) {
        if(user != null){
            putUidToMemory(user.getId());
            User decryptedUser = EncryptUtil.decrypt(
                    userRepository.searchUserByID(user.getId())
            );
            currUser.postValue(decryptedUser);
        }else{
            logout();
        }
    }
    public void logout() {
        clearUidFromMemory();
        currUser.postValue(null);
    }

    public List<User> searchUser(String search){
        if(currUser.getValue() != null){
            return userRepository.userSearch(currUser.getValue(), search);
        }
        return new ArrayList<>();
    }

    public List<User> getFriends(){
        if(currUser.getValue() != null){
            return userRepository.getUserFriendList(currUser.getValue());
        }
        return new ArrayList<>();
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

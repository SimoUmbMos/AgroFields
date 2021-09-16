package com.mosc.simo.ptuxiaki3741.backend.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.backend.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.LandRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.backend.repositorys.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG ="UserViewModel";
    private static final String sharedPreferenceKey = "currUser";
    private static final long sharedPreferenceDefault = -1;

    private final MutableLiveData<User> currUser = new MutableLiveData<>();
    private final MutableLiveData<List<User>> friends = new MutableLiveData<>();
    private final MutableLiveData<List<User>> friendRequests = new MutableLiveData<>();
    private final MutableLiveData<List<User>> blocks = new MutableLiveData<>();

    private final UserRepositoryImpl userRepository;
    private final LandRepositoryImpl landRepository;
    private SharedPreferences sharedPref;

    public LiveData<User> getCurrUser(){
        return currUser;
    }
    public MutableLiveData<List<User>> getFriendList(){
        return friends;
    }
    public MutableLiveData<List<User>> getFriendRequestList(){
        return friendRequests;
    }
    public MutableLiveData<List<User>> getBlockList(){
        return blocks;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        userRepository = new UserRepositoryImpl(db);
        landRepository = new LandRepositoryImpl(db);
    }
    public void init(SharedPreferences sharedPref){
        this.sharedPref = sharedPref;
        AsyncTask.execute(this::initCurrUser);
    }
    private void initCurrUser() {
        User user = loadCurrUser();
        currUser.postValue(user);
        populateCurrUserRelativeLists(user);
    }
    private void populateCurrUserRelativeLists(User user) {
        if(user != null){
            friends.postValue(getFriends(user));
            friendRequests.postValue(getFriendRequests(user));
            blocks.postValue(getBlocks(user));
        }else{
            friends.postValue(new ArrayList<>());
            friendRequests.postValue(new ArrayList<>());
            blocks.postValue(new ArrayList<>());
        }
    }
    private User loadCurrUser() {
        long uid = getUidFromMemory();
        User user = userRepository.searchUserByID(uid);
        if(user == null){
            clearUidFromMemory();
        }
        return user;
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
            userRepository.deleteAllRelationships(user);
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
            populateCurrUserRelativeLists(decryptedUser);
        }else{
            logout();
        }
    }
    public void logout() {
        clearUidFromMemory();
        currUser.postValue(null);
        populateCurrUserRelativeLists(null);
    }

    public List<User> searchUser(String search){
        if(currUser.getValue() != null){
            return userRepository.userSearch(currUser.getValue(), search);
        }
        return null;
    }

    private List<User> getFriends(User user){
        if(user != null){
            Log.d(TAG, "getFriends: USER NOT NULL");
            return userRepository.getUserFriendList(user);
        }
        Log.d(TAG, "getFriends: USER NULL");
        return new ArrayList<>();
    }
    private List<User> getFriendRequests(User user){
        if(user != null){
            Log.d(TAG, "getFriendRequests: USER NOT NULL");
            return userRepository.getUserFriendRequestList(user);
        }
        Log.d(TAG, "getFriendRequests: USER NULL");
        return new ArrayList<>();
    }
    private List<User> getBlocks(User user){
        if(user != null){
            Log.d(TAG, "getBlocks: USER NOT NULL");
            return userRepository.getUserBlockList(user);
        }
        Log.d(TAG, "getBlocks: USER NULL");
        return new ArrayList<>();
    }

    public void refreshLists(){
        populateCurrUserRelativeLists(currUser.getValue());
    }

    public UserFriendRequestStatus sendFriendRequest(User user){
        if(currUser.getValue() != null && user != null){
            switch (userRepository.sendFriendRequest(currUser.getValue(),user)){
                case ACCEPTED:
                    populateCurrUserRelativeLists(currUser.getValue());
                    return UserFriendRequestStatus.ACCEPTED;
                case REQUESTED:
                    return UserFriendRequestStatus.REQUESTED;
            }
        }
        return UserFriendRequestStatus.REQUEST_FAILED;
    }
    public boolean acceptRequest(User user){
        if(currUser.getValue() != null && user != null){
            if(userRepository.acceptFriendRequest(currUser.getValue(),user)){
                populateCurrUserRelativeLists(currUser.getValue());
                return true;
            }
        }
        return false;
    }
    public boolean declineRequest(User user){
        if(currUser.getValue() != null && user != null){
            if(userRepository.declineFriendRequest(currUser.getValue(),user)){
                populateCurrUserRelativeLists(currUser.getValue());
                return true;
            }
        }
        return false;
    }
    public boolean deleteFriend(User user){
        if(currUser.getValue() != null && user != null){
            if(userRepository.deleteFriend(currUser.getValue(),user)){
                populateCurrUserRelativeLists(currUser.getValue());
                return true;
            }
        }
        return false;
    }
    public void blockUser(User user){
        if(currUser.getValue() != null && user != null){
            userRepository.blockUser(currUser.getValue(),user);
            populateCurrUserRelativeLists(currUser.getValue());
        }
    }
}

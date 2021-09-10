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
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
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
        AsyncTask.execute(()->{
            initCurrUser();
            if(currUser.getValue() != null){
                populateCurrUserRelativeLists();
            }else{
                clearCurrUserRelativeLists();
            }
        });
    }
    private void initCurrUser() {
        User user = loadCurrUser();
        currUser.postValue(user);
    }
    private void populateCurrUserRelativeLists() {
        friends.postValue(getFriends());
        friendRequests.postValue(getFriendRequests());
        blocks.postValue(getBlocks());
    }
    private void clearCurrUserRelativeLists(){
        friends.postValue(new ArrayList<>());
        friendRequests.postValue(new ArrayList<>());
        blocks.postValue(new ArrayList<>());
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
            populateCurrUserRelativeLists();
        }else{
            logout();
        }
    }
    public void logout() {
        clearUidFromMemory();
        currUser.postValue(null);
        clearCurrUserRelativeLists();
    }

    public List<User> searchUser(String search, int page){
        if(currUser.getValue() != null){
            return userRepository.userSearch(currUser.getValue(), search, page);
        }
        return null;
    }
    public int getSearchPageCount(String search){
        if(currUser.getValue() != null){
            return userRepository.searchPageCount(currUser.getValue(), search);
        }
        return -1;
    }

    private List<User> getFriends(){
        if(currUser.getValue() != null){
            return userRepository.getUserFriendList(currUser.getValue());
        }
        return new ArrayList<>();
    }
    private List<User> getFriendRequests(){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.getUserFriendRequestList(currUser.getValue());
        }
        return result;
    }
    private List<User> getBlocks(){
        List<User> result = new ArrayList<>();
        if(currUser.getValue() != null){
            result = userRepository.getUserBlockList(currUser.getValue());
        }
        return result;
    }

    public UserFriendRequestStatus sendFriendRequest(User user){
        if(currUser.getValue() != null && user != null){
            return userRepository.sendFriendRequest(currUser.getValue(),user);
        }
        return UserFriendRequestStatus.REQUEST_FAILED;
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

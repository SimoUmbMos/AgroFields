package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.database.RoomDatabase;
import com.mosc.simo.ptuxiaki3741.enums.UserFriendRequestStatus;
import com.mosc.simo.ptuxiaki3741.models.entities.LandMemo;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.models.entities.UserMemo;
import com.mosc.simo.ptuxiaki3741.repositorys.implement.UserRepositoryImpl;
import com.mosc.simo.ptuxiaki3741.util.EncryptUtil;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    public static final String TAG ="UserViewModel";

    private final UserRepositoryImpl userRepository;
    private SharedPreferences sharedPref;

    private final MutableLiveData<User> currUser = new MutableLiveData<>();
    private final MutableLiveData<List<User>> contacts = new MutableLiveData<>();
    private final MutableLiveData<List<User>> inboxRequests = new MutableLiveData<>();
    private final MutableLiveData<List<User>> outboxRequests = new MutableLiveData<>();
    private final MutableLiveData<List<User>> blocks = new MutableLiveData<>();
    private final MutableLiveData<List<UserMemo>> contactsMemos = new MutableLiveData<>();
    private final MutableLiveData<List<LandMemo>> landsMemos = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        RoomDatabase db = MainActivity.getRoomDb(application.getApplicationContext());
        userRepository = new UserRepositoryImpl(db);
    }

    public void init(SharedPreferences sharedPref){
        this.sharedPref = sharedPref;
        AsyncTask.execute(this::initFromMemory);
    }

    public LiveData<User> getCurrUser(){
        return currUser;
    }
    public MutableLiveData<List<User>> getFriendList(){
        return contacts;
    }
    public MutableLiveData<List<User>> getReceivedRequestList(){
        return inboxRequests;
    }
    public MutableLiveData<List<User>> getSendedRequestList(){
        return outboxRequests;
    }
    public MutableLiveData<List<User>> getBlockList(){
        return blocks;
    }
    //todo: (idea) implement getContactsMemoList
    public MutableLiveData<List<UserMemo>> getContactsMemoList(){
        return contactsMemos;
    }
    //todo: (idea) implement getLandsMemoList
    public MutableLiveData<List<LandMemo>> getLandsMemoList(){
        return landsMemos;
    }

    private void initFromMemory() {
        long uid = loadCurrUser();
        initCurrUser(uid);
    }
    private void initCurrUser(long id) {
        User user = null;
        if(id != AppValues.sharedPreferenceDefaultUserViewModel){
            user = userRepository.getUserByID(id);
        }
        currUser.postValue(user);
        populateCurrUserRelativeLists(user);
    }
    //todo: (idea) split function populateCurrUserRelativeLists
    private void populateCurrUserRelativeLists(User user) {
        if(user != null){
            contacts.postValue(getFriends(user));
            inboxRequests.postValue(getInboxRequests(user));
            outboxRequests.postValue(getOutboxRequests(user));
            blocks.postValue(getBlocks(user));
            contactsMemos.postValue(getContactsMemos(user));
            landsMemos.postValue(getLandsMemos(user));
        }else{
            contacts.postValue(new ArrayList<>());
            inboxRequests.postValue(new ArrayList<>());
            outboxRequests.postValue(new ArrayList<>());
            blocks.postValue(new ArrayList<>());
            contactsMemos.postValue(new ArrayList<>());
            landsMemos.postValue(new ArrayList<>());
        }
    }
    private long loadCurrUser() {
        long uid = getUidFromMemory();
        if(uid == AppValues.sharedPreferenceDefaultUserViewModel){
            clearUidFromMemory();
        }
        return uid;
    }
    private User loadUserData(User user) {
        User decryptedUser = user;
        try{
            decryptedUser = EncryptUtil.decrypt(
                    userRepository.getUserByID(user.getId())
            );
        }catch (Exception e){
            Log.e(TAG, "loadUserData: ", e);
        }
        return decryptedUser;
    }

    private void clearUidFromMemory(){
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(AppValues.sharedPreferenceKeyUserViewModel);
            editor.apply();
        }
    }
    private void putUidToMemory(long id){
        if(sharedPref != null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(AppValues.sharedPreferenceKeyUserViewModel, id);
            editor.apply();
        }
    }
    private long getUidFromMemory() {
        if(sharedPref != null){
            return sharedPref.getLong(
                    AppValues.sharedPreferenceKeyUserViewModel,
                    AppValues.sharedPreferenceDefaultUserViewModel
            );
        }
        return AppValues.sharedPreferenceDefaultUserViewModel;
    }

    private List<User> getFriends(User user){
        if(user != null){
            return userRepository.getUserFriendList(user);
        }
        return new ArrayList<>();
    }
    private List<User> getInboxRequests(User user){
        if(user != null){
            return userRepository.getUserInboxRequestList(user);
        }
        return new ArrayList<>();
    }
    private List<User> getOutboxRequests(User user){
        if(user != null){
            return userRepository.getUserOutboxRequestList(user);
        }
        return new ArrayList<>();
    }
    private List<User> getBlocks(User user){
        if(user != null){
            return userRepository.getUserBlockList(user);
        }
        return new ArrayList<>();
    }
    private List<UserMemo> getContactsMemos(User user){
        if(user != null){
            return userRepository.getUserFriendsMemosList(user);
        }
        return new ArrayList<>();
    }
    private List<LandMemo> getLandsMemos(User user){
        if(user != null){
            return userRepository.getUserLandsMemosList(user);
        }
        return new ArrayList<>();
    }

    public void refreshVM(){
        if(currUser.getValue() != null){
            initCurrUser(currUser.getValue().getId());
        }else{
            initCurrUser(AppValues.sharedPreferenceDefaultUserViewModel);
        }
    }

    public User getUserByID(long uid){
        return userRepository.getUserByID(uid);
    }
    public User saveNewUser(User user){
        return userRepository.saveNewUser(user);
    }
    public void editUser(User user){
        if(user != null){
            boolean isCurrUser = user.equals(currUser.getValue());
            userRepository.editUser(user);
            if(isCurrUser){
                User decryptedUser = currUser.getValue();
                try{
                    decryptedUser = EncryptUtil.decrypt(
                            getUserByID(user.getId())
                    );
                }catch (Exception e){
                    Log.e(TAG, "editUser: ", e);
                }
                this.currUser.postValue(decryptedUser);
            }
        }
    }
    public void deleteUser(User user){
        if(user != null){
            boolean isCurrUser = user.equals(currUser.getValue());
            userRepository.deleteUser(user);
            if(isCurrUser){
                logout();
            }
        }
    }

    public User checkCredentials(User user) {
        return userRepository.getUserByUserNameAndPassword(
                user.getUsername(),
                user.getPassword()
        );
    }
    public User checkUserNameCredentials(User user) {
        return userRepository.getUserByUserName(
                user.getUsername()
        );
    }
    public void singIn(User user) {
        if(user != null){
            putUidToMemory(user.getId());
            User decryptedUser = loadUserData(user);
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

    public UserFriendRequestStatus sendRequest(User user){
        UserFriendRequestStatus r = UserFriendRequestStatus.REQUEST_FAILED;
        if(currUser.getValue() != null && user != null){
            r = userRepository.sendFriendRequest(currUser.getValue(),user);
            if(r != UserFriendRequestStatus.REQUEST_FAILED){
                populateCurrUserRelativeLists(currUser.getValue());
            }
        }
        return r;
    }
    public boolean deleteRequest(User user){
        if(currUser.getValue() != null && user != null){
            if (userRepository.deleteFriendRequest(currUser.getValue(),user)) {
                populateCurrUserRelativeLists(currUser.getValue());
                return true;
            }
        }
        return false;
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
    public void removeBlock(User user){
        if(currUser.getValue() != null && user != null){
            userRepository.removeBlock(currUser.getValue(),user);
            populateCurrUserRelativeLists(currUser.getValue());
        }
    }
}

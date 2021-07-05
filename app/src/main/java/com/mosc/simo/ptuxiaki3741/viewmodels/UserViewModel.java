package com.mosc.simo.ptuxiaki3741.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosc.simo.ptuxiaki3741.MainActivity;
import com.mosc.simo.ptuxiaki3741.models.User;
import com.mosc.simo.ptuxiaki3741.repositorys.UserRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<User> currUser = new MutableLiveData<>();
    private final UserRepositoryImpl userRepository;
    private SharedPreferences sharedPref;
    private boolean isInit = false;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepositoryImpl(MainActivity.getDb(application));
    }

    public boolean isInit() {
        return isInit;
    }

    public LiveData<List<User>> getUsers(){
        return users;
    }
    public void addUser(User user){
        List<User> userList = users.getValue();
        if(userList != null){
            add(user, userList);
        }
    }
    public void editUser(User user){
        List<User> userList = users.getValue();
        if(userList != null){
            edit(user, userList);
        }
    }
    public void removeUser(User user){
        List<User> userList = users.getValue();
        if(userList != null){
            remove(user, userList);
        }
    }

    public LiveData<User> getCurrUser(){
        return currUser;
    }
    public void singIn(long userID) {
        if(users.getValue() != null){
            User user = null;
            for(User tempUser:users.getValue()){
                if(tempUser.getId() == userID){
                    user = tempUser;
                    break;
                }
            }
            currUser.postValue(user);
            storeCurrUserToMemory();
        }
    }
    public void logout() {
        currUser.postValue(null);
    }

    private void storeCurrUserToMemory(){
        SharedPreferences.Editor editor = sharedPref.edit();
        long userID = -1;
        if(currUser.getValue() != null){
            userID = currUser.getValue().getId();
        }
        editor.putLong("currUser", userID);
        editor.apply();
    }
    private long getCurrUserIDFromMemory() {
        return sharedPref.getLong("currUser", -1);
    }

    private void add(User user, List<User> userList) {
        userList.add(user);
        users.postValue(userList);
    }
    private void edit(User user, List<User> userList) {
        int index = -1;
        for(User tempUser : userList){
            if(tempUser.getId() == user.getId()){
                index = userList.indexOf(tempUser);
            }
        }
        if(index != -1){
            userList.set(index,user);
            users.postValue(userList);
        }
    }
    private void remove(User user, List<User> userList) {
        if(userList.contains(user)){
            userList.remove(user);
            users.postValue(userList);
        }else{
            int index = -1;
            for(User tempUser : userList){
                if(tempUser.getId() == user.getId()){
                    index = userList.indexOf(tempUser);
                }
            }
            if(index != -1){
                userList.remove(index);
                users.postValue(userList);
            }
        }
    }

    public void init(SharedPreferences sharedPref){
        isInit = true;
        this.sharedPref = sharedPref;
        initUsers();
        initCurrUser();
    }
    private void initUsers() {
        List<User> userList = users.getValue();
        if(userList == null){
            userList = new ArrayList<>();
        }
        loadUsers(userList);
        users.postValue(userList);
    }
    private void initCurrUser() {
        User user = loadCurrUser();
        currUser.postValue(user);
    }
    private void loadUsers(List<User> userList) {
        //TODO: load users from db
        userList.add(new User("4200","makos"));
    }
    private User loadCurrUser() {
        /*long uid = getCurrUserIDFromMemory();
        if(uid == -1){
            return null;
        }else{
            //TODO: load currUser from db
        }*/
        return new User(1,"4200","makos");
    }
}

package com.mosc.simo.ptuxiaki3741.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.mosc.simo.ptuxiaki3741.backend.enums.UserDBAction;

import java.util.Date;

@Entity(tableName = "UserRelationships",primaryKeys = {"SenderID","ReceiverID"})
public class UserRelationship {
    @ColumnInfo(name = "SenderID")
    private long senderID;
    @ColumnInfo(name = "ReceiverID")
    private long receiverID;
    @ColumnInfo(name = "Type")
    private UserDBAction Type;
    @ColumnInfo(name = "Date")
    private Date Date;

    public UserRelationship(long senderID, long receiverID, UserDBAction Type, Date Date){
        setSenderID(senderID);
        setReceiverID(receiverID);
        setType(Type);
        setDate(Date);
    }

    public void setSenderID(long senderID) {
        this.senderID = senderID;
    }
    public void setReceiverID(long receiverID) {
        this.receiverID = receiverID;
    }
    public void setType(UserDBAction type) {
        Type = type;
    }
    public void setDate(Date date) {
        Date = date;
    }

    public long getSenderID() {
        return senderID;
    }
    public long getReceiverID() {
        return receiverID;
    }
    public UserDBAction getType() {
        return Type;
    }
    public Date getDate() {
        return Date;
    }
}

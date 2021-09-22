package com.mosc.simo.ptuxiaki3741.backend.database;

public final class RoomValues {
    public static final int DATABASE_VERSION = 22;
    public static final class LandDaoValues{
        public static final String GetLands
                = "SELECT * FROM LandData";
        public static final String GetLandData
                = "SELECT * FROM LandData WHERE id = :lid";
        public static final String GetLandByCreatorId
                = "SELECT * FROM LandData WHERE CreatorID = :uid";
        public static final String GetUserSharedLands
                = "SELECT l.* FROM LandData l INNER JOIN SharedLands s ON l.id = s.LandID WHERE s.UserID = :uid";
        public static final String GetSharedLandsToUser
                = "SELECT l.* FROM LandData l INNER JOIN SharedLands s ON l.id = s.LandID WHERE l.CreatorID = :ownerID AND s.UserID = :sharedId";
        public static final String GetSharedLandsToOtherUsers
                = "SELECT l.* FROM LandData l INNER JOIN SharedLands s ON l.id = s.LandID WHERE l.CreatorID = :ownerID";
        public static final String DeleteByUID
                = "DELETE FROM LandData WHERE CreatorID = :uid";
    }
    public static final class LandHistoryDaoValues{
        public static final String GetLandRecordByLandID
                = "SELECT * FROM LandDataRecord Where LandID = :lid ORDER BY Date, LandTitle";
        public static final String GetLandRecordsByUserId
                = "SELECT * FROM LandDataRecord Where CreatorID = :uid ORDER BY LandID, Date, LandTitle";
    }
    public static final class SharedLandDaoValues{
        public static final String GetSharedLandsByUidAndLid
                = "SELECT * FROM SharedLands WHERE LandID = :lid AND UserID = :uid";
        public static final String DeleteByUserID
                = "DELETE FROM SharedLands WHERE UserID = :uid";
        public static final String DeleteByLandID
                = "DELETE FROM SharedLands WHERE LandID = :lid";
    }
    public static final class UserDaoValues{
        public static final String SearchUserByUserName
                = "SELECT * FROM Users WHERE Username GLOB  '*' || :search || '*' AND id != :searcherID";
        public static final String GetUsersByReceiverIDAndType
                = "SELECT u.* FROM Users u INNER JOIN UserRelationships ur ON u.id = ur.SenderID WHERE ur.ReceiverID = :receiverID AND ur.Type = :type";
        public static final String GetUsersBySenderIDAndType
                = "SELECT u.* FROM Users u INNER JOIN UserRelationships ur ON u.id = ur.ReceiverID WHERE ur.SenderID = :receiverID and ur.Type = :type";
        public static final String GetUserById
                = "SELECT * FROM Users WHERE id = :id";
        public static final String GetUserByUserName
                = "SELECT * FROM Users WHERE Username = :username";
        public static final String GetUserByUserNameAndPassword
                = "SELECT * FROM Users WHERE Username = :username AND Password = :password";
    }
    public static final class UserRelationshipDaoValues{
        public static final String GetByIDs
                = "SELECT * FROM UserRelationships WHERE (SenderID = :id1 AND ReceiverID = :id2) OR (SenderID = :id2 AND ReceiverID = :id1)";
        public static final String GetByIDAndType
                = "SELECT * FROM UserRelationships WHERE (ReceiverID = :id OR SenderID = :id) AND Type = :type";
        public static final String DeleteByUserID
                = "DELETE FROM UserRelationships WHERE SenderID = :uid OR ReceiverID = :uid";
        public static final String DeleteByIDsAndType
                = "DELETE FROM UserRelationships WHERE ReceiverID = :rid AND SenderID = :sid AND Type = :type";
    }
}

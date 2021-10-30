package com.mosc.simo.ptuxiaki3741.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosc.simo.ptuxiaki3741.models.entities.LandDataRecord;

import java.util.List;

@Dao
public interface LandHistoryDao {
    @Query("SELECT * FROM LandDataRecord " +
            "WHERE LandID = :lid " +
            "ORDER BY Date, LandTitle")
    List<LandDataRecord> getLandRecordByLandID(long lid);

    @Query("SELECT * FROM LandDataRecord " +
            "WHERE CreatorID = :uid " +
            "ORDER BY LandID, Date, LandTitle")
    List<LandDataRecord> getLandRecordsByUserId(long uid);

    @Query("SELECT r.* " +
            "FROM LandDataRecord r LEFT JOIN UserLandPermissions p ON p.LandID = r.LandID " +
            "WHERE r.CreatorID = :uid OR (p.UserID = :uid AND p.AdminPermission = :admin) " +
            "ORDER BY r.LandID, r.Date, r.LandTitle")
    List<LandDataRecord> getLandRecordsByUserIdAndPermission(long uid, boolean admin);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LandDataRecord landRecord);

    @Delete
    void delete(LandDataRecord landRecord);
}

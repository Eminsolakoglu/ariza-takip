package com.arizatakip.system.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.arizatakip.system.model.ArizaLog;

import java.util.List;

@Dao
public interface ArizaLogDao {
    @Insert
    void insert(ArizaLog log);

    @Query("SELECT * FROM ariza_logs WHERE arizaId = :arizaId ORDER BY tarih ASC")
    List<ArizaLog> getByArizaId(int arizaId);

    @Query("DELETE FROM ariza_logs WHERE arizaId = :arizaId")
    void deleteByArizaId(int arizaId);

    @Query("SELECT * FROM ariza_logs WHERE tarih >= :since ORDER BY tarih ASC")
    List<ArizaLog> getLogsSince(long since);
}

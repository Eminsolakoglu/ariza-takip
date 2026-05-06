package com.arizatakip.system.data;

import android.content.Context;

import com.arizatakip.system.model.ArizaLog;

import java.util.List;

public class ArizaLogRepository {

    private static AppDatabase db;

    public static void init(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public static void addLog(int arizaId, String kullanici, String detay) {
        ArizaLog log = new ArizaLog(arizaId, System.currentTimeMillis(), kullanici, detay);
        db.arizaLogDao().insert(log);
    }

    public static List<ArizaLog> getByArizaId(int arizaId) {
        return db.arizaLogDao().getByArizaId(arizaId);
    }

    public static void deleteByArizaId(int arizaId) {
        db.arizaLogDao().deleteByArizaId(arizaId);
    }
}

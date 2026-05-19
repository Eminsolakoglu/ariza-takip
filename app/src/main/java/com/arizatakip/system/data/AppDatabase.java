package com.arizatakip.system.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.model.ArizaLog;
import com.arizatakip.system.model.User;

@Database(entities = {Ariza.class, User.class, ArizaLog.class}, version = 11)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ArizaDao arizaDao();
    public abstract UserDao userDao();
    public abstract ArizaLogDao arizaLogDao();

    // 7→8: ariza_logs tablosu eklendi
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `ariza_logs` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`arizaId` INTEGER NOT NULL, " +
                "`tarih` INTEGER NOT NULL, " +
                "`kullanici` TEXT, " +
                "`detay` TEXT)"
            );
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ariza_db"
                    )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

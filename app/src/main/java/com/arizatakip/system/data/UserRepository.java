package com.arizatakip.system.data;

import android.content.Context;

import com.arizatakip.system.model.User;

import java.util.List;

public class UserRepository {

    private static AppDatabase db;

    public static void init(Context context) {
        db = AppDatabase.getInstance(context);
        prepopulateUsers();
    }

    // Uygulama ilk açıldığında tablo boşsa örnek kullanıcıları otomatik ekler
    private static void prepopulateUsers() {
        if (db.userDao().getUserCount() == 0) {
            db.userDao().insert(new User("admin", "1234", "admin"));
            db.userDao().insert(new User("tech", "1234", "tech"));
            db.userDao().insert(new User("tech2", "1234", "tech"));
            db.userDao().insert(new User("client", "1234", "client"));
            db.userDao().insert(new User("client2", "1234", "client"));
        }
    }

    // DB'den giriş kontrolü yapar
    public static User login(String username, String password) {
        return db.userDao().login(username, password);
    }
    public static List<String> getAllTechUsernames() {
        return db.userDao().getAllTechUsernames();
    }
}
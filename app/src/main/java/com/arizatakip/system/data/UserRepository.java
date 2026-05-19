package com.arizatakip.system.data;

import android.content.Context;

import com.arizatakip.system.model.User;
import com.arizatakip.system.utils.PasswordUtils;

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
            db.userDao().insert(new User("admin", PasswordUtils.hash("1234"), "admin"));
            db.userDao().insert(new User("tech", PasswordUtils.hash("1234"), "tech"));
            db.userDao().insert(new User("tech2", PasswordUtils.hash("1234"), "tech"));
            db.userDao().insert(new User("client", PasswordUtils.hash("1234"), "client"));
            db.userDao().insert(new User("client2", PasswordUtils.hash("1234"), "client"));
        }
    }

    public static User login(String username, String password) {
        User user = db.userDao().findByUsername(username);
        if (user != null && PasswordUtils.verify(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    public static List<String> getAllTechUsernames() {
        return db.userDao().getAllTechUsernames();
    }
}
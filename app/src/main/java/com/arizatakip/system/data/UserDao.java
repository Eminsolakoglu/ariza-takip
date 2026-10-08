package com.arizatakip.system.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.arizatakip.system.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    // Veritabanında hiç kullanıcı var mı diye kontrol etmek için
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    @Query("SELECT username FROM users WHERE role = 'tech'")
    List<String> getAllTechUsernames();
}
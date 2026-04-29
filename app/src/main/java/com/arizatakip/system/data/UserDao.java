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

    // Girilen kullanıcı adı ve şifreyle eşleşen bir kayıt var mı diye veritabanına sorar
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    // Veritabanında hiç kullanıcı var mı diye kontrol etmek için
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    @Query("SELECT username FROM users WHERE role = 'tech'")
    List<String> getAllTechUsernames();
}
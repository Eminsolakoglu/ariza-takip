package com.arizatakip.system;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.UserRepository;
import com.arizatakip.system.model.User;
import com.arizatakip.system.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Kullanıcı adı ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            // DİNAMİK VERİTABANI KONTROLÜ
            User loggedInUser = UserRepository.login(username, password);

            if (loggedInUser != null) {
                sessionManager.createSession(loggedInUser.getUsername(), loggedInUser.getRole());

                Toast.makeText(this, "Hoş geldin, " + loggedInUser.getUsername(), Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                // Veritabanında böyle bir kullanıcı-şifre eşleşmesi yok
                Toast.makeText(this, "Hatalı kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
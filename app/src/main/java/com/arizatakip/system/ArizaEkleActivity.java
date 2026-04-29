package com.arizatakip.system;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.utils.SessionManager;

public class ArizaEkleActivity extends AppCompatActivity {

    EditText etBaslik, etAciklama;
    Button btnKaydet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ariza_ekle);

        etBaslik = findViewById(R.id.etBaslik);
        etAciklama = findViewById(R.id.etAciklama);
        btnKaydet = findViewById(R.id.btnKaydet);
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            finish(); // Ekranı kapatır ve bir önceki sayfaya döner
        });
        btnKaydet.setOnClickListener(v -> {

            String baslik = etBaslik.getText().toString();
            String aciklama = etAciklama.getText().toString();

            if (baslik.isEmpty() || aciklama.isEmpty()) {
                Toast.makeText(this, "Alanların boş bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            // SessionManager'dan kullanıcıyı alıyoruz
            SessionManager session = new SessionManager(this);
            String currentUser = session.getUsername();

            // Kaydederken gönderiyoruz
            ArizaRepository.addAriza(baslik, aciklama, currentUser);

            Toast.makeText(this, "Arıza kaydedildi", Toast.LENGTH_SHORT).show();

            finish();
        });
// ...
    }
}
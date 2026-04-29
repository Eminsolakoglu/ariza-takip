package com.arizatakip.system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.data.UserRepository;
import com.arizatakip.system.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArizaDetayActivity extends AppCompatActivity {

    TextView tvBaslik, tvAciklama, tvDurum, tvTarih, tvAtanan, tvOlusturan;
    Button btnBack, btnDurum, btnTeknikerAta, btnSil;
    SessionManager sessionManager;

    int arizaId;
    String currentDurum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ariza_detay);

        sessionManager = new SessionManager(this);
        String role = sessionManager.getRole();

        tvBaslik = findViewById(R.id.tvBaslik);
        tvAciklama = findViewById(R.id.tvAciklama);
        tvDurum = findViewById(R.id.tvDurum);
        tvTarih = findViewById(R.id.tvTarih);
        tvAtanan = findViewById(R.id.tvAtanan);
        tvOlusturan = findViewById(R.id.tvOlusturan);

        btnBack = findViewById(R.id.btnBack);
        btnDurum = findViewById(R.id.btnDurum);
        btnTeknikerAta = findViewById(R.id.btnTeknikerAta);
        btnSil = findViewById(R.id.btnSil);
        Intent intent = getIntent();
        arizaId = intent.getIntExtra("id", -1);
        currentDurum = intent.getStringExtra("durum");

        tvBaslik.setText(intent.getStringExtra("baslik"));
        tvAciklama.setText(intent.getStringExtra("aciklama"));
        String rawDurum = intent.getStringExtra("durum");
        String ilkGosterilecekMetin = rawDurum;
        if ("OPEN".equals(rawDurum)) ilkGosterilecekMetin = "Beklemede";
        else if ("IN_PROGRESS".equals(rawDurum)) ilkGosterilecekMetin = "İşlemde";
        else if ("CLOSED".equals(rawDurum)) ilkGosterilecekMetin = "Çözüldü";

        tvDurum.setText("Durum: " + ilkGosterilecekMetin);
        tvAtanan.setText("Atanan: " + intent.getStringExtra("atanan"));
        tvOlusturan.setText("Açan: " + intent.getStringExtra("olusturan"));

        try {
            long timeInMillis = Long.parseLong(intent.getStringExtra("tarih"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("tr", "TR"));
            tvTarih.setText("Tarih: " + sdf.format(new Date(timeInMillis)));
        } catch (Exception e) {
            tvTarih.setText("Tarih: " + intent.getStringExtra("tarih"));
        }

        // YETKİLENDİRME
        if (role.equals("client")) {
            btnDurum.setVisibility(View.GONE);
            btnTeknikerAta.setVisibility(View.GONE);
            btnSil.setVisibility(View.GONE);
        } else if (role.equals("tech")) {
            btnDurum.setVisibility(View.VISIBLE);
            btnTeknikerAta.setVisibility(View.GONE);
            btnSil.setVisibility(View.GONE);
        } else if (role.equals("admin")) {
            btnDurum.setVisibility(View.VISIBLE);
            btnTeknikerAta.setVisibility(View.VISIBLE);
            btnSil.setVisibility(View.VISIBLE);
        }

        btnBack.setOnClickListener(v -> finish());

        btnDurum.setOnClickListener(v -> {
            // Veritabanında tutulan asıl değerler
            String[] dbDurumlar = {"OPEN", "IN_PROGRESS", "CLOSED"};
            // Ekranda kullanıcıya gösterilecek Türkçe seçenekler
            String[] gosterilenDurumlar = {"Beklemede", "İşlemde", "Çözüldü"};
            new AlertDialog.Builder(this)
                    .setTitle("Durum Güncelle")
                    .setItems(gosterilenDurumlar, (dialog, which) -> {
                        String secilenDurum = dbDurumlar[which];

                        ArizaRepository.updateDurum(arizaId, secilenDurum);
                        currentDurum = secilenDurum;
                        String gosterilecekMetin = currentDurum;
                        if (currentDurum.equals("OPEN")) gosterilecekMetin = "Beklemede";
                        else if (currentDurum.equals("IN_PROGRESS")) gosterilecekMetin = "İşlemde";
                        else if (currentDurum.equals("CLOSED")) gosterilecekMetin = "Çözüldü";

                        tvDurum.setText("Durum: " + gosterilecekMetin);
                    })
                    .show();
        });

        btnTeknikerAta.setOnClickListener(v -> {
            List<String> techList = UserRepository.getAllTechUsernames();

            if (techList == null || techList.isEmpty()) {
                Toast.makeText(this, "Sistemde teknik personel bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Listeyi Dialog'un okuyabileceği Diziye (Array) çeviriyoruz
            String[] techs = techList.toArray(new String[0]);

            new AlertDialog.Builder(this)
                    .setTitle("Tekniker Seçin")
                    .setItems(techs, (dialog, which) -> {
                        String secilenTech = techs[which];

                        ArizaRepository.updateAtananKisi(arizaId, secilenTech);
                        tvAtanan.setText("Atanan: " + secilenTech);
                        Toast.makeText(this, "Görev " + secilenTech + " adlı personele atandı.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
        btnSil.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Dikkat!")
                    .setMessage("Bu arıza kaydını kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        // Veritabanından sil
                        ArizaRepository.deleteById(arizaId);

                        Toast.makeText(this, "Arıza başarıyla silindi.", Toast.LENGTH_SHORT).show();

                        // Ekranı kapat ve listeye dön
                        finish();
                    })
                    .setNegativeButton("İptal", null) // İptale basarsa hiçbir şey yapma
                    .show();
        });
    }
}
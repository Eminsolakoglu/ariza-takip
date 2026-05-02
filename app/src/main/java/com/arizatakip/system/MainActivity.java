package com.arizatakip.system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arizatakip.system.adapter.ArizaAdapter;
import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SessionManager sessionManager;
    ArizaAdapter adapter;
    MaterialButton btnEkle;
    TextView tvListeBaslik, tvFiltreyiTemizle;
    TextView tvCountOpen, tvCountInProgress, tvCountClosed;

    List<Ariza> hamListe = new ArrayList<>();
    String aktifFiltre = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        TextView tvUser = findViewById(R.id.tvUser);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnEkle = findViewById(R.id.btnEkle);
        tvListeBaslik = findViewById(R.id.tvListeBaslik);
        tvFiltreyiTemizle = findViewById(R.id.tvFiltreyiTemizle);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // İstatistik Kartları
        MaterialCardView cardOpen = findViewById(R.id.cardOpen);
        MaterialCardView cardInProgress = findViewById(R.id.cardInProgress);
        MaterialCardView cardClosed = findViewById(R.id.cardClosed);

        tvCountOpen = findViewById(R.id.tvCountOpen);
        tvCountInProgress = findViewById(R.id.tvCountInProgress);
        tvCountClosed = findViewById(R.id.tvCountClosed);

        tvUser.setText("Hoş geldin, " + sessionManager.getUsername());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        String role = sessionManager.getRole();
        if (role.equals("client")) {
            btnEkle.setVisibility(View.VISIBLE);
        } else {
            btnEkle.setVisibility(View.GONE);
        }

        btnEkle.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ArizaEkleActivity.class));
        });

        cardOpen.setOnClickListener(v -> filtreyiUygula("OPEN"));
        cardInProgress.setOnClickListener(v -> filtreyiUygula("IN_PROGRESS"));
        cardClosed.setOnClickListener(v -> filtreyiUygula("CLOSED"));

        tvFiltreyiTemizle.setOnClickListener(v -> filtreyiUygula("ALL"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ArizaAdapter(new ArrayList<>(), ariza -> {
            Intent intent = new Intent(MainActivity.this, ArizaDetayActivity.class);
            intent.putExtra("id", ariza.getId());
            intent.putExtra("baslik", ariza.getBaslik());
            intent.putExtra("aciklama", ariza.getAciklama());
            intent.putExtra("durum", ariza.getDurum());
            intent.putExtra("tarih", ariza.getTarih());
            intent.putExtra("olusturan", ariza.getOlusturanKisi());
            intent.putExtra("atanan", ariza.getAtananKisi());
            intent.putExtra("aciliyet", ariza.getAciliyetDerecesi());
            intent.putExtra("konum", ariza.getKonum());
            intent.putExtra("kategori", ariza.getKategori());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listeyiYukleVeSay();
    }

    private void listeyiYukleVeSay() {
        if (adapter == null) return;

        String role = sessionManager.getRole();
        String username = sessionManager.getUsername();

        // 1. Rolüne göre verileri çek
        if (role.equals("client")) {
            hamListe = ArizaRepository.getByKullanici(username);
        } else if (role.equals("tech")) {
            hamListe = ArizaRepository.getByAtananKisi(username);
        } else {
            hamListe = ArizaRepository.getAll();
        }

        if (hamListe == null) hamListe = new ArrayList<>();

        // 2. Durumları say ve kartlara yazdır
        int countOpen = 0, countInProgress = 0, countClosed = 0;
        for (Ariza a : hamListe) {
            if ("OPEN".equals(a.getDurum())) countOpen++;
            else if ("IN_PROGRESS".equals(a.getDurum())) countInProgress++;
            else if ("CLOSED".equals(a.getDurum())) countClosed++;
        }

        tvCountOpen.setText(String.valueOf(countOpen));
        tvCountInProgress.setText(String.valueOf(countInProgress));
        tvCountClosed.setText(String.valueOf(countClosed));

        // 3. Mevcut filtreyi (ALL, OPEN vs.) listeye uygula
        filtreyiUygula(aktifFiltre);
    }

    private void filtreyiUygula(String durum) {

        // Eğer seçilen karta tekrar tıklanırsa filtreyi iptal et (Tümünü göster)
        if (aktifFiltre.equals(durum) && !durum.equals("ALL")) {
            aktifFiltre = "ALL";
        } else {
            aktifFiltre = durum;
        }

        List<Ariza> filtrelenmisListe = new ArrayList<>();

        for (Ariza a : hamListe) {
            if (aktifFiltre.equals("ALL") || a.getDurum().equals(aktifFiltre)) {
                filtrelenmisListe.add(a);
            }
        }

        if (aktifFiltre.equals("ALL")) {
            tvListeBaslik.setText("Tüm Kayıtlar");
            tvFiltreyiTemizle.setVisibility(View.GONE);
        } else if (aktifFiltre.equals("OPEN")) {
            tvListeBaslik.setText("Bekleyen Arızalar");
            tvFiltreyiTemizle.setVisibility(View.VISIBLE);
        } else if (aktifFiltre.equals("IN_PROGRESS")) {
            tvListeBaslik.setText("İşlemdeki Arızalar");
            tvFiltreyiTemizle.setVisibility(View.VISIBLE);
        } else if (aktifFiltre.equals("CLOSED")) {
            tvListeBaslik.setText("Çözülen Arızalar");
            tvFiltreyiTemizle.setVisibility(View.VISIBLE);
        }

        adapter.updateList(filtrelenmisListe);
    }
}
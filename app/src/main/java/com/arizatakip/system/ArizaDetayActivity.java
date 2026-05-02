package com.arizatakip.system;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.data.UserRepository;
import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArizaDetayActivity extends AppCompatActivity {

    TextView tvBaslik, tvAciklama, tvDurum, tvTarih, tvAtanan, tvOlusturan, tvAciliyet;
    Button btnBack, btnDurum, btnTeknikerAta, btnSil, btnDuzenle;
    SessionManager sessionManager;
    String role;

    int arizaId;
    String currentDurum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ariza_detay);

        sessionManager = new SessionManager(this);
        role = sessionManager.getRole();

        tvBaslik = findViewById(R.id.tvBaslik);
        tvAciklama = findViewById(R.id.tvAciklama);
        tvDurum = findViewById(R.id.tvDurum);
        tvTarih = findViewById(R.id.tvTarih);
        tvAtanan = findViewById(R.id.tvAtanan);
        tvOlusturan = findViewById(R.id.tvOlusturan);
        tvAciliyet = findViewById(R.id.tvAciliyet);

        btnBack = findViewById(R.id.btnBack);
        btnDurum = findViewById(R.id.btnDurum);
        btnTeknikerAta = findViewById(R.id.btnTeknikerAta);
        btnSil = findViewById(R.id.btnSil);
        btnDuzenle = findViewById(R.id.btnDuzenle);

        arizaId = getIntent().getIntExtra("id", -1);

        // İlk yükleme intent verisiyle yapılır; sonraki yenilemeler onResume'da DB'den gelir
        populateFromIntent(getIntent());

        btnBack.setOnClickListener(v -> finish());

        btnDuzenle.setOnClickListener(v -> {
            Ariza ariza = ArizaRepository.getById(arizaId);
            if (ariza == null) return;
            Intent editIntent = new Intent(this, ArizaEkleActivity.class);
            editIntent.putExtra("isEdit", true);
            editIntent.putExtra("editId", arizaId);
            editIntent.putExtra("editBaslik", ariza.getBaslik());
            editIntent.putExtra("editAciklama", ariza.getAciklama());
            editIntent.putExtra("editAciliyet", ariza.getAciliyetDerecesi());
            startActivity(editIntent);
        });

        btnDurum.setOnClickListener(v -> {
            String[] dbDurumlar = {"OPEN", "IN_PROGRESS", "CLOSED"};
            String[] gosterilenDurumlar = {"Beklemede", "İşlemde", "Çözüldü"};
            new AlertDialog.Builder(this)
                    .setTitle("Durum Güncelle")
                    .setItems(gosterilenDurumlar, (dialog, which) -> {
                        ArizaRepository.updateDurum(arizaId, dbDurumlar[which]);
                        currentDurum = dbDurumlar[which];
                        tvDurum.setText("Durum: " + gosterilenDurumlar[which]);
                    })
                    .show();
        });

        btnTeknikerAta.setOnClickListener(v -> {
            List<String> techList = UserRepository.getAllTechUsernames();
            if (techList == null || techList.isEmpty()) {
                Toast.makeText(this, "Sistemde teknik personel bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] techs = techList.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Tekniker Seçin")
                    .setItems(techs, (dialog, which) -> {
                        ArizaRepository.updateAtananKisi(arizaId, techs[which]);
                        tvAtanan.setText("Atanan: " + techs[which]);
                        btnDuzenle.setVisibility(View.GONE); // atandıktan sonra düzenleme kapanır
                        Toast.makeText(this, "Görev " + techs[which] + " adlı personele atandı.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        btnSil.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Dikkat!")
                .setMessage("Bu arıza kaydını kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                .setPositiveButton("Evet, Sil", (dialog, which) -> {
                    ArizaRepository.deleteById(arizaId);
                    Toast.makeText(this, "Arıza başarıyla silindi.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("İptal", null)
                .show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Düzenleme sonrası DB'den taze veriyi çek
        if (arizaId != -1) {
            Ariza ariza = ArizaRepository.getById(arizaId);
            if (ariza != null) refreshUI(ariza);
        }
    }

    private void populateFromIntent(Intent intent) {
        currentDurum = intent.getStringExtra("durum");

        tvBaslik.setText(intent.getStringExtra("baslik"));
        tvAciklama.setText(intent.getStringExtra("aciklama"));
        tvAtanan.setText("Atanan: " + intent.getStringExtra("atanan"));
        tvOlusturan.setText("Açan: " + intent.getStringExtra("olusturan"));

        applyDurumText(intent.getStringExtra("durum"));
        applyAciliyetBadge(intent.getStringExtra("aciliyet"));
        applyTarih(intent.getStringExtra("tarih"));
        applyButtonVisibility(intent.getStringExtra("atanan"));
    }

    private void refreshUI(Ariza ariza) {
        currentDurum = ariza.getDurum();

        tvBaslik.setText(ariza.getBaslik());
        tvAciklama.setText(ariza.getAciklama());
        tvAtanan.setText("Atanan: " + ariza.getAtananKisi());
        tvOlusturan.setText("Açan: " + ariza.getOlusturanKisi());

        applyDurumText(ariza.getDurum());
        applyAciliyetBadge(ariza.getAciliyetDerecesi());
        applyTarih(ariza.getTarih());
        applyButtonVisibility(ariza.getAtananKisi());
    }

    private void applyDurumText(String durum) {
        String label = durum;
        if ("OPEN".equals(durum)) label = "Beklemede";
        else if ("IN_PROGRESS".equals(durum)) label = "İşlemde";
        else if ("CLOSED".equals(durum)) label = "Çözüldü";
        tvDurum.setText("Durum: " + label);
    }

    private void applyAciliyetBadge(String aciliyet) {
        String label;
        int bgColor, textColor;
        if ("COK".equals(aciliyet)) {
            label = "Çok Acil";
            bgColor = Color.parseColor("#FFEBEE");
            textColor = Color.parseColor("#C62828");
        } else if ("ORTA".equals(aciliyet)) {
            label = "Orta";
            bgColor = Color.parseColor("#FFF3E0");
            textColor = Color.parseColor("#E65100");
        } else {
            label = "Normal";
            bgColor = Color.parseColor("#E8F5E9");
            textColor = Color.parseColor("#2E7D32");
        }
        tvAciliyet.setText(label);
        tvAciliyet.setTextColor(textColor);
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.RECTANGLE);
        badge.setCornerRadius(32f);
        badge.setColor(bgColor);
        tvAciliyet.setBackground(badge);
    }

    private void applyTarih(String tarih) {
        try {
            long ms = Long.parseLong(tarih);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("tr", "TR"));
            tvTarih.setText("Tarih: " + sdf.format(new Date(ms)));
        } catch (Exception e) {
            tvTarih.setText("Tarih: " + tarih);
        }
    }

    private void applyButtonVisibility(String atananKisi) {
        if ("client".equals(role)) {
            btnDurum.setVisibility(View.GONE);
            btnTeknikerAta.setVisibility(View.GONE);
            btnSil.setVisibility(View.GONE);
            boolean atanmamis = "Atanmadı".equals(atananKisi);
            btnDuzenle.setVisibility(atanmamis ? View.VISIBLE : View.GONE);
        } else if ("tech".equals(role)) {
            btnDurum.setVisibility(View.VISIBLE);
            btnTeknikerAta.setVisibility(View.GONE);
            btnSil.setVisibility(View.GONE);
            btnDuzenle.setVisibility(View.GONE);
        } else if ("admin".equals(role)) {
            btnDurum.setVisibility(View.VISIBLE);
            btnTeknikerAta.setVisibility(View.VISIBLE);
            btnSil.setVisibility(View.VISIBLE);
            btnDuzenle.setVisibility(View.GONE);
        }
    }
}

package com.arizatakip.system;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class ArizaEkleActivity extends AppCompatActivity {

    TextInputEditText etBaslik, etAciklama;
    AutoCompleteTextView actvAciliyet;
    Button btnKaydet;
    TextView tvTitle;

    private static final String[] ACILIYET_LABELS = {"Normal", "Orta", "Çok Acil"};
    private static final String[] ACILIYET_VALUES = {"NORMAL", "ORTA", "COK"};

    private boolean isEditMode = false;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ariza_ekle);

        tvTitle = findViewById(R.id.tvTitle);
        etBaslik = findViewById(R.id.etBaslik);
        etAciklama = findViewById(R.id.etAciklama);
        actvAciliyet = findViewById(R.id.actvAciliyet);
        btnKaydet = findViewById(R.id.btnKaydet);

        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, ACILIYET_LABELS);
        actvAciliyet.setAdapter(dropdownAdapter);

        isEditMode = getIntent().getBooleanExtra("isEdit", false);

        if (isEditMode) {
            editId = getIntent().getIntExtra("editId", -1);
            tvTitle.setText("Arızayı Düzenle");
            btnKaydet.setText("GÜNCELLE");

            etBaslik.setText(getIntent().getStringExtra("editBaslik"));
            etAciklama.setText(getIntent().getStringExtra("editAciklama"));

            String editAciliyet = getIntent().getStringExtra("editAciliyet");
            String editLabel = ACILIYET_LABELS[0];
            for (int i = 0; i < ACILIYET_VALUES.length; i++) {
                if (ACILIYET_VALUES[i].equals(editAciliyet)) {
                    editLabel = ACILIYET_LABELS[i];
                    break;
                }
            }
            actvAciliyet.setText(editLabel, false);
        } else {
            actvAciliyet.setText(ACILIYET_LABELS[0], false);
        }

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        btnKaydet.setOnClickListener(v -> {
            String baslik = etBaslik.getText().toString().trim();
            String aciklama = etAciklama.getText().toString().trim();
            String aciliyetLabel = actvAciliyet.getText().toString().trim();

            if (baslik.isEmpty() || aciklama.isEmpty()) {
                Toast.makeText(this, "Alanların boş bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            String aciliyetValue = "NORMAL";
            for (int i = 0; i < ACILIYET_LABELS.length; i++) {
                if (ACILIYET_LABELS[i].equals(aciliyetLabel)) {
                    aciliyetValue = ACILIYET_VALUES[i];
                    break;
                }
            }

            if (isEditMode) {
                ArizaRepository.updateAriza(editId, baslik, aciklama, aciliyetValue);
                Toast.makeText(this, "Arıza güncellendi", Toast.LENGTH_SHORT).show();
            } else {
                SessionManager session = new SessionManager(this);
                ArizaRepository.addAriza(baslik, aciklama, session.getUsername(), aciliyetValue);
                Toast.makeText(this, "Arıza kaydedildi", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }
}

package com.arizatakip.system;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.arizatakip.system.data.ArizaLogRepository;
import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArizaEkleActivity extends AppCompatActivity {

    TextInputEditText etBaslik, etAciklama;
    AutoCompleteTextView actvAciliyet, actvKonum, actvKategori;
    Button btnKaydet;
    TextView tvTitle;
    LinearLayout llImageSlots;

    private static final int MAX_GORSELLER = 3;
    private final List<String> gorselPaths = new ArrayList<>();
    private File cameraFile;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    private static final String[] ACILIYET_LABELS = {"Normal", "Orta", "Çok Acil"};
    private static final String[] ACILIYET_VALUES = {"NORMAL", "ORTA", "COK"};

    private static final String[] KONUM_SECENEKLERI = {
            "Genel Müdürlük", "Muhasebe", "İnsan Kaynakları", "Hukuk",
            "Satış ve Pazarlama", "Bilgi Teknolojileri", "Sunucu Odası",
            "Toplantı Odası", "Yemekhane", "Mutfak", "Depo",
            "Güvenlik", "Resepsiyon", "Arşiv", "Teknik Servis", "Diğer"
    };

    private static final String[] KATEGORI_SECENEKLERI = {
            "Elektrik", "Mekanik / İklimlendirme", "Tesisat",
            "Yapısal", "Donanım (IT)", "Yazılım", "Ağ / İnternet",
            "Asansör", "Yangın / Güvenlik Sistemi", "Diğer"
    };

    private boolean isEditMode = false;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && cameraFile != null) {
                gorselPaths.add(cameraFile.getAbsolutePath());
                refreshImageSlots();
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String saved = saveImageToInternal(uri);
                if (saved != null) {
                    gorselPaths.add(saved);
                    refreshImageSlots();
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchCamera();
            else Toast.makeText(this, "Kamera izni gereklidir", Toast.LENGTH_SHORT).show();
        });

        setContentView(R.layout.activity_ariza_ekle);

        tvTitle = findViewById(R.id.tvTitle);
        etBaslik = findViewById(R.id.etBaslik);
        etAciklama = findViewById(R.id.etAciklama);
        actvAciliyet = findViewById(R.id.actvAciliyet);
        actvKonum = findViewById(R.id.actvKonum);
        actvKategori = findViewById(R.id.actvKategori);
        btnKaydet = findViewById(R.id.btnKaydet);
        llImageSlots = findViewById(R.id.llImageSlots);

        actvAciliyet.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ACILIYET_LABELS));
        actvKonum.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, KONUM_SECENEKLERI));
        actvKategori.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, KATEGORI_SECENEKLERI));

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
                if (ACILIYET_VALUES[i].equals(editAciliyet)) { editLabel = ACILIYET_LABELS[i]; break; }
            }
            actvAciliyet.setText(editLabel, false);
            actvKonum.setText(getIntent().getStringExtra("editKonum"), false);
            actvKategori.setText(getIntent().getStringExtra("editKategori"), false);

            // Mevcut görselleri yükle
            Ariza existing = ArizaRepository.getById(editId);
            if (existing != null && existing.getGorseller() != null && !existing.getGorseller().isEmpty()) {
                gorselPaths.addAll(Arrays.asList(existing.getGorseller().split(",")));
            }
        } else {
            actvAciliyet.setText(ACILIYET_LABELS[0], false);
            actvKonum.setText(KONUM_SECENEKLERI[0], false);
            actvKategori.setText(KATEGORI_SECENEKLERI[0], false);
        }

        refreshImageSlots();

        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        btnKaydet.setOnClickListener(v -> {
            String baslik = etBaslik.getText().toString().trim();
            String aciklama = etAciklama.getText().toString().trim();
            String aciliyetLabel = actvAciliyet.getText().toString().trim();
            String konum = actvKonum.getText().toString().trim();
            String kategori = actvKategori.getText().toString().trim();

            if (baslik.isEmpty() || aciklama.isEmpty()) {
                Toast.makeText(this, "Alanların boş bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            String aciliyetValue = "NORMAL";
            for (int i = 0; i < ACILIYET_LABELS.length; i++) {
                if (ACILIYET_LABELS[i].equals(aciliyetLabel)) { aciliyetValue = ACILIYET_VALUES[i]; break; }
            }

            String gorselStr = String.join(",", gorselPaths);

            if (isEditMode) {
                ArizaRepository.updateAriza(editId, baslik, aciklama, aciliyetValue, konum, kategori);
                ArizaRepository.updateGorseller(editId, gorselStr);
                Toast.makeText(this, "Arıza güncellendi", Toast.LENGTH_SHORT).show();
            } else {
                SessionManager session = new SessionManager(this);
                long insertedId = ArizaRepository.addAriza(baslik, aciklama, session.getUsername(), aciliyetValue, konum, kategori, gorselStr);
                ArizaLogRepository.addLog((int) insertedId, session.getUsername(),
                        session.getUsername() + " arızayı açtı.");
                Toast.makeText(this, "Arıza kaydedildi", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    // ── Image slots ──────────────────────────────────────────────────────────

    private void refreshImageSlots() {
        llImageSlots.removeAllViews();
        for (int i = 0; i < gorselPaths.size(); i++) {
            llImageSlots.addView(buildThumbnailSlot(gorselPaths.get(i), i));
        }
        if (gorselPaths.size() < MAX_GORSELLER) {
            llImageSlots.addView(buildAddSlot());
        }
    }

    private android.view.View buildThumbnailSlot(String path, int index) {
        int size = dp(96);

        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(0, 0, dp(10), 0);
        frame.setLayoutParams(lp);

        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        card.setRadius(dp(10));
        card.setCardElevation(0f);
        card.setStrokeWidth(1);
        card.setStrokeColor(Color.parseColor("#DCE2EA"));

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap bmp = decodeThumbnail(path, size, size);
        if (bmp != null) iv.setImageBitmap(bmp);
        card.addView(iv);
        frame.addView(card);

        // Remove button
        TextView removeBtn = new TextView(this);
        int btnSize = dp(22);
        FrameLayout.LayoutParams btnLp = new FrameLayout.LayoutParams(btnSize, btnSize);
        btnLp.gravity = Gravity.TOP | Gravity.END;
        btnLp.setMargins(0, dp(4), dp(4), 0);
        removeBtn.setLayoutParams(btnLp);
        removeBtn.setText("✕");
        removeBtn.setTextColor(Color.WHITE);
        removeBtn.setTextSize(11f);
        removeBtn.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor("#CC1A56A0"));
        removeBtn.setBackground(circle);
        removeBtn.setOnClickListener(v -> {
            gorselPaths.remove(index);
            refreshImageSlots();
        });
        frame.addView(removeBtn);

        return frame;
    }

    private android.view.View buildAddSlot() {
        int size = dp(96);

        LinearLayout slot = new LinearLayout(this);
        slot.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        slot.setOrientation(LinearLayout.VERTICAL);
        slot.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(10));
        bg.setColor(Color.parseColor("#F0F3F7"));
        bg.setStroke(dp(1), Color.parseColor("#DCE2EA"));
        slot.setBackground(bg);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconLp.gravity = Gravity.CENTER_HORIZONTAL;
        icon.setLayoutParams(iconLp);
        icon.setImageResource(android.R.drawable.ic_input_add);
        icon.setColorFilter(Color.parseColor("#1A56A0"));
        slot.addView(icon);

        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.gravity = Gravity.CENTER_HORIZONTAL;
        labelLp.topMargin = dp(4);
        label.setLayoutParams(labelLp);
        label.setText("Görsel Ekle");
        label.setTextSize(10f);
        label.setTextColor(Color.parseColor("#1A56A0"));
        slot.addView(label);

        slot.setOnClickListener(v -> showImageSourceDialog());
        return slot;
    }

    // ── Camera / Gallery ─────────────────────────────────────────────────────

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Görsel Ekle")
                .setItems(new String[]{"Galeri", "Kamera"}, (dialog, which) -> {
                    if (which == 0) galleryLauncher.launch("image/*");
                    else checkCameraAndLaunch();
                })
                .show();
    }

    private void checkCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File dir = new File(getFilesDir(), "ariza_images");
            if (!dir.exists()) dir.mkdirs();
            cameraFile = new File(dir, "img_" + System.currentTimeMillis() + ".jpg");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraFile);
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternal(Uri sourceUri) {
        try {
            File dir = new File(getFilesDir(), "ariza_images");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, "img_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(sourceUri);
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return dest.getAbsolutePath();
        } catch (Exception e) {
            Toast.makeText(this, "Görsel kaydedilemedi", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Bitmap decodeThumbnail(String path, int reqW, int reqH) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            opts.inSampleSize = Math.max(1, Math.max(opts.outWidth / reqW, opts.outHeight / reqH));
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, opts);
        } catch (Exception e) {
            return null;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

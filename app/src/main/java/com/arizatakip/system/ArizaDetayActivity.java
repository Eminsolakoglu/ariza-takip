package com.arizatakip.system;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.data.UserRepository;
import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArizaDetayActivity extends AppCompatActivity {

    TextView tvBaslik, tvAciklama, tvDurum, tvTarih, tvAtanan, tvOlusturan, tvAciliyet, tvKonum, tvKategori;
    Button btnBack, btnDurum, btnTeknikerAta, btnSil, btnDuzenle;
    MaterialCardView cardGorseller;
    LinearLayout llImageSlots;

    SessionManager sessionManager;
    String role;
    int arizaId;
    String currentDurum;
    String currentAtanan;

    private static final int MAX_GORSELLER = 3;
    private final List<String> gorselPaths = new ArrayList<>();
    private File cameraFile;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && cameraFile != null) {
                gorselPaths.add(cameraFile.getAbsolutePath());
                saveAndRefreshSlots();
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String saved = saveImageToInternal(uri);
                if (saved != null) {
                    gorselPaths.add(saved);
                    saveAndRefreshSlots();
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchCamera();
            else Toast.makeText(this, "Kamera izni gereklidir", Toast.LENGTH_SHORT).show();
        });

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
        tvKonum = findViewById(R.id.tvKonum);
        tvKategori = findViewById(R.id.tvKategori);
        cardGorseller = findViewById(R.id.cardGorseller);
        llImageSlots = findViewById(R.id.llImageSlots);

        btnBack = findViewById(R.id.btnBack);
        btnDurum = findViewById(R.id.btnDurum);
        btnTeknikerAta = findViewById(R.id.btnTeknikerAta);
        btnSil = findViewById(R.id.btnSil);
        btnDuzenle = findViewById(R.id.btnDuzenle);

        arizaId = getIntent().getIntExtra("id", -1);
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
            editIntent.putExtra("editKonum", ariza.getKonum());
            editIntent.putExtra("editKategori", ariza.getKategori());
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
                        currentAtanan = techs[which];
                        tvAtanan.setText("Atanan: " + techs[which]);
                        btnDuzenle.setVisibility(View.GONE);
                        refreshImageSlots();
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
        if (arizaId != -1) {
            Ariza ariza = ArizaRepository.getById(arizaId);
            if (ariza != null) refreshUI(ariza);
        }
    }

    private void populateFromIntent(Intent intent) {
        currentDurum = intent.getStringExtra("durum");
        currentAtanan = intent.getStringExtra("atanan");

        tvBaslik.setText(intent.getStringExtra("baslik"));
        tvAciklama.setText(intent.getStringExtra("aciklama"));
        tvAtanan.setText("Atanan: " + currentAtanan);
        tvOlusturan.setText("Açan: " + intent.getStringExtra("olusturan"));

        applyDurumText(currentDurum);
        applyAciliyetBadge(intent.getStringExtra("aciliyet"));
        applyTarih(intent.getStringExtra("tarih"));
        applyMetaLabels(intent.getStringExtra("konum"), intent.getStringExtra("kategori"));
        applyButtonVisibility(currentAtanan);

        String gorsellerStr = intent.getStringExtra("gorseller");
        gorselPaths.clear();
        if (gorsellerStr != null && !gorsellerStr.isEmpty()) {
            gorselPaths.addAll(Arrays.asList(gorsellerStr.split(",")));
        }
        refreshImageSlots();
    }

    private void refreshUI(Ariza ariza) {
        currentDurum = ariza.getDurum();
        currentAtanan = ariza.getAtananKisi();

        tvBaslik.setText(ariza.getBaslik());
        tvAciklama.setText(ariza.getAciklama());
        tvAtanan.setText("Atanan: " + currentAtanan);
        tvOlusturan.setText("Açan: " + ariza.getOlusturanKisi());

        applyDurumText(currentDurum);
        applyAciliyetBadge(ariza.getAciliyetDerecesi());
        applyTarih(ariza.getTarih());
        applyMetaLabels(ariza.getKonum(), ariza.getKategori());
        applyButtonVisibility(currentAtanan);

        gorselPaths.clear();
        if (ariza.getGorseller() != null && !ariza.getGorseller().isEmpty()) {
            gorselPaths.addAll(Arrays.asList(ariza.getGorseller().split(",")));
        }
        refreshImageSlots();
    }

    // ── Image slots ──────────────────────────────────────────────────────────

    private void refreshImageSlots() {
        boolean canAdd    = canAddImages();
        boolean canRemove = canRemoveImages();
        llImageSlots.removeAllViews();

        for (int i = 0; i < gorselPaths.size(); i++) {
            llImageSlots.addView(buildThumbnailSlot(gorselPaths.get(i), i, canRemove));
        }
        if (canAdd && gorselPaths.size() < MAX_GORSELLER) {
            llImageSlots.addView(buildAddSlot());
        }

        boolean shouldShow = !gorselPaths.isEmpty() || canAdd;
        cardGorseller.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    /** Görsel ekleyebilir: admin her zaman, client sadece atanmamış arızada */
    private boolean canAddImages() {
        if ("admin".equals(role)) return true;
        if ("client".equals(role)) return "Atanmadı".equals(currentAtanan);
        return false;
    }

    /** Görsel kaldırabilir: admin ve client her zaman */
    private boolean canRemoveImages() {
        return "admin".equals(role) || "client".equals(role);
    }

    private void saveAndRefreshSlots() {
        ArizaRepository.updateGorseller(arizaId, String.join(",", gorselPaths));
        refreshImageSlots();
    }

    private android.view.View buildThumbnailSlot(String path, int index, boolean canRemove) {
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
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, GorselDetayActivity.class);
            intent.putExtra(GorselDetayActivity.EXTRA_PATH, path);
            startActivity(intent);
        });
        frame.addView(card);

        if (canRemove) {
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
                saveAndRefreshSlots();
            });
            frame.addView(removeBtn);
        }

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

    // ── UI helpers ────────────────────────────────────────────────────────────

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
            bgColor = Color.parseColor("#EBF2FC");
            textColor = Color.parseColor("#0F3D7A");
        } else if ("ORTA".equals(aciliyet)) {
            label = "Orta";
            bgColor = Color.parseColor("#E3EAF6");
            textColor = Color.parseColor("#2E6DC4");
        } else {
            label = "Normal";
            bgColor = Color.parseColor("#F0F3F7");
            textColor = Color.parseColor("#4A5568");
        }
        tvAciliyet.setText(label);
        tvAciliyet.setTextColor(textColor);
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.RECTANGLE);
        badge.setCornerRadius(32f);
        badge.setColor(bgColor);
        tvAciliyet.setBackground(badge);
    }

    private void applyMetaLabels(String konum, String kategori) {
        tvKonum.setText("📍 " + (konum != null && !konum.isEmpty() ? konum : "—"));
        tvKategori.setText("🏷 " + (kategori != null && !kategori.isEmpty() ? kategori : "—"));
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

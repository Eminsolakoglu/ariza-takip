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
import android.widget.EditText;
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
import android.widget.ImageButton;
import com.google.android.material.button.MaterialButton;
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
    TextView chevBaslik, chevAciklama, chevDurum, chevAciliyet, chevKonum, chevKategori, chevAtanan;
    LinearLayout rowBaslik, rowAciklama, rowDurum, rowAciliyet, rowKonum, rowKategori, rowAtanan;
    ImageButton btnBack;
    MaterialButton btnSil;
    MaterialCardView cardGorseller;
    LinearLayout llImageSlots;

    SessionManager sessionManager;
    String role;
    int arizaId;
    String currentDurum;
    String currentAtanan;
    String currentAciliyet;
    String currentKonum;
    String currentKategori;

    private static final int MAX_GORSELLER = 3;
    private final List<String> gorselPaths = new ArrayList<>();
    private File cameraFile;

    private static final String[] DURUM_DISPLAY = {"Beklemede", "İşlemde", "Çözüldü"};
    private static final String[] DURUM_VALUES  = {"OPEN", "IN_PROGRESS", "CLOSED"};
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

        tvBaslik    = findViewById(R.id.tvBaslik);
        tvAciklama  = findViewById(R.id.tvAciklama);
        tvDurum     = findViewById(R.id.tvDurum);
        tvTarih     = findViewById(R.id.tvTarih);
        tvAtanan    = findViewById(R.id.tvAtanan);
        tvOlusturan = findViewById(R.id.tvOlusturan);
        tvAciliyet  = findViewById(R.id.tvAciliyet);
        tvKonum     = findViewById(R.id.tvKonum);
        tvKategori  = findViewById(R.id.tvKategori);

        chevBaslik   = findViewById(R.id.chevBaslik);
        chevAciklama = findViewById(R.id.chevAciklama);
        chevDurum    = findViewById(R.id.chevDurum);
        chevAciliyet = findViewById(R.id.chevAciliyet);
        chevKonum    = findViewById(R.id.chevKonum);
        chevKategori = findViewById(R.id.chevKategori);
        chevAtanan   = findViewById(R.id.chevAtanan);

        rowBaslik   = findViewById(R.id.rowBaslik);
        rowAciklama = findViewById(R.id.rowAciklama);
        rowDurum    = findViewById(R.id.rowDurum);
        rowAciliyet = findViewById(R.id.rowAciliyet);
        rowKonum    = findViewById(R.id.rowKonum);
        rowKategori = findViewById(R.id.rowKategori);
        rowAtanan   = findViewById(R.id.rowAtanan);

        cardGorseller = findViewById(R.id.cardGorseller);
        llImageSlots  = findViewById(R.id.llImageSlots);

        btnBack = findViewById(R.id.btnBack);
        btnSil  = findViewById(R.id.btnSil);

        arizaId = getIntent().getIntExtra("id", -1);
        populateFromIntent(getIntent());

        btnBack.setOnClickListener(v -> finish());

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
        currentDurum    = intent.getStringExtra("durum");
        currentAtanan   = intent.getStringExtra("atanan");
        currentAciliyet = intent.getStringExtra("aciliyet");
        currentKonum    = intent.getStringExtra("konum");
        currentKategori = intent.getStringExtra("kategori");

        tvBaslik.setText(intent.getStringExtra("baslik"));
        tvAciklama.setText(intent.getStringExtra("aciklama"));
        tvAtanan.setText(currentAtanan);
        tvOlusturan.setText(intent.getStringExtra("olusturan"));

        applyDurumText(currentDurum);
        applyAciliyetBadge(currentAciliyet);
        applyTarih(intent.getStringExtra("tarih"));
        applyMetaLabels(currentKonum, currentKategori);
        setupEditAffordance();
        applyButtonVisibility();

        String gorsellerStr = intent.getStringExtra("gorseller");
        gorselPaths.clear();
        if (gorsellerStr != null && !gorsellerStr.isEmpty()) {
            gorselPaths.addAll(Arrays.asList(gorsellerStr.split(",")));
        }
        refreshImageSlots();
    }

    private void refreshUI(Ariza ariza) {
        currentDurum    = ariza.getDurum();
        currentAtanan   = ariza.getAtananKisi();
        currentAciliyet = ariza.getAciliyetDerecesi();
        currentKonum    = ariza.getKonum();
        currentKategori = ariza.getKategori();

        tvBaslik.setText(ariza.getBaslik());
        tvAciklama.setText(ariza.getAciklama());
        tvAtanan.setText(currentAtanan);
        tvOlusturan.setText(ariza.getOlusturanKisi());

        applyDurumText(currentDurum);
        applyAciliyetBadge(currentAciliyet);
        applyTarih(ariza.getTarih());
        applyMetaLabels(currentKonum, currentKategori);
        setupEditAffordance();
        applyButtonVisibility();

        gorselPaths.clear();
        if (ariza.getGorseller() != null && !ariza.getGorseller().isEmpty()) {
            gorselPaths.addAll(Arrays.asList(ariza.getGorseller().split(",")));
        }
        refreshImageSlots();
    }

    // ── Inline edit affordance ───────────────────────────────────────────────

    private void setupEditAffordance() {
        boolean isAdmin = "admin".equals(role);
        boolean isTech  = "tech".equals(role);
        boolean clientCanEdit = "client".equals(role) && "Atanmadı".equals(currentAtanan);

        // Başlık
        if (isAdmin || clientCanEdit) {
            chevBaslik.setVisibility(View.VISIBLE);
            rowBaslik.setOnClickListener(v -> showTextEditDialog("Başlık düzenle",
                    tvBaslik.getText().toString(), text -> {
                        ArizaRepository.updateAriza(arizaId, text,
                                tvAciklama.getText().toString(), currentAciliyet, currentKonum, currentKategori);
                        tvBaslik.setText(text);
                    }));
        } else {
            chevBaslik.setVisibility(View.GONE);
            rowBaslik.setClickable(false);
        }

        // Açıklama
        if (isAdmin || clientCanEdit) {
            chevAciklama.setVisibility(View.VISIBLE);
            rowAciklama.setOnClickListener(v -> showTextEditDialog("Açıklama düzenle",
                    tvAciklama.getText().toString(), text -> {
                        ArizaRepository.updateAriza(arizaId, tvBaslik.getText().toString(),
                                text, currentAciliyet, currentKonum, currentKategori);
                        tvAciklama.setText(text);
                    }));
        } else {
            chevAciklama.setVisibility(View.GONE);
            rowAciklama.setClickable(false);
        }

        // Durum
        if (isAdmin || isTech) {
            chevDurum.setVisibility(View.VISIBLE);
            rowDurum.setOnClickListener(v -> showListDialog("Durum güncelle", DURUM_DISPLAY,
                    indexOf(DURUM_VALUES, currentDurum), which -> {
                        ArizaRepository.updateDurum(arizaId, DURUM_VALUES[which]);
                        currentDurum = DURUM_VALUES[which];
                        applyDurumText(currentDurum);
                    }));
        } else {
            chevDurum.setVisibility(View.GONE);
            rowDurum.setClickable(false);
        }

        // Aciliyet
        if (isAdmin || clientCanEdit) {
            chevAciliyet.setVisibility(View.VISIBLE);
            rowAciliyet.setOnClickListener(v -> showListDialog("Aciliyet seç", ACILIYET_LABELS,
                    indexOf(ACILIYET_VALUES, currentAciliyet), which -> {
                        currentAciliyet = ACILIYET_VALUES[which];
                        ArizaRepository.updateAriza(arizaId, tvBaslik.getText().toString(),
                                tvAciklama.getText().toString(), currentAciliyet, currentKonum, currentKategori);
                        applyAciliyetBadge(currentAciliyet);
                    }));
        } else {
            chevAciliyet.setVisibility(View.GONE);
            rowAciliyet.setClickable(false);
        }

        // Konum
        if (isAdmin || clientCanEdit) {
            chevKonum.setVisibility(View.VISIBLE);
            rowKonum.setOnClickListener(v -> showListDialog("Konum seç", KONUM_SECENEKLERI,
                    indexOf(KONUM_SECENEKLERI, currentKonum), which -> {
                        currentKonum = KONUM_SECENEKLERI[which];
                        ArizaRepository.updateAriza(arizaId, tvBaslik.getText().toString(),
                                tvAciklama.getText().toString(), currentAciliyet, currentKonum, currentKategori);
                        tvKonum.setText(currentKonum);
                    }));
        } else {
            chevKonum.setVisibility(View.GONE);
            rowKonum.setClickable(false);
        }

        // Kategori
        if (isAdmin || clientCanEdit) {
            chevKategori.setVisibility(View.VISIBLE);
            rowKategori.setOnClickListener(v -> showListDialog("Kategori seç", KATEGORI_SECENEKLERI,
                    indexOf(KATEGORI_SECENEKLERI, currentKategori), which -> {
                        currentKategori = KATEGORI_SECENEKLERI[which];
                        ArizaRepository.updateAriza(arizaId, tvBaslik.getText().toString(),
                                tvAciklama.getText().toString(), currentAciliyet, currentKonum, currentKategori);
                        tvKategori.setText(currentKategori);
                    }));
        } else {
            chevKategori.setVisibility(View.GONE);
            rowKategori.setClickable(false);
        }

        // Atanan (admin only)
        if (isAdmin) {
            chevAtanan.setVisibility(View.VISIBLE);
            rowAtanan.setOnClickListener(v -> {
                List<String> techList = UserRepository.getAllTechUsernames();
                if (techList == null || techList.isEmpty()) {
                    Toast.makeText(this, "Sistemde teknik personel bulunamadı!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] techs = techList.toArray(new String[0]);
                showListDialog("Tekniker Seç", techs, indexOf(techs, currentAtanan), which -> {
                    ArizaRepository.updateAtananKisi(arizaId, techs[which]);
                    currentAtanan = techs[which];
                    tvAtanan.setText(currentAtanan);
                    setupEditAffordance();
                    refreshImageSlots();
                    Toast.makeText(this, "Görev " + techs[which] + " adlı personele atandı.", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            chevAtanan.setVisibility(View.GONE);
            rowAtanan.setClickable(false);
        }
    }

    // ── Dialog helpers ───────────────────────────────────────────────────────

    private void showTextEditDialog(String title, String currentValue, OnTextConfirm callback) {
        EditText input = new EditText(this);
        input.setText(currentValue);
        if (currentValue != null) input.setSelection(currentValue.length());
        input.setSingleLine(false);
        int pad = dp(16);
        input.setPadding(pad, pad / 2, pad, pad / 2);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Kaydet", (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) callback.onConfirm(text);
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void showListDialog(String title, String[] items, int checkedItem, OnItemSelected callback) {
        final int[] selected = {checkedItem};
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(items, checkedItem, (d, w) -> selected[0] = w)
                .setPositiveButton("Tamam", (d, w) -> { if (selected[0] >= 0) callback.onSelect(selected[0]); })
                .setNegativeButton("İptal", null)
                .show();
    }

    private int indexOf(String[] arr, String value) {
        if (value == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) return i;
        }
        return -1;
    }

    interface OnTextConfirm { void onConfirm(String text); }
    interface OnItemSelected { void onSelect(int which); }

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

    private boolean canAddImages() {
        if ("admin".equals(role)) return true;
        if ("client".equals(role)) return "Atanmadı".equals(currentAtanan);
        return false;
    }

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
        if ("OPEN".equals(durum))        label = "Beklemede";
        else if ("IN_PROGRESS".equals(durum)) label = "İşlemde";
        else if ("CLOSED".equals(durum)) label = "Çözüldü";
        tvDurum.setText(label);
    }

    private void applyAciliyetBadge(String aciliyet) {
        String label;
        int bgColor, textColor;
        if ("COK".equals(aciliyet)) {
            label     = "Çok Acil";
            bgColor   = Color.parseColor("#EBF2FC");
            textColor = Color.parseColor("#0F3D7A");
        } else if ("ORTA".equals(aciliyet)) {
            label     = "Orta";
            bgColor   = Color.parseColor("#E3EAF6");
            textColor = Color.parseColor("#2E6DC4");
        } else {
            label     = "Normal";
            bgColor   = Color.parseColor("#F0F3F7");
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
        tvKonum.setText(konum != null && !konum.isEmpty() ? konum : "—");
        tvKategori.setText(kategori != null && !kategori.isEmpty() ? kategori : "—");
    }

    private void applyTarih(String tarih) {
        try {
            long ms = Long.parseLong(tarih);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("tr", "TR"));
            tvTarih.setText(sdf.format(new Date(ms)));
        } catch (Exception e) {
            tvTarih.setText(tarih);
        }
    }

    private void applyButtonVisibility() {
        btnSil.setVisibility("admin".equals(role) ? View.VISIBLE : View.GONE);
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

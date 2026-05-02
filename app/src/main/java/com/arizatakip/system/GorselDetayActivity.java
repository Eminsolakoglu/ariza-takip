package com.arizatakip.system;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GorselDetayActivity extends AppCompatActivity {

    public static final String EXTRA_PATH = "gorsel_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gorsel_detay);

        ImageView ivFullImage = findViewById(R.id.ivFullImage);
        ImageButton btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());
        ivFullImage.setOnClickListener(v -> finish());

        String path = getIntent().getStringExtra(EXTRA_PATH);
        if (path != null) {
            Bitmap bmp = decodeBitmap(path);
            if (bmp != null) ivFullImage.setImageBitmap(bmp);
        }
    }

    private Bitmap decodeBitmap(String path) {
        try {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int maxW = dm.widthPixels;
            int maxH = dm.heightPixels;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            opts.inSampleSize = Math.max(1, Math.max(opts.outWidth / maxW, opts.outHeight / maxH));
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, opts);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.arizatakip.system.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arizatakip.system.R;

public class ToastHelper {

    public static void olusturuldu(Context ctx, String mesaj) {
        show(ctx, mesaj, R.drawable.ic_check_white, "#1A56A0");
    }

    public static void guncellendi(Context ctx, String mesaj) {
        show(ctx, mesaj, R.drawable.ic_edit_white, "#2E6DC4");
    }

    public static void silindi(Context ctx, String mesaj) {
        show(ctx, mesaj, R.drawable.ic_delete_white, "#8896A8");
    }

    public static void hata(Context ctx, String mesaj) {
        show(ctx, mesaj, R.drawable.ic_error_white, "#EF4444");
    }

    @SuppressWarnings("deprecation")
    private static void show(Context ctx, String mesaj, int iconRes, String bgColorHex) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.toast_custom, null);

        FrameLayout iconContainer = view.findViewById(R.id.iconContainer);
        ImageView icon = view.findViewById(R.id.toastIcon);
        TextView message = view.findViewById(R.id.toastMessage);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(bgColorHex));
        iconContainer.setBackground(circle);

        icon.setImageResource(iconRes);
        message.setText(mesaj);

        Toast toast = new Toast(ctx);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
}

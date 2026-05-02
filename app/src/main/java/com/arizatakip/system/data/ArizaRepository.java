package com.arizatakip.system.data;

import android.content.Context;

import com.arizatakip.system.model.Ariza;

import java.util.List;

public class ArizaRepository {

    private static AppDatabase db;

    public static void init(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // addAriza metodunu şu şekilde değiştir:
// addAriza metodunda yeni arıza açıldığında henüz kimseye atanmadığı için boş bırakıyoruz
    public static void addAriza(String baslik, String aciklama, String olusturanKisi, String aciliyetDerecesi, String konum, String kategori) {
        Ariza ariza = new Ariza(
                baslik,
                aciklama,
                "OPEN",
                String.valueOf(System.currentTimeMillis()),
                olusturanKisi,
                "Atanmadı",
                aciliyetDerecesi,
                konum,
                kategori
        );
        db.arizaDao().insert(ariza);
    }

    // Yeni metotlarımızı dışa açıyoruz
    public static List<Ariza> getByAtananKisi(String tech) {
        return db.arizaDao().getByAtananKisi(tech);
    }

    public static void updateAtananKisi(int id, String tech) {
        db.arizaDao().updateAtananKisi(id, tech);
    }
    public static void deleteById(int id) {
        db.arizaDao().deleteById(id);
    }
    public static List<Ariza> getAll() {
        return db.arizaDao().getAll();
    }
    public static void updateDurum(int id, String durum) {
        db.arizaDao().updateDurum(id, durum);
    }
    public static List<Ariza> getByKullanici(String kisi) {
        return db.arizaDao().getByKullanici(kisi);
    }

    public static Ariza getById(int id) {
        return db.arizaDao().getById(id);
    }

    public static void updateAriza(int id, String baslik, String aciklama, String aciliyetDerecesi, String konum, String kategori) {
        db.arizaDao().updateAriza(id, baslik, aciklama, aciliyetDerecesi, konum, kategori);
    }
}
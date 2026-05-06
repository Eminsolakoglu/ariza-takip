package com.arizatakip.system.data;

import android.content.Context;

import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.model.ArizaLog;

import java.util.List;

public class ArizaRepository {

    private static AppDatabase db;

    public static void init(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public static long addAriza(String baslik, String aciklama, String olusturanKisi, String aciliyetDerecesi, String konum, String kategori, String gorseller) {
        Ariza ariza = new Ariza(
                baslik, aciklama, "OPEN",
                String.valueOf(System.currentTimeMillis()),
                olusturanKisi, "Atanmadı",
                aciliyetDerecesi, konum, kategori, gorseller
        );
        return db.arizaDao().insert(ariza);
    }

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

    public static void updateGorseller(int id, String gorseller) {
        db.arizaDao().updateGorseller(id, gorseller);
    }

    public static int getCount() {
        return db.arizaDao().getCount();
    }

    // Uygulama ilk açıldığında demo arızaları ekler (sadece DB boşsa)
    public static void prepopulateDemo() {
        if (db.arizaDao().getCount() > 0) return;

        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        // 1 – OPEN: Klima, client, 3 gün önce
        int id1 = (int) db.arizaDao().insert(new Ariza(
                "Klima Arızası",
                "Ofis kliması çalışmıyor, sıcaklık artmaya devam ediyor.",
                "OPEN", String.valueOf(now - 3 * day),
                "client", "Atanmadı", "COK", "Genel Müdürlük", "Mekanik / İklimlendirme", ""));
        db.arizaLogDao().insert(new ArizaLog(id1, now - 3 * day, "client", "client arızayı açtı."));

        // 2 – OPEN: İnternet, client2, 2 gün önce
        int id2 = (int) db.arizaDao().insert(new Ariza(
                "Wi-Fi Bağlantı Kesintisi",
                "Satış ofisinde internet bağlantısı kesiliyor.",
                "OPEN", String.valueOf(now - 2 * day),
                "client2", "Atanmadı", "COK", "Satış ve Pazarlama", "Ağ / İnternet", ""));
        db.arizaLogDao().insert(new ArizaLog(id2, now - 2 * day, "client2", "client2 arızayı açtı."));

        // 3 – OPEN: Yazılım, client, 4 gün önce
        int id3 = (int) db.arizaDao().insert(new Ariza(
                "Muhasebe Yazılımı Lisans Hatası",
                "Muhasebe programı lisans süresi dolmuş, açılmıyor.",
                "OPEN", String.valueOf(now - 4 * day),
                "client", "Atanmadı", "ORTA", "Muhasebe", "Yazılım", ""));
        db.arizaLogDao().insert(new ArizaLog(id3, now - 4 * day, "client", "client arızayı açtı."));

        // 4 – OPEN: Güvenlik, client2, 1 gün önce
        int id4 = (int) db.arizaDao().insert(new Ariza(
                "Güvenlik Kamerası Görüntü Vermiyor",
                "Resepsiyon güvenlik kamerası görüntü göndermiyor.",
                "OPEN", String.valueOf(now - 1 * day),
                "client2", "Atanmadı", "ORTA", "Resepsiyon", "Yangın / Güvenlik Sistemi", ""));
        db.arizaLogDao().insert(new ArizaLog(id4, now - 1 * day, "client2", "client2 arızayı açtı."));

        // 5 – IN_PROGRESS: Elektrik, tech, 5 gün önce
        int id5 = (int) db.arizaDao().insert(new Ariza(
                "Elektrik Prizi Arızası",
                "Toplantı odasındaki priz grubu çalışmıyor.",
                "IN_PROGRESS", String.valueOf(now - 5 * day),
                "client", "tech", "ORTA", "Toplantı Odası", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id5, now - 5 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id5, now - 5 * day + 30 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id5, now - 4 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 6 – IN_PROGRESS: Donanım, tech, 6 gün önce
        int id6 = (int) db.arizaDao().insert(new Ariza(
                "Sunucu Odası Bakım Talebi",
                "Sunucu fanları aşırı ısınıyor, acil bakım gerekiyor.",
                "IN_PROGRESS", String.valueOf(now - 6 * day),
                "client", "tech", "COK", "Sunucu Odası", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id6, now - 6 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id6, now - 6 * day + 20 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id6, now - 5 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 7 – IN_PROGRESS: Asansör, tech2, 5 gün önce
        int id7 = (int) db.arizaDao().insert(new Ariza(
                "Asansör Kapısı Açılmıyor",
                "2. kattaki asansör kapısı otomatik açılmıyor.",
                "IN_PROGRESS", String.valueOf(now - 5 * day),
                "client2", "tech2", "COK", "Genel Müdürlük", "Asansör", ""));
        db.arizaLogDao().insert(new ArizaLog(id7, now - 5 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id7, now - 5 * day + 45 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id7, now - 4 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 8 – IN_PROGRESS: Tesisat, tech, 7 gün önce
        int id8 = (int) db.arizaDao().insert(new Ariza(
                "Mutfak Tesisat Sızıntısı",
                "Mutfak lavabo altında su sızıntısı var.",
                "IN_PROGRESS", String.valueOf(now - 7 * day),
                "client", "tech", "COK", "Mutfak", "Tesisat", ""));
        db.arizaLogDao().insert(new ArizaLog(id8, now - 7 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id8, now - 7 * day + 60 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id8, now - 6 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 9 – CLOSED: Donanım, tech2, açıldı 7 gün, kapandı 2 gün önce (5 günlük çözüm)
        int id9 = (int) db.arizaDao().insert(new Ariza(
                "Projektör Değişimi",
                "Toplantı odası projektörü görüntü vermiyor, değişim gerekiyor.",
                "CLOSED", String.valueOf(now - 7 * day),
                "client2", "tech2", "NORMAL", "Toplantı Odası", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id9, now - 7 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id9, now - 7 * day + 2 * 60 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id9, now - 6 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id9, now - 2 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 10 – CLOSED: Elektrik, tech, açıldı 7 gün, kapandı 3 gün önce (4 günlük çözüm)
        int id10 = (int) db.arizaDao().insert(new Ariza(
                "Elektrik Panosu Sigortası",
                "Teknik servis elektrik panosunun sigortası atmaya devam ediyor.",
                "CLOSED", String.valueOf(now - 7 * day),
                "client", "tech", "COK", "Teknik Servis", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id10, now - 7 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id10, now - 7 * day + 30 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id10, now - 6 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id10, now - 3 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));
    }
}

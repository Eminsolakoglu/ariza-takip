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

        // ── 10 OPEN – 10 farklı kategori, son 6 güne yayılmış, client/client2 eşit ──

        // 11 – OPEN: Yapısal – İnsan Kaynakları – client – 12 saat önce
        int id11 = (int) db.arizaDao().insert(new Ariza(
                "Asma Tavan Çöküyor",
                "İnsan Kaynakları ofisindeki asma tavanda derin çatlak ve sarkma var.",
                "OPEN", String.valueOf(now - 12 * 60 * 60 * 1000L),
                "client", "Atanmadı", "ORTA", "İnsan Kaynakları", "Yapısal", ""));
        db.arizaLogDao().insert(new ArizaLog(id11, now - 12 * 60 * 60 * 1000L, "client", "client arızayı açtı."));

        // 12 – OPEN: Donanım (IT) – Hukuk – client2 – 1.5 gün önce
        int id12 = (int) db.arizaDao().insert(new Ariza(
                "Fotokopi Makinesi Kağıt Sıkıştırıyor",
                "Hukuk birimine ait fotokopi makinesi sürekli kağıt sıkıştırıyor.",
                "OPEN", String.valueOf(now - (long)(1.5 * day)),
                "client2", "Atanmadı", "NORMAL", "Hukuk", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id12, now - (long)(1.5 * day), "client2", "client2 arızayı açtı."));

        // 13 – OPEN: Elektrik – Yemekhane – client – 2.5 gün önce
        int id13 = (int) db.arizaDao().insert(new Ariza(
                "Yemekhane Aydınlatma Arızası",
                "Yemekhane tavan lambalarının üçte biri yanmıyor.",
                "OPEN", String.valueOf(now - (long)(2.5 * day)),
                "client", "Atanmadı", "COK", "Yemekhane", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id13, now - (long)(2.5 * day), "client", "client arızayı açtı."));

        // 14 – OPEN: Tesisat – Depo – client2 – 3.5 gün önce
        int id14 = (int) db.arizaDao().insert(new Ariza(
                "Depo Su Sızıntısı",
                "Depo alanının köşesinde tavandan su damlıyor, raf sistemleri ıslak.",
                "OPEN", String.valueOf(now - (long)(3.5 * day)),
                "client2", "Atanmadı", "ORTA", "Depo", "Tesisat", ""));
        db.arizaLogDao().insert(new ArizaLog(id14, now - (long)(3.5 * day), "client2", "client2 arızayı açtı."));

        // 15 – OPEN: Asansör – Arşiv – client – 4.5 gün önce
        int id15 = (int) db.arizaDao().insert(new Ariza(
                "Asansör Sesli Uyarı Sistemi Çalışmıyor",
                "Arşiv katına çıkan asansörde sesli uyarı butonu ve alarm çalışmıyor.",
                "OPEN", String.valueOf(now - (long)(4.5 * day)),
                "client", "Atanmadı", "COK", "Arşiv", "Asansör", ""));
        db.arizaLogDao().insert(new ArizaLog(id15, now - (long)(4.5 * day), "client", "client arızayı açtı."));

        // 16 – OPEN: Diğer – Güvenlik – client2 – 5.5 gün önce
        int id16 = (int) db.arizaDao().insert(new Ariza(
                "Güvenlik Noktası Isıtıcı Arızası",
                "Dış güvenlik kabinindeki elektrikli ısıtıcı çalışmıyor.",
                "OPEN", String.valueOf(now - (long)(5.5 * day)),
                "client2", "Atanmadı", "NORMAL", "Güvenlik", "Diğer", ""));
        db.arizaLogDao().insert(new ArizaLog(id16, now - (long)(5.5 * day), "client2", "client2 arızayı açtı."));

        // 17 – OPEN: Mekanik / İklimlendirme – Bilgi Teknolojileri – client – 1 gün önce
        int id17 = (int) db.arizaDao().insert(new Ariza(
                "BT Odası Klima Filtresi Tıkalı",
                "Bilgi teknolojileri odasının klima filtresi tıkalı, oda ısınıyor.",
                "OPEN", String.valueOf(now - day),
                "client", "Atanmadı", "ORTA", "Bilgi Teknolojileri", "Mekanik / İklimlendirme", ""));
        db.arizaLogDao().insert(new ArizaLog(id17, now - day, "client", "client arızayı açtı."));

        // 18 – OPEN: Ağ / İnternet – İnsan Kaynakları – client2 – 2 gün önce
        int id18 = (int) db.arizaDao().insert(new Ariza(
                "VPN Bağlantısı Kesilmesi",
                "İnsan kaynakları personeli VPN'e bağlanamıyor, uzaktan erişim yok.",
                "OPEN", String.valueOf(now - 2 * day),
                "client2", "Atanmadı", "COK", "İnsan Kaynakları", "Ağ / İnternet", ""));
        db.arizaLogDao().insert(new ArizaLog(id18, now - 2 * day, "client2", "client2 arızayı açtı."));

        // 19 – OPEN: Yazılım – Toplantı Odası – client – 3 gün önce
        int id19 = (int) db.arizaDao().insert(new Ariza(
                "Video Konferans Yazılımı Açılmıyor",
                "Toplantı odasındaki bilgisayarda konferans yazılımı güncelleme hatasında takılı.",
                "OPEN", String.valueOf(now - 3 * day),
                "client", "Atanmadı", "ORTA", "Toplantı Odası", "Yazılım", ""));
        db.arizaLogDao().insert(new ArizaLog(id19, now - 3 * day, "client", "client arızayı açtı."));

        // 20 – OPEN: Yangın / Güvenlik Sistemi – Depo – client2 – 4 gün önce
        int id20 = (int) db.arizaDao().insert(new Ariza(
                "Yangın Dedektörü Pil Uyarısı",
                "Depo alanındaki yangın dedektörleri sürekli pil uyarısı veriyor.",
                "OPEN", String.valueOf(now - 4 * day),
                "client2", "Atanmadı", "COK", "Depo", "Yangın / Güvenlik Sistemi", ""));
        db.arizaLogDao().insert(new ArizaLog(id20, now - 4 * day, "client2", "client2 arızayı açtı."));

        // ── 10 IN_PROGRESS – tech: 21,23,26,27,30 / tech2: 22,24,25,28,29 ──────

        // 21 – IN_PROGRESS: Elektrik – Bilgi Teknolojileri – tech – 8 gün önce
        int id21 = (int) db.arizaDao().insert(new Ariza(
                "UPS Akü Değişimi",
                "Bilgi teknolojileri UPS cihazı akü kapasitesi %20'ye düştü.",
                "IN_PROGRESS", String.valueOf(now - 8 * day),
                "client", "tech", "ORTA", "Bilgi Teknolojileri", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id21, now - 8 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id21, now - 8 * day + 40 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id21, now - 7 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 22 – IN_PROGRESS: Tesisat – Arşiv – tech2 – 9 gün önce
        int id22 = (int) db.arizaDao().insert(new Ariza(
                "Arşiv Boru Sızıntısı",
                "Arşiv bölümü duvar içindeki boru hattından sızıntı tespit edildi.",
                "IN_PROGRESS", String.valueOf(now - 9 * day),
                "client2", "tech2", "COK", "Arşiv", "Tesisat", ""));
        db.arizaLogDao().insert(new ArizaLog(id22, now - 9 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id22, now - 9 * day + 35 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id22, now - 8 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 23 – IN_PROGRESS: Donanım (IT) – Hukuk – tech – 10 gün önce
        int id23 = (int) db.arizaDao().insert(new Ariza(
                "Hukuk Birimi Sunucu RAID Uyarısı",
                "Hukuk birimine ait dosya sunucusu RAID uyarısı veriyor, disk değişimi gerekli.",
                "IN_PROGRESS", String.valueOf(now - 10 * day),
                "client", "tech", "COK", "Hukuk", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id23, now - 10 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id23, now - 10 * day + 25 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id23, now - 9 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 24 – IN_PROGRESS: Ağ / İnternet – Genel Müdürlük – tech2 – 11 gün önce
        int id24 = (int) db.arizaDao().insert(new Ariza(
                "Genel Müdürlük Switch Aşırı Isınıyor",
                "Genel müdürlük katındaki ağ switch'i aşırı ısınıyor, portlar düşüyor.",
                "IN_PROGRESS", String.valueOf(now - 11 * day),
                "client2", "tech2", "COK", "Genel Müdürlük", "Ağ / İnternet", ""));
        db.arizaLogDao().insert(new ArizaLog(id24, now - 11 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id24, now - 11 * day + 50 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id24, now - 10 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 25 – IN_PROGRESS: Yazılım – Satış ve Pazarlama – tech2 – 12 gün önce
        int id25 = (int) db.arizaDao().insert(new Ariza(
                "CRM Yazılımı Senkronizasyon Hatası",
                "Satış ekibinin kullandığı CRM yazılımı bulutla senkronize olmuyor.",
                "IN_PROGRESS", String.valueOf(now - 12 * day),
                "client", "tech2", "ORTA", "Satış ve Pazarlama", "Yazılım", ""));
        db.arizaLogDao().insert(new ArizaLog(id25, now - 12 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id25, now - 12 * day + 45 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id25, now - 11 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 26 – IN_PROGRESS: Mekanik / İklimlendirme – Teknik Servis – tech – 9 gün önce
        int id26 = (int) db.arizaDao().insert(new Ariza(
                "Teknik Servis Egzoz Fanı Arızası",
                "Teknik servis alanının egzoz fanı dönmüyor, kimyasal koku birikiyor.",
                "IN_PROGRESS", String.valueOf(now - 9 * day),
                "client2", "tech", "COK", "Teknik Servis", "Mekanik / İklimlendirme", ""));
        db.arizaLogDao().insert(new ArizaLog(id26, now - 9 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id26, now - 9 * day + 20 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id26, now - 8 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 27 – IN_PROGRESS: Yangın / Güvenlik Sistemi – Sunucu Odası – tech – 8 gün önce
        int id27 = (int) db.arizaDao().insert(new Ariza(
                "Sunucu Odası CO2 Tüpü Basıncı Düşük",
                "Sunucu odasındaki CO2 yangın tüpünün basınç göstergesi kırmızı bölgede.",
                "IN_PROGRESS", String.valueOf(now - 8 * day),
                "client", "tech", "COK", "Sunucu Odası", "Yangın / Güvenlik Sistemi", ""));
        db.arizaLogDao().insert(new ArizaLog(id27, now - 8 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id27, now - 8 * day + 15 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id27, now - 7 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 28 – IN_PROGRESS: Yapısal – Muhasebe – tech2 – 13 gün önce
        int id28 = (int) db.arizaDao().insert(new Ariza(
                "Muhasebe Ofisi Döşeme Kaplaması Kalkıyor",
                "Muhasebe bölümündeki parke döşemenin bir bölümü kabarıp kalkıyor.",
                "IN_PROGRESS", String.valueOf(now - 13 * day),
                "client2", "tech2", "NORMAL", "Muhasebe", "Yapısal", ""));
        db.arizaLogDao().insert(new ArizaLog(id28, now - 13 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id28, now - 13 * day + 60 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id28, now - 12 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 29 – IN_PROGRESS: Asansör – Resepsiyon – tech2 – 10 gün önce
        int id29 = (int) db.arizaDao().insert(new Ariza(
                "Asansör Kapı Sensörü Hatası",
                "Resepsiyon yanındaki asansör kapısı düzgün kapanmıyor, sürekli açılıp kapanıyor.",
                "IN_PROGRESS", String.valueOf(now - 10 * day),
                "client", "tech2", "COK", "Resepsiyon", "Asansör", ""));
        db.arizaLogDao().insert(new ArizaLog(id29, now - 10 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id29, now - 10 * day + 30 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id29, now - 9 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // 30 – IN_PROGRESS: Elektrik – İnsan Kaynakları – tech – 11 gün önce
        int id30 = (int) db.arizaDao().insert(new Ariza(
                "İK Ofisi Sigorta Sürekli Atıyor",
                "İnsan kaynakları katının elektrik sigortası gün içinde birkaç kez atıyor.",
                "IN_PROGRESS", String.valueOf(now - 11 * day),
                "client2", "tech", "ORTA", "İnsan Kaynakları", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id30, now - 11 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id30, now - 11 * day + 55 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id30, now - 10 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));

        // ── 10 CLOSED – 31-35 son 7 günde kapandı (haftalık istat.), 36-40 eski ──

        // 31 – CLOSED: Ağ – Bilgi Teknolojileri – tech – açıldı 14g, kapandı 6g önce
        int id31 = (int) db.arizaDao().insert(new Ariza(
                "Firewall Kural Hatası",
                "Bilgi teknolojileri güvenlik duvarı yanlış kural nedeniyle iç ağı bloke ediyordu.",
                "CLOSED", String.valueOf(now - 14 * day),
                "client", "tech", "COK", "Bilgi Teknolojileri", "Ağ / İnternet", ""));
        db.arizaLogDao().insert(new ArizaLog(id31, now - 14 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id31, now - 14 * day + 20 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id31, now - 13 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id31, now - 6 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 32 – CLOSED: Elektrik – Hukuk – tech2 – açıldı 15g, kapandı 5g önce
        int id32 = (int) db.arizaDao().insert(new Ariza(
                "Hukuk Ofisi Aydınlatma Titreşiyor",
                "Hukuk birimi aydınlatma armatürleri titreşiyor ve sönüp yanıyor.",
                "CLOSED", String.valueOf(now - 15 * day),
                "client2", "tech2", "ORTA", "Hukuk", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id32, now - 15 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id32, now - 15 * day + 30 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id32, now - 14 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id32, now - 5 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 33 – CLOSED: Yazılım – Arşiv – tech – açıldı 12g, kapandı 4g önce
        int id33 = (int) db.arizaDao().insert(new Ariza(
                "Arşiv Tarama Yazılımı PDF Hatası",
                "Belge tarama yazılımı büyük dosyaları PDF'e dönüştüremiyordu.",
                "CLOSED", String.valueOf(now - 12 * day),
                "client", "tech", "NORMAL", "Arşiv", "Yazılım", ""));
        db.arizaLogDao().insert(new ArizaLog(id33, now - 12 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id33, now - 12 * day + 40 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id33, now - 11 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id33, now - 4 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 34 – CLOSED: Tesisat – Yemekhane – tech2 – açıldı 13g, kapandı 3g önce
        int id34 = (int) db.arizaDao().insert(new Ariza(
                "Yemekhane Lavabo Tahliyesi Tıkalı",
                "Yemekhane lavabosunun tahliye hattı tamamen tıkandı, su geri geliyor.",
                "CLOSED", String.valueOf(now - 13 * day),
                "client2", "tech2", "COK", "Yemekhane", "Tesisat", ""));
        db.arizaLogDao().insert(new ArizaLog(id34, now - 13 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id34, now - 13 * day + 25 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id34, now - 12 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id34, now - 3 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 35 – CLOSED: Donanım (IT) – Depo – tech – açıldı 11g, kapandı 2g önce
        int id35 = (int) db.arizaDao().insert(new Ariza(
                "Depo Barkod Okuyucu Arızası",
                "Depo yönetim sistemine bağlı barkod okuyucu USB portu tanımıyordu.",
                "CLOSED", String.valueOf(now - 11 * day),
                "client", "tech", "ORTA", "Depo", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id35, now - 11 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id35, now - 11 * day + 50 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id35, now - 10 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id35, now - 2 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 36 – CLOSED (eski): Yangın / Güvenlik – Güvenlik – tech2 – kapandı 14g önce
        int id36 = (int) db.arizaDao().insert(new Ariza(
                "Güvenlik DVR Disk Arızası",
                "Güvenlik DVR cihazı sabit diski bozuldu, kayıt yapılamıyordu.",
                "CLOSED", String.valueOf(now - 20 * day),
                "client2", "tech2", "COK", "Güvenlik", "Yangın / Güvenlik Sistemi", ""));
        db.arizaLogDao().insert(new ArizaLog(id36, now - 20 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id36, now - 20 * day + 60 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id36, now - 19 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id36, now - 14 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 37 – CLOSED (eski): Mekanik – Toplantı Odası – tech – kapandı 15g önce
        int id37 = (int) db.arizaDao().insert(new Ariza(
                "Toplantı Odası Fan Coil Su Sızıntısı",
                "Büyük toplantı odasının fan coil ünitesi su sızdırmaya başladı.",
                "CLOSED", String.valueOf(now - 22 * day),
                "client", "tech", "COK", "Toplantı Odası", "Mekanik / İklimlendirme", ""));
        db.arizaLogDao().insert(new ArizaLog(id37, now - 22 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id37, now - 22 * day + 30 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id37, now - 21 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id37, now - 15 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 38 – CLOSED (eski): Elektrik – Genel Müdürlük – tech2 – kapandı 10g önce
        int id38 = (int) db.arizaDao().insert(new Ariza(
                "Genel Müdürlük Acil Aydınlatma Arızası",
                "Genel müdürlük acil aydınlatma armatürleri bakım süresini geçirmiş, devreye girmiyor.",
                "CLOSED", String.valueOf(now - 18 * day),
                "client2", "tech2", "ORTA", "Genel Müdürlük", "Elektrik", ""));
        db.arizaLogDao().insert(new ArizaLog(id38, now - 18 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id38, now - 18 * day + 45 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id38, now - 17 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id38, now - 10 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 39 – CLOSED (eski): Yapısal – Muhasebe – tech – kapandı 12g önce
        int id39 = (int) db.arizaDao().insert(new Ariza(
                "Muhasebe Cam Bölme Çatlağı",
                "Muhasebe birimi ofis cam bölmesinde derin çatlak oluştu, güvenlik riski var.",
                "CLOSED", String.valueOf(now - 25 * day),
                "client", "tech", "ORTA", "Muhasebe", "Yapısal", ""));
        db.arizaLogDao().insert(new ArizaLog(id39, now - 25 * day, "client", "client arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id39, now - 25 * day + 35 * 60 * 1000, "admin", "Arıza tech kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id39, now - 24 * day, "tech", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id39, now - 12 * day, "tech", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));

        // 40 – CLOSED (eski): Donanım (IT) – Sunucu Odası – tech2 – kapandı 11g önce
        int id40 = (int) db.arizaDao().insert(new Ariza(
                "Sunucu Odası Kart Okuyucu Arızası",
                "Sunucu odasına girişte kullanılan akıllı kart okuyucu tepki vermiyordu.",
                "CLOSED", String.valueOf(now - 19 * day),
                "client2", "tech2", "COK", "Sunucu Odası", "Donanım (IT)", ""));
        db.arizaLogDao().insert(new ArizaLog(id40, now - 19 * day, "client2", "client2 arızayı açtı."));
        db.arizaLogDao().insert(new ArizaLog(id40, now - 19 * day + 20 * 60 * 1000, "admin", "Arıza tech2 kişisine atandı."));
        db.arizaLogDao().insert(new ArizaLog(id40, now - 18 * day, "tech2", "Durum \"Beklemede\" → \"İşlemde\" olarak değiştirildi."));
        db.arizaLogDao().insert(new ArizaLog(id40, now - 11 * day, "tech2", "Durum \"İşlemde\" → \"Çözüldü\" olarak değiştirildi."));
    }
}

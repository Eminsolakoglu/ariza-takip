package com.arizatakip.system.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "arizalar")
public class Ariza {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String baslik;
    private String aciklama;
    private String durum;
    private String tarih;
    private String atananKisi;
    private String olusturanKisi;
    private String aciliyetDerecesi;
    private String konum;
    private String kategori;
    private String gorseller;

    public Ariza(String baslik, String aciklama, String durum, String tarih, String olusturanKisi, String atananKisi, String aciliyetDerecesi, String konum, String kategori, String gorseller) {
        this.baslik = baslik;
        this.aciklama = aciklama;
        this.durum = durum;
        this.tarih = tarih;
        this.olusturanKisi = olusturanKisi;
        this.atananKisi = atananKisi;
        this.aciliyetDerecesi = aciliyetDerecesi;
        this.konum = konum;
        this.kategori = kategori;
        this.gorseller = gorseller;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAtananKisi() { return atananKisi; }
    public void setAtananKisi(String atananKisi) { this.atananKisi = atananKisi; }
    public String getBaslik() { return baslik; }
    public String getAciklama() { return aciklama; }
    public String getDurum() { return durum; }
    public String getTarih() { return tarih; }
    public String getOlusturanKisi() { return olusturanKisi; }
    public void setOlusturanKisi(String olusturanKisi) { this.olusturanKisi = olusturanKisi; }
    public String getAciliyetDerecesi() { return aciliyetDerecesi; }
    public void setAciliyetDerecesi(String aciliyetDerecesi) { this.aciliyetDerecesi = aciliyetDerecesi; }
    public String getKonum() { return konum; }
    public void setKonum(String konum) { this.konum = konum; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public String getGorseller() { return gorseller; }
    public void setGorseller(String gorseller) { this.gorseller = gorseller; }
}
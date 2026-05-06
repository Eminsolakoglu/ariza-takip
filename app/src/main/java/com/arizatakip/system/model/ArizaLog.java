package com.arizatakip.system.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ariza_logs")
public class ArizaLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int arizaId;
    private long tarih;
    private String kullanici;
    private String detay;

    public ArizaLog(int arizaId, long tarih, String kullanici, String detay) {
        this.arizaId = arizaId;
        this.tarih = tarih;
        this.kullanici = kullanici;
        this.detay = detay;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getArizaId() { return arizaId; }
    public void setArizaId(int arizaId) { this.arizaId = arizaId; }
    public long getTarih() { return tarih; }
    public void setTarih(long tarih) { this.tarih = tarih; }
    public String getKullanici() { return kullanici; }
    public void setKullanici(String kullanici) { this.kullanici = kullanici; }
    public String getDetay() { return detay; }
    public void setDetay(String detay) { this.detay = detay; }
}

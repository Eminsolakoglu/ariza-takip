package com.arizatakip.system.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.arizatakip.system.model.Ariza;

import java.util.List;

@Dao
public interface ArizaDao {

    @Insert
    long insert(Ariza ariza);

    @Query("SELECT * FROM arizalar ORDER BY id DESC")
    List<Ariza> getAll();

    @Query("UPDATE arizalar SET durum = :durum WHERE id = :id")
    void updateDurum(int id, String durum);
    // Teknikere atananları getir
    @Query("SELECT * FROM arizalar WHERE olusturanKisi = :kisi ORDER BY id DESC")
    List<Ariza> getByKullanici(String kisi);
    @Query("SELECT * FROM arizalar WHERE atananKisi = :tech ORDER BY id DESC")
    List<Ariza> getByAtananKisi(String tech);

    // Arızaya tekniker ata
    @Query("UPDATE arizalar SET atananKisi = :tech WHERE id = :id")
    void updateAtananKisi(int id, String tech);
    // Arızayı ID'sine göre kalıcı olarak siler
    @Query("DELETE FROM arizalar WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM arizalar WHERE id = :id LIMIT 1")
    Ariza getById(int id);

    @Query("UPDATE arizalar SET baslik = :baslik, aciklama = :aciklama, aciliyetDerecesi = :aciliyetDerecesi, konum = :konum, kategori = :kategori WHERE id = :id")
    void updateAriza(int id, String baslik, String aciklama, String aciliyetDerecesi, String konum, String kategori);

    @Query("UPDATE arizalar SET gorseller = :gorseller WHERE id = :id")
    void updateGorseller(int id, String gorseller);

    @Query("SELECT COUNT(*) FROM arizalar")
    int getCount();
}
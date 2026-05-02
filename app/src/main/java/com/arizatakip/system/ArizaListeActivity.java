package com.arizatakip.system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arizatakip.system.adapter.ArizaAdapter;
import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.model.Ariza;

import java.util.ArrayList;
import java.util.List;

public class ArizaListeActivity extends AppCompatActivity {

    private ArizaAdapter adapter;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ariza_liste);

        spinnerFilter = findViewById(R.id.spinnerFilter);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button btnHome = findViewById(R.id.btnHome);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALL", "OPEN", "IN_PROGRESS", "CLOSED"}
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);
        btnHome.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ArizaAdapter(new ArrayList<>(), ariza -> {
            Intent intent = new Intent(this, ArizaDetayActivity.class);
            intent.putExtra("id", ariza.getId());
            intent.putExtra("baslik", ariza.getBaslik());
            intent.putExtra("aciklama", ariza.getAciklama());
            intent.putExtra("durum", ariza.getDurum());
            intent.putExtra("tarih", ariza.getTarih());
            intent.putExtra("olusturan", ariza.getOlusturanKisi());
            intent.putExtra("atanan", ariza.getAtananKisi());
            intent.putExtra("aciliyet", ariza.getAciliyetDerecesi());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // 🔥 FILTER
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Seçim değiştiğinde listeyi güncelleyen metodu çağırıyoruz
                listeyiGuncelle();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listeyiGuncelle();
    }

    private void listeyiGuncelle() {
        if (spinnerFilter == null || adapter == null) return;

        String selected = spinnerFilter.getSelectedItem().toString();

        List<Ariza> all = ArizaRepository.getAll();
        if (all == null) all = new ArrayList<>();
        List<Ariza> filtered = new ArrayList<>();

        for (Ariza a : all) {
            String durum = a.getDurum() != null ? a.getDurum().toUpperCase() : "";

            if (selected.equals("ALL") || durum.equals(selected)) {
                filtered.add(a);
            }
        }

        adapter.updateList(filtered);
    }
}
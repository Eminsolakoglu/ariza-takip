package com.arizatakip.system.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arizatakip.system.R;
import com.arizatakip.system.model.Ariza;

import java.util.ArrayList;
import java.util.List;

public class ArizaAdapter extends RecyclerView.Adapter<ArizaAdapter.ViewHolder> {

    private List<Ariza> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Ariza ariza);
    }

    public ArizaAdapter(List<Ariza> list, OnItemClickListener listener) {
        this.list = (list != null) ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Ariza> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBaslik, tvAciklama;
        View viewStatusIndicator; // YENİ: Renk çizgisini tanımladık

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBaslik = itemView.findViewById(R.id.tvBaslik);
            tvAciklama = itemView.findViewById(R.id.tvAciklama);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator); // YENİ: ID'yi eşleştirdik
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ariza, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Ariza ariza = list.get(position);

        holder.tvBaslik.setText(ariza.getBaslik());
        holder.tvAciklama.setText(ariza.getAciklama());

        String durum = (ariza.getDurum() != null) ? ariza.getDurum().toUpperCase() : "";

        switch (durum) {
            case "OPEN":
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#F57C00"));
                break;
            case "IN_PROGRESS":
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#FBC02D"));
                break;
            case "CLOSED":
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#388E3C"));
                break;
            default:
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#F57C00"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(ariza);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
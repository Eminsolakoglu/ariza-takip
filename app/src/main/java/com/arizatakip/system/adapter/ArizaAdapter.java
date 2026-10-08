package com.arizatakip.system.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
        TextView tvBaslik, tvAciklama, tvAciliyet;
        View viewStatusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBaslik = itemView.findViewById(R.id.tvBaslik);
            tvAciklama = itemView.findViewById(R.id.tvAciklama);
            tvAciliyet = itemView.findViewById(R.id.tvAciliyet);
            viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
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
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#F9A825"));
                break;
            case "CLOSED":
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#388E3C"));
                break;
            default:
                holder.viewStatusIndicator.setBackgroundColor(Color.parseColor("#F57C00"));
                break;
        }

        String aciliyet = (ariza.getAciliyetDerecesi() != null) ? ariza.getAciliyetDerecesi().toUpperCase() : "NORMAL";
        String aciliyetLabel;
        int badgeBgColor;
        int badgeTextColor;

        switch (aciliyet) {
            case "ORTA":
                aciliyetLabel = "Orta";
                badgeBgColor = Color.parseColor("#E3EAF6");
                badgeTextColor = Color.parseColor("#2E6DC4");
                break;
            case "COK":
                aciliyetLabel = "Çok Acil";
                badgeBgColor = Color.parseColor("#EBF2FC");
                badgeTextColor = Color.parseColor("#0F3D7A");
                break;
            default:
                aciliyetLabel = "Normal";
                badgeBgColor = Color.parseColor("#F0F3F7");
                badgeTextColor = Color.parseColor("#4A5568");
                break;
        }

        holder.tvAciliyet.setText(aciliyetLabel);
        holder.tvAciliyet.setTextColor(badgeTextColor);
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.RECTANGLE);
        badge.setCornerRadius(32f);
        badge.setColor(badgeBgColor);
        holder.tvAciliyet.setBackground(badge);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(ariza);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

package com.arizatakip.system;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arizatakip.system.data.ArizaLogRepository;
import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.model.Ariza;
import com.arizatakip.system.model.ArizaLog;
import com.arizatakip.system.utils.SessionManager;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private HorizontalBarChart barChart;
    private MaterialCardView cardBar, cardResolution;
    private TextView tvStatOpen, tvStatInProgress, tvStatClosed;
    private TextView tvAvgAge, tvWeeklyResolution, tvEmptyPie, tvEmptyBar;

    private SessionManager sessionManager;
    private String role;

    private static final int[] CHART_COLORS = {
        Color.parseColor("#1A56A0"),
        Color.parseColor("#2E6DC4"),
        Color.parseColor("#0F3D7A"),
        Color.parseColor("#4A8CD5"),
        Color.parseColor("#6BA3E3"),
        Color.parseColor("#5C7E9B"),
        Color.parseColor("#3D7AB5"),
        Color.parseColor("#1E9DB5"),
        Color.parseColor("#8896A8"),
        Color.parseColor("#2A4A6E"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        role = sessionManager.getRole();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        cardBar = findViewById(R.id.cardBar);
        cardResolution = findViewById(R.id.cardResolution);
        tvStatOpen = findViewById(R.id.tvStatOpen);
        tvStatInProgress = findViewById(R.id.tvStatInProgress);
        tvStatClosed = findViewById(R.id.tvStatClosed);
        tvAvgAge = findViewById(R.id.tvAvgAge);
        tvWeeklyResolution = findViewById(R.id.tvWeeklyResolution);
        tvEmptyPie = findViewById(R.id.tvEmptyPie);
        tvEmptyBar = findViewById(R.id.tvEmptyBar);

        if ("admin".equals(role)) {
            cardBar.setVisibility(View.VISIBLE);
            cardResolution.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Ariza> arizalar = "tech".equals(role)
                ? ArizaRepository.getByAtananKisi(sessionManager.getUsername())
                : ArizaRepository.getAll();
        if (arizalar == null) arizalar = new ArrayList<>();

        // Özet sayaçlar
        int open = 0, inProgress = 0, closed = 0;
        for (Ariza a : arizalar) {
            if ("OPEN".equals(a.getDurum())) open++;
            else if ("IN_PROGRESS".equals(a.getDurum())) inProgress++;
            else if ("CLOSED".equals(a.getDurum())) closed++;
        }
        tvStatOpen.setText(String.valueOf(open));
        tvStatInProgress.setText(String.valueOf(inProgress));
        tvStatClosed.setText(String.valueOf(closed));

        // Ortalama bekleme süresi (açık + işlemdeki arızaların yaşı)
        long totalAgeMs = 0;
        int activeCount = 0;
        long now = System.currentTimeMillis();
        for (Ariza a : arizalar) {
            if ("OPEN".equals(a.getDurum()) || "IN_PROGRESS".equals(a.getDurum())) {
                try {
                    totalAgeMs += now - Long.parseLong(a.getTarih());
                    activeCount++;
                } catch (NumberFormatException ignored) {}
            }
        }
        tvAvgAge.setText(activeCount > 0 ? formatSure(totalAgeMs / activeCount) : "—");

        drawPieChart(arizalar);

        if ("admin".equals(role)) {
            drawBarChart(arizalar);
            computeWeeklyResolution();
        }
    }

    // ── Pasta Grafiği ─────────────────────────────────────────────────────────

    private void drawPieChart(List<Ariza> arizalar) {
        Map<String, Integer> catCount = new LinkedHashMap<>();
        for (Ariza a : arizalar) {
            if ("OPEN".equals(a.getDurum())) {
                String kat = (a.getKategori() != null && !a.getKategori().isEmpty())
                        ? a.getKategori() : "Diğer";
                catCount.put(kat, catCount.getOrDefault(kat, 0) + 1);
            }
        }

        if (catCount.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvEmptyPie.setVisibility(View.VISIBLE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);
        tvEmptyPie.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int colorIdx = 0;
        for (Map.Entry<String, Integer> e : catCount.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
            colors.add(CHART_COLORS[colorIdx % CHART_COLORS.length]);
            colorIdx++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "";
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(36f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setExtraRightOffset(24f);

        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setDrawInside(false);
        legend.setTextSize(11f);
        legend.setTextColor(Color.parseColor("#4A5568"));
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10f);
        legend.setXEntrySpace(8f);
        legend.setYEntrySpace(4f);

        pieChart.animateY(900);
        pieChart.invalidate();
    }

    // ── Çubuk Grafiği ─────────────────────────────────────────────────────────

    private void drawBarChart(List<Ariza> arizalar) {
        Map<String, Integer> techCount = new LinkedHashMap<>();
        for (Ariza a : arizalar) {
            if ("OPEN".equals(a.getDurum()) || "IN_PROGRESS".equals(a.getDurum())) {
                String tech = a.getAtananKisi();
                if (tech != null && !tech.isEmpty() && !"Atanmadı".equals(tech)) {
                    techCount.put(tech, techCount.getOrDefault(tech, 0) + 1);
                }
            }
        }

        if (techCount.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvEmptyBar.setVisibility(View.VISIBLE);
            return;
        }
        barChart.setVisibility(View.VISIBLE);
        tvEmptyBar.setVisibility(View.GONE);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : techCount.entrySet()) {
            entries.add(new BarEntry(idx, e.getValue()));
            labels.add(e.getKey());
            colors.add(CHART_COLORS[idx % CHART_COLORS.length]);
            idx++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.parseColor("#1A2332"));
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + " iş";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.55f);
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setHighlightPerTapEnabled(false);

        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#4A5568"));
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F3F7"));
        leftAxis.setTextColor(Color.parseColor("#8896A8"));
        leftAxis.setTextSize(10f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Yüksekliği teknisyen sayısına göre ayarla
        int barHeightPx = Math.max(dp(160), labels.size() * dp(56));
        barChart.getLayoutParams().height = barHeightPx;
        barChart.requestLayout();

        barChart.animateY(700);
        barChart.invalidate();
    }

    // ── Haftalık Ortalama Çözüm Süresi ────────────────────────────────────────

    private void computeWeeklyResolution() {
        long sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        List<ArizaLog> recentLogs = ArizaLogRepository.getLogsSince(sevenDaysAgo);

        long totalMs = 0;
        int count = 0;

        for (ArizaLog log : recentLogs) {
            if (log.getDetay() != null && log.getDetay().contains("Çözüldü")) {
                Ariza a = ArizaRepository.getById(log.getArizaId());
                if (a != null) {
                    try {
                        long openMs = Long.parseLong(a.getTarih());
                        long closeMs = log.getTarih();
                        if (closeMs > openMs) {
                            totalMs += (closeMs - openMs);
                            count++;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (count > 0) {
            tvWeeklyResolution.setText(
                    formatSure(totalMs / count) + "\n(son 7 günde " + count + " arıza çözüldü)");
        } else {
            tvWeeklyResolution.setText("Son 7 günde kapatılan arıza bulunmuyor.");
        }
    }

    // ── Yardımcılar ───────────────────────────────────────────────────────────

    private String formatSure(long ms) {
        long totalMin = ms / (1000 * 60);
        long totalHour = totalMin / 60;
        long days = totalHour / 24;
        long remHour = totalHour % 24;
        long remMin = totalMin % 60;

        if (days > 0) {
            return days + " gün " + remHour + " saat";
        } else if (totalHour > 0) {
            return totalHour + " saat " + remMin + " dk";
        } else {
            return totalMin > 0 ? totalMin + " dakika" : "< 1 dakika";
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

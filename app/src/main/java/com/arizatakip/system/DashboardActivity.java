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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChartCategory;
    private PieChart pieChartTech;
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

        pieChartCategory = findViewById(R.id.pieChart);
        pieChartTech     = findViewById(R.id.pieChartTech);
        cardBar          = findViewById(R.id.cardBar);
        cardResolution   = findViewById(R.id.cardResolution);
        tvStatOpen       = findViewById(R.id.tvStatOpen);
        tvStatInProgress = findViewById(R.id.tvStatInProgress);
        tvStatClosed     = findViewById(R.id.tvStatClosed);
        tvAvgAge         = findViewById(R.id.tvAvgAge);
        tvWeeklyResolution = findViewById(R.id.tvWeeklyResolution);
        tvEmptyPie       = findViewById(R.id.tvEmptyPie);
        tvEmptyBar       = findViewById(R.id.tvEmptyBar);

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

        drawCategoryPie(arizalar);

        if ("admin".equals(role)) {
            drawTechPie(arizalar);
            computeWeeklyResolution();
        }
    }

    // ── Kategori Pasta Grafiği ────────────────────────────────────────────────

    private void drawCategoryPie(List<Ariza> arizalar) {
        Map<String, Integer> catCount = new LinkedHashMap<>();
        for (Ariza a : arizalar) {
            if ("OPEN".equals(a.getDurum())) {
                String kat = (a.getKategori() != null && !a.getKategori().isEmpty())
                        ? a.getKategori() : "Diğer";
                catCount.put(kat, catCount.getOrDefault(kat, 0) + 1);
            }
        }

        if (catCount.isEmpty()) {
            pieChartCategory.setVisibility(View.GONE);
            tvEmptyPie.setVisibility(View.VISIBLE);
            return;
        }
        pieChartCategory.setVisibility(View.VISIBLE);
        tvEmptyPie.setVisibility(View.GONE);

        applyPieChart(pieChartCategory, catCount, "Bekleyen");
    }

    // ── Teknisyen İş Yükü Pasta Grafiği ──────────────────────────────────────

    private void drawTechPie(List<Ariza> arizalar) {
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
            pieChartTech.setVisibility(View.GONE);
            tvEmptyBar.setVisibility(View.VISIBLE);
            return;
        }
        pieChartTech.setVisibility(View.VISIBLE);
        tvEmptyBar.setVisibility(View.GONE);

        applyPieChart(pieChartTech, techCount, "Aktif İş");
    }

    // ── Ortak Pasta Grafiği Çizici ────────────────────────────────────────────

    private void applyPieChart(PieChart chart, Map<String, Integer> data, String centerLabel) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        int colorIdx = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
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
        chart.setData(pieData);
        chart.setUsePercentValues(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(42f);
        chart.setTransparentCircleRadius(46f);
        chart.setHoleColor(Color.WHITE);
        chart.setCenterText(centerLabel);
        chart.setCenterTextSize(13f);
        chart.setCenterTextColor(Color.parseColor("#8896A8"));
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setExtraRightOffset(20f);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        Legend legend = chart.getLegend();
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
        legend.setYEntrySpace(5f);

        chart.animateY(900);
        chart.invalidate();
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
                        long openMs  = Long.parseLong(a.getTarih());
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
        long totalMin  = ms / (1000 * 60);
        long totalHour = totalMin / 60;
        long days      = totalHour / 24;
        long remHour   = totalHour % 24;
        long remMin    = totalMin % 60;

        if (days > 0) return days + " gün " + remHour + " saat";
        if (totalHour > 0) return totalHour + " saat " + remMin + " dk";
        return totalMin > 0 ? totalMin + " dakika" : "< 1 dakika";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

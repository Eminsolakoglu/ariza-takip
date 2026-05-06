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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private PieChart pieChart;
    private HorizontalBarChart barChart;
    private LineChart lineChart;
    private MaterialCardView cardBar, cardLine, cardResolution;
    private TextView tvStatOpen, tvStatInProgress, tvStatClosed;
    private TextView tvAvgAge, tvWeeklyResolution, tvEmptyPie, tvEmptyBar;

    private SessionManager sessionManager;
    private String role;

    // Birbirinden kolayca ayırt edilebilir renk paleti
    private static final int[] PIE_COLORS = {
        Color.parseColor("#3B82F6"),  // mavi
        Color.parseColor("#EF4444"),  // kırmızı
        Color.parseColor("#10B981"),  // yeşil
        Color.parseColor("#F59E0B"),  // amber
        Color.parseColor("#8B5CF6"),  // mor
        Color.parseColor("#06B6D4"),  // cyan
        Color.parseColor("#F97316"),  // turuncu
        Color.parseColor("#EC4899"),  // pembe
        Color.parseColor("#14B8A6"),  // teal
        Color.parseColor("#6366F1"),  // indigo
    };

    // Bar chart için bireysel, belirgin renkler
    private static final int[] BAR_COLORS = {
        Color.parseColor("#3B82F6"),  // mavi
        Color.parseColor("#EF4444"),  // kırmızı
        Color.parseColor("#10B981"),  // yeşil
        Color.parseColor("#F59E0B"),  // amber
        Color.parseColor("#8B5CF6"),  // mor
        Color.parseColor("#F97316"),  // turuncu
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        role = sessionManager.getRole();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        pieChart        = findViewById(R.id.pieChart);
        barChart        = findViewById(R.id.barChart);
        lineChart       = findViewById(R.id.lineChart);
        cardBar         = findViewById(R.id.cardBar);
        cardLine        = findViewById(R.id.cardLine);
        cardResolution  = findViewById(R.id.cardResolution);
        tvStatOpen      = findViewById(R.id.tvStatOpen);
        tvStatInProgress= findViewById(R.id.tvStatInProgress);
        tvStatClosed    = findViewById(R.id.tvStatClosed);
        tvAvgAge        = findViewById(R.id.tvAvgAge);
        tvWeeklyResolution = findViewById(R.id.tvWeeklyResolution);
        tvEmptyPie      = findViewById(R.id.tvEmptyPie);
        tvEmptyBar      = findViewById(R.id.tvEmptyBar);

        if ("admin".equals(role)) {
            cardBar.setVisibility(View.VISIBLE);
            cardLine.setVisibility(View.VISIBLE);
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

        // Ortalama bekleme süresi
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
            drawTechBar(arizalar);
            drawWeeklyLine(arizalar);
            computeWeeklyResolution();
        }
    }

    // ── Pasta Grafiği: Kategori Dağılımı ─────────────────────────────────────

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
            pieChart.setVisibility(View.GONE);
            tvEmptyPie.setVisibility(View.VISIBLE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);
        tvEmptyPie.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : catCount.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
            colors.add(PIE_COLORS[idx % PIE_COLORS.length]);
            idx++;
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setSliceSpace(2f);
        ds.setSelectionShift(5f);
        ds.setValueTextSize(12f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return (int) v + ""; }
        });

        pieChart.setData(new PieData(ds));
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(44f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText("Bekleyen");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(Color.parseColor("#8896A8"));
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setExtraRightOffset(20f);
        disableDescription(pieChart);
        styleLegend(pieChart.getLegend());

        pieChart.animateY(900);
        pieChart.invalidate();
    }

    // ── Yatay Çubuk Grafiği: Teknisyen İş Yükü ───────────────────────────────

    private void drawTechBar(List<Ariza> arizalar) {
        // Her teknisyen için aktif (OPEN+IN_PROGRESS) iş sayısı
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

        List<BarEntry>  entries = new ArrayList<>();
        List<String>    labels  = new ArrayList<>();
        List<Integer>   colors  = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : techCount.entrySet()) {
            entries.add(new BarEntry(idx, e.getValue()));
            labels.add(e.getKey());
            colors.add(BAR_COLORS[idx % BAR_COLORS.length]);
            idx++;
        }

        BarDataSet ds = new BarDataSet(entries, "");
        ds.setColors(colors);
        ds.setValueTextColor(Color.parseColor("#1A2332"));
        ds.setValueTextSize(11f);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return (int) v + " iş"; }
        });

        BarData barData = new BarData(ds);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setHighlightPerTapEnabled(false);
        disableDescription(barChart);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#1A2332"));
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis left = barChart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setGranularity(1f);
        left.setDrawGridLines(true);
        left.setGridColor(Color.parseColor("#F0F3F7"));
        left.setTextColor(Color.parseColor("#8896A8"));

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Yüksekliği teknisyen sayısına göre ayarla (min 160dp, her teknisyen için +52dp)
        barChart.getLayoutParams().height = Math.max(dp(160), labels.size() * dp(60));
        barChart.requestLayout();

        barChart.animateY(700);
        barChart.invalidate();
    }

    // ── Çizgi Grafik: Son 7 Günlük Trend ─────────────────────────────────────

    private void drawWeeklyLine(List<Ariza> arizalar) {
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        float[] openedPerDay = new float[7];
        float[] closedPerDay = new float[7];

        // Açılan: Ariza.tarih'e göre
        for (Ariza a : arizalar) {
            try {
                long tarihMs = Long.parseLong(a.getTarih());
                int daysAgo = (int) ((now - tarihMs) / day);
                if (daysAgo >= 0 && daysAgo < 7) {
                    openedPerDay[6 - daysAgo]++;
                }
            } catch (NumberFormatException ignored) {}
        }

        // Çözülen: Audit log girişlerine göre
        List<ArizaLog> recentLogs = ArizaLogRepository.getLogsSince(now - 7 * day);
        for (ArizaLog log : recentLogs) {
            if (log.getDetay() != null && log.getDetay().contains("Çözüldü")) {
                int daysAgo = (int) ((now - log.getTarih()) / day);
                if (daysAgo >= 0 && daysAgo < 7) {
                    closedPerDay[6 - daysAgo]++;
                }
            }
        }

        // X ekseni etiketleri (gün/ay formatında)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", new Locale("tr", "TR"));
        String[] xLabels = new String[7];
        for (int i = 6; i >= 0; i--) {
            xLabels[6 - i] = sdf.format(new Date(now - (long) i * day));
        }

        List<Entry> openedEntries = new ArrayList<>();
        List<Entry> closedEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            openedEntries.add(new Entry(i, openedPerDay[i]));
            closedEntries.add(new Entry(i, closedPerDay[i]));
        }

        LineDataSet openedSet = new LineDataSet(openedEntries, "Açılan");
        openedSet.setColor(Color.parseColor("#3B82F6"));
        openedSet.setCircleColor(Color.parseColor("#3B82F6"));
        openedSet.setCircleHoleColor(Color.WHITE);
        openedSet.setLineWidth(2.5f);
        openedSet.setCircleRadius(5f);
        openedSet.setDrawValues(true);
        openedSet.setValueTextSize(10f);
        openedSet.setValueTextColor(Color.parseColor("#1A2332"));
        openedSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        openedSet.setDrawFilled(true);
        openedSet.setFillColor(Color.parseColor("#3B82F6"));
        openedSet.setFillAlpha(25);
        openedSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v == 0 ? "" : String.valueOf((int) v);
            }
        });

        LineDataSet closedSet = new LineDataSet(closedEntries, "Çözülen");
        closedSet.setColor(Color.parseColor("#10B981"));
        closedSet.setCircleColor(Color.parseColor("#10B981"));
        closedSet.setCircleHoleColor(Color.WHITE);
        closedSet.setLineWidth(2.5f);
        closedSet.setCircleRadius(5f);
        closedSet.setDrawValues(true);
        closedSet.setValueTextSize(10f);
        closedSet.setValueTextColor(Color.parseColor("#1A2332"));
        closedSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        closedSet.setDrawFilled(true);
        closedSet.setFillColor(Color.parseColor("#10B981"));
        closedSet.setFillAlpha(25);
        closedSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v == 0 ? "" : String.valueOf((int) v);
            }
        });

        lineChart.setData(new LineData(openedSet, closedSet));
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setHighlightPerTapEnabled(false);
        disableDescription(lineChart);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#4A5568"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F3F7"));
        leftAxis.setTextColor(Color.parseColor("#8896A8"));
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(11f);
        legend.setTextColor(Color.parseColor("#4A5568"));
        legend.setForm(Legend.LegendForm.LINE);

        lineChart.animateX(900);
        lineChart.invalidate();
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

    private void disableDescription(com.github.mikephil.charting.charts.Chart<?> chart) {
        Description d = new Description();
        d.setText("");
        chart.setDescription(d);
    }

    private void styleLegend(Legend legend) {
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
    }

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

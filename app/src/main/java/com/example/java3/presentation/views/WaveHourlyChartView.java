package com.example.java3.presentation.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.java3.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WaveHourlyChartView extends View {
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final RectF chartBounds = new RectF();
    private final RectF bubbleBounds = new RectF();

    private final List<Float> waveHeights = new ArrayList<>();
    private final List<String> timeLabels = new ArrayList<>();
    private int currentIndex = -1;
    private int maxIndex = -1;

    public WaveHourlyChartView(Context context) {
        super(context);
        init();
    }

    public WaveHourlyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint.setColor(Color.parseColor("#D8E2EC"));
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.parseColor("#94A3B8"));
        axisPaint.setStrokeWidth(dp(1));
        axisPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setColor(Color.parseColor("#2B1565C0"));
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));

        bubblePaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        bubblePaint.setStyle(Paint.Style.FILL);

        bubbleTextPaint.setColor(Color.WHITE);
        bubbleTextPaint.setTextSize(sp(10));
        bubbleTextPaint.setFakeBoldText(true);
        bubbleTextPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        textPaint.setTextSize(sp(10));
    }

    public void setWaveHeights(List<Float> values, int currentIndex) {
        setWaveData(values, null, currentIndex);
    }

    public void setWaveData(List<Float> values, List<String> labels, int currentIndex) {
        waveHeights.clear();
        timeLabels.clear();
        if (values != null) {
            int limit = Math.min(24, values.size());
            for (int i = 0; i < limit; i++) {
                Float value = values.get(i);
                waveHeights.add(value != null ? Math.max(0f, value) : 0f);
            }
        }
        if (labels != null) {
            int limit = Math.min(waveHeights.size(), labels.size());
            for (int i = 0; i < limit; i++) {
                timeLabels.add(labels.get(i));
            }
        }
        this.currentIndex = currentIndex >= 0 && currentIndex < waveHeights.size() ? currentIndex : 0;
        this.maxIndex = findMaxIndex();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        chartBounds.set(dp(42), dp(42), getWidth() - dp(14), getHeight() - dp(30));
        drawGrid(canvas);
        if (waveHeights.size() < 2) {
            drawEmpty(canvas);
            return;
        }
        drawWave(canvas);
        drawMarkers(canvas);
        drawAxisLabels(canvas);
    }

    private void drawGrid(Canvas canvas) {
        float maxWave = getAxisMaxWave();
        for (int i = 0; i <= 3; i++) {
            float ratio = i / 3f;
            float y = chartBounds.bottom - chartBounds.height() * ratio;
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            canvas.drawText(String.format(Locale.getDefault(), "%.1f m", maxWave * ratio), dp(2), y + dp(4), textPaint);
        }

        int[] ticks = {4, 8, 12, 16, 20};
        for (int tick : ticks) {
            if (tick >= waveHeights.size()) continue;
            float x = xForIndex(tick);
            canvas.drawLine(x, chartBounds.top, x, chartBounds.bottom, gridPaint);
        }
        canvas.drawLine(chartBounds.left, chartBounds.bottom, chartBounds.right, chartBounds.bottom, axisPaint);
    }

    private void drawEmpty(Canvas canvas) {
        String text = "Memuat grafik gelombang per jam...";
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(text, chartBounds.left, chartBounds.centerY(), textPaint);
    }

    private void drawWave(Canvas canvas) {
        float maxWave = getAxisMaxWave();
        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < waveHeights.size(); i++) {
            float x = xForIndex(i);
            float y = yForWave(waveHeights.get(i), maxWave);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, chartBounds.bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(xForIndex(waveHeights.size() - 1), chartBounds.bottom);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }

    private void drawMarkers(Canvas canvas) {
        if (maxIndex >= 0 && maxIndex < waveHeights.size()) {
            drawBubble(canvas, maxIndex, String.format(Locale.getDefault(), "%.1f m", waveHeights.get(maxIndex)));
        }
        if (currentIndex >= 0 && currentIndex < waveHeights.size()) {
            float maxWave = getAxisMaxWave();
            float x = xForIndex(currentIndex);
            float y = yForWave(waveHeights.get(currentIndex), maxWave);
            pointPaint.setStyle(Paint.Style.FILL);
            pointPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dp(8), pointPaint);
            pointPaint.setStyle(Paint.Style.STROKE);
            pointPaint.setStrokeWidth(dp(3));
            pointPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
            canvas.drawCircle(x, y, dp(8), pointPaint);
            pointPaint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawBubble(Canvas canvas, int index, String label) {
        float maxWave = getAxisMaxWave();
        float x = xForIndex(index);
        float y = Math.max(chartBounds.top - dp(8), yForWave(waveHeights.get(index), maxWave) - dp(18));
        float width = Math.max(dp(52), bubbleTextPaint.measureText(label) + dp(18));
        float left = Math.max(chartBounds.left, Math.min(x - width / 2f, chartBounds.right - width));
        bubbleBounds.set(left, y - dp(14), left + width, y + dp(10));
        canvas.drawRoundRect(bubbleBounds, dp(10), dp(10), bubblePaint);
        canvas.drawText(label, bubbleBounds.centerX(), bubbleBounds.centerY() + dp(4), bubbleTextPaint);
    }

    private void drawAxisLabels(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        int[] ticks = {4, 8, 12, 16, 20};
        for (int tick : ticks) {
            if (tick >= waveHeights.size()) continue;
            String label = getHourLabel(tick);
            canvas.drawText(label, xForIndex(tick), getHeight() - dp(8), textPaint);
        }
    }

    private String getHourLabel(int index) {
        if (index >= 0 && index < timeLabels.size()) {
            String value = timeLabels.get(index);
            if (value != null && value.length() >= 13) {
                return value.substring(11, 13) + ":00";
            }
        }
        return index + "j";
    }

    private float xForIndex(int index) {
        if (waveHeights.size() <= 1) return chartBounds.left;
        return chartBounds.left + (chartBounds.width() * index / (waveHeights.size() - 1f));
    }

    private float yForWave(float wave, float maxWave) {
        float ratio = Math.max(0f, Math.min(1f, wave / maxWave));
        return chartBounds.bottom - (chartBounds.height() * ratio);
    }

    private float getAxisMaxWave() {
        float max = Math.max(0.3f, getMaxWave());
        return (float) (Math.ceil(max * 10f) / 10f);
    }

    private float getMaxWave() {
        float max = 0f;
        for (Float value : waveHeights) {
            if (value != null && value > max) {
                max = value;
            }
        }
        return max;
    }

    private int findMaxIndex() {
        int index = -1;
        float max = -1f;
        for (int i = 0; i < waveHeights.size(); i++) {
            float value = waveHeights.get(i);
            if (value > max) {
                max = value;
                index = i;
            }
        }
        return index;
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}

package com.cse2200.maacare.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.cse2200.maacare.R;

/**
 * Custom View extending android.view.View to render a medical gestational progress arc gauge.
 * Overrides onMeasure() and onDraw(Canvas) using Paint, Path, and RectF.
 */
public class PregnancyProgressArcView extends View {

    private static final float START_ANGLE = 145f;
    private static final float TOTAL_SWEEP = 250f;
    private static final int DEFAULT_MAX_WEEKS = 40;

    private int currentWeek = 20;
    private int maxWeeks = DEFAULT_MAX_WEEKS;
    private int daysRemaining = 140;

    private float strokeWidthPx = 36f;
    private int t1Color = Color.parseColor("#FFA726"); // Amber
    private int t2Color = Color.parseColor("#26A69A"); // Teal
    private int t3Color = Color.parseColor("#E91E63"); // Rose
    private int trackColor = Color.parseColor("#E0E0E0");
    private int indicatorColor = Color.parseColor("#C2185B");
    private boolean showCenterText = true;

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint indicatorPaint;
    private Paint textPrimaryPaint;
    private Paint textSecondaryPaint;
    private Paint textTertiaryPaint;

    private final RectF arcBounds = new RectF();

    public PregnancyProgressArcView(Context context) {
        super(context);
        init(null);
    }

    public PregnancyProgressArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PregnancyProgressArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PregnancyProgressArcView);
            currentWeek = a.getInt(R.styleable.PregnancyProgressArcView_currentWeek, 20);
            maxWeeks = a.getInt(R.styleable.PregnancyProgressArcView_maxWeeks, 40);
            strokeWidthPx = a.getDimension(R.styleable.PregnancyProgressArcView_arcStrokeWidth, 36f);
            t1Color = a.getColor(R.styleable.PregnancyProgressArcView_trimester1Color, t1Color);
            t2Color = a.getColor(R.styleable.PregnancyProgressArcView_trimester2Color, t2Color);
            t3Color = a.getColor(R.styleable.PregnancyProgressArcView_trimester3Color, t3Color);
            trackColor = a.getColor(R.styleable.PregnancyProgressArcView_trackColor, trackColor);
            indicatorColor = a.getColor(R.styleable.PregnancyProgressArcView_indicatorColor, indicatorColor);
            showCenterText = a.getBoolean(R.styleable.PregnancyProgressArcView_showCenterText, true);
            a.recycle();
        }

        // Initialize Paints
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);

        textPrimaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPrimaryPaint.setColor(Color.parseColor("#212121"));
        textPrimaryPaint.setTextAlign(Paint.Align.CENTER);
        textPrimaryPaint.setFakeBoldText(true);

        textSecondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSecondaryPaint.setColor(Color.parseColor("#666666"));
        textSecondaryPaint.setTextAlign(Paint.Align.CENTER);

        textTertiaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textTertiaryPaint.setColor(indicatorColor);
        textTertiaryPaint.setTextAlign(Paint.Align.CENTER);
        textTertiaryPaint.setFakeBoldText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 500;
        int desiredHeight = 440;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : desiredWidth;
        int height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : desiredHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = strokeWidthPx * 1.2f;
        arcBounds.set(padding, padding, w - padding, h - padding);

        // Adjust text sizes proportionally
        textPrimaryPaint.setTextSize(w * 0.12f);
        textSecondaryPaint.setTextSize(w * 0.055f);
        textTertiaryPaint.setTextSize(w * 0.05f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Draw Background Track Arc
        canvas.drawArc(arcBounds, START_ANGLE, TOTAL_SWEEP, false, trackPaint);

        // 2. Calculate Angle for current week
        float clampedWeek = Math.max(0, Math.min(maxWeeks, currentWeek));
        float currentProgressSweep = (clampedWeek / (float) maxWeeks) * TOTAL_SWEEP;

        // Trimester sector sweeps
        // T1: Weeks 1 - 12 (12 / 40 = 30%)
        // T2: Weeks 13 - 27 (15 / 40 = 37.5%)
        // T3: Weeks 28 - 40 (13 / 40 = 32.5%)
        float t1MaxSweep = (12f / 40f) * TOTAL_SWEEP;
        float t2MaxSweep = (15f / 40f) * TOTAL_SWEEP;
        float t3MaxSweep = (13f / 40f) * TOTAL_SWEEP;

        float remainingSweep = currentProgressSweep;

        // Draw Trimester 1 arc segment
        if (remainingSweep > 0) {
            float sweepToDraw = Math.min(remainingSweep, t1MaxSweep);
            progressPaint.setColor(t1Color);
            canvas.drawArc(arcBounds, START_ANGLE, sweepToDraw, false, progressPaint);
            remainingSweep -= sweepToDraw;
        }

        // Draw Trimester 2 arc segment
        if (remainingSweep > 0) {
            float sweepToDraw = Math.min(remainingSweep, t2MaxSweep);
            progressPaint.setColor(t2Color);
            canvas.drawArc(arcBounds, START_ANGLE + t1MaxSweep, sweepToDraw, false, progressPaint);
            remainingSweep -= sweepToDraw;
        }

        // Draw Trimester 3 arc segment
        if (remainingSweep > 0) {
            float sweepToDraw = Math.min(remainingSweep, t3MaxSweep);
            progressPaint.setColor(t3Color);
            canvas.drawArc(arcBounds, START_ANGLE + t1MaxSweep + t2MaxSweep, sweepToDraw, false, progressPaint);
        }

        // 3. Draw Center Information Typography
        if (showCenterText) {
            float centerX = arcBounds.centerX();
            float centerY = arcBounds.centerY() + 10;

            String weekText = "Week " + currentWeek;
            String trimesterText;
            if (currentWeek <= 12) {
                trimesterText = "1st Trimester";
            } else if (currentWeek <= 27) {
                trimesterText = "2nd Trimester";
            } else {
                trimesterText = "3rd Trimester";
            }
            String daysText = daysRemaining + " Days to EDD";

            canvas.drawText(weekText, centerX, centerY - 25, textPrimaryPaint);
            canvas.drawText(trimesterText, centerX, centerY + 25, textSecondaryPaint);
            canvas.drawText(daysText, centerX, centerY + 70, textTertiaryPaint);
        }
    }

    /**
     * Updates the gestational progress and triggers redrawing of the arc.
     */
    public void setProgress(int week, int daysLeft) {
        this.currentWeek = Math.max(1, Math.min(maxWeeks, week));
        this.daysRemaining = Math.max(0, daysLeft);
        invalidate();
    }

    public int getCurrentWeek() {
        return currentWeek;
    }
}

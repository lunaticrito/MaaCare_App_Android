package com.cse2200.maacare.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.cse2200.maacare.R;
import com.cse2200.maacare.data.db.entity.BabyGrowthEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View rendering WHO Percentile Growth Curves (3rd, 15th, 50th median, 85th, 97th)
 * with a shaded normal green corridor and baby's actual measured weight bezier curve.
 */
public class BabyGrowthChartView extends View {

    private Paint gridPaint;
    private Paint axisTextPaint;
    private Paint percentileP3P97Paint;
    private Paint percentileP15P85Paint;
    private Paint medianP50Paint;
    private Paint normalZonePaint;
    private Paint actualGrowthPaint;
    private Paint actualPointPaint;
    private Paint actualPointInnerPaint;

    private final Path pathP3 = new Path();
    private final Path pathP15 = new Path();
    private final Path pathP50 = new Path();
    private final Path pathP85 = new Path();
    private final Path pathP97 = new Path();
    private final Path zonePath = new Path();
    private final Path actualCurvePath = new Path();

    private final List<BabyGrowthEntity> growthData = new ArrayList<>();

    // WHO Standard Weight Percentiles for 0-6 Months (kg)
    // Month: 0, 1, 2, 3, 4, 5, 6
    private static final float[] P3_WEIGHTS  = {2.4f, 3.4f, 4.3f, 5.0f, 5.6f, 6.0f, 6.4f};
    private static final float[] P15_WEIGHTS = {2.8f, 3.8f, 4.8f, 5.5f, 6.1f, 6.6f, 7.0f};
    private static final float[] P50_WEIGHTS = {3.3f, 4.5f, 5.6f, 6.4f, 7.0f, 7.5f, 7.9f};
    private static final float[] P85_WEIGHTS = {3.8f, 5.1f, 6.3f, 7.2f, 7.8f, 8.4f, 8.8f};
    private static final float[] P97_WEIGHTS = {4.2f, 5.7f, 7.0f, 8.0f, 8.7f, 9.3f, 9.8f};

    private static final float MIN_WEIGHT = 2.0f;
    private static final float MAX_WEIGHT = 11.0f;
    private static final int MAX_MONTHS = 6;

    public BabyGrowthChartView(Context context) {
        super(context);
        init();
    }

    public BabyGrowthChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BabyGrowthChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E3E8"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        axisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTextPaint.setColor(Color.parseColor("#7A828E"));
        axisTextPaint.setTextSize(26f);

        // Percentiles 3% & 97% (Thin grey/amber dashed boundary)
        percentileP3P97Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentileP3P97Paint.setColor(Color.parseColor("#B0BEC5"));
        percentileP3P97Paint.setStyle(Paint.Style.STROKE);
        percentileP3P97Paint.setStrokeWidth(2.5f);

        // Percentiles 15% & 85% (Normal boundary)
        percentileP15P85Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentileP15P85Paint.setColor(Color.parseColor("#81C784"));
        percentileP15P85Paint.setStyle(Paint.Style.STROKE);
        percentileP15P85Paint.setStrokeWidth(3f);

        // Median 50% curve
        medianP50Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        medianP50Paint.setColor(Color.parseColor("#4CAF50"));
        medianP50Paint.setStyle(Paint.Style.STROKE);
        medianP50Paint.setStrokeWidth(4.5f);

        // Shaded green zone (15% to 85%)
        normalZonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normalZonePaint.setColor(Color.parseColor("#154CAF50")); // 8% alpha translucent green
        normalZonePaint.setStyle(Paint.Style.FILL);

        // Actual growth curve (Pink/Coral accent)
        actualGrowthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        actualGrowthPaint.setColor(Color.parseColor("#F28B96"));
        actualGrowthPaint.setStyle(Paint.Style.STROKE);
        actualGrowthPaint.setStrokeWidth(6.5f);
        actualGrowthPaint.setPathEffect(new CornerPathEffect(24));

        actualPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        actualPointPaint.setColor(Color.parseColor("#F28B96"));
        actualPointPaint.setStyle(Paint.Style.FILL);

        actualPointInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        actualPointInnerPaint.setColor(Color.WHITE);
        actualPointInnerPaint.setStyle(Paint.Style.FILL);
    }

    public void setGrowthData(List<BabyGrowthEntity> data) {
        growthData.clear();
        if (data != null) {
            growthData.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) (width * 0.62f); // Golden ratio proportions
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = 70;
        int paddingRight = 40;
        int paddingTop = 40;
        int paddingBottom = 60;

        float width = getWidth() - paddingLeft - paddingRight;
        float height = getHeight() - paddingTop - paddingBottom;

        if (width <= 0 || height <= 0) return;

        // Draw horizontal grid lines & Y labels (Weight kg)
        for (int w = 2; w <= 10; w += 2) {
            float y = paddingTop + height - ((w - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * height;
            canvas.drawLine(paddingLeft, y, paddingLeft + width, y, gridPaint);
            canvas.drawText(w + " kg", 12f, y + 8f, axisTextPaint);
        }

        // Draw vertical grid lines & X labels (Months)
        for (int m = 0; m <= MAX_MONTHS; m++) {
            float x = paddingLeft + (m / (float) MAX_MONTHS) * width;
            canvas.drawLine(x, paddingTop, x, paddingTop + height, gridPaint);
            String label = m == 0 ? "Birth" : m + "m";
            float textWidth = axisTextPaint.measureText(label);
            canvas.drawText(label, x - textWidth / 2f, getHeight() - 15f, axisTextPaint);
        }

        // Compute WHO curves paths
        buildCurvePath(pathP3, P3_WEIGHTS, paddingLeft, paddingTop, width, height);
        buildCurvePath(pathP15, P15_WEIGHTS, paddingLeft, paddingTop, width, height);
        buildCurvePath(pathP50, P50_WEIGHTS, paddingLeft, paddingTop, width, height);
        buildCurvePath(pathP85, P85_WEIGHTS, paddingLeft, paddingTop, width, height);
        buildCurvePath(pathP97, P97_WEIGHTS, paddingLeft, paddingTop, width, height);

        // Build Normal Zone Polygon between P15 and P85
        zonePath.reset();
        for (int m = 0; m <= MAX_MONTHS; m++) {
            float x = paddingLeft + (m / (float) MAX_MONTHS) * width;
            float y = paddingTop + height - ((P85_WEIGHTS[m] - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * height;
            if (m == 0) zonePath.moveTo(x, y);
            else zonePath.lineTo(x, y);
        }
        for (int m = MAX_MONTHS; m >= 0; m--) {
            float x = paddingLeft + (m / (float) MAX_MONTHS) * width;
            float y = paddingTop + height - ((P15_WEIGHTS[m] - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * height;
            zonePath.lineTo(x, y);
        }
        zonePath.close();

        // 1. Draw Normal Corridor (Green Fill)
        canvas.drawPath(zonePath, normalZonePaint);

        // 2. Draw WHO Percentile Reference Curves
        canvas.drawPath(pathP3, percentileP3P97Paint);
        canvas.drawPath(pathP97, percentileP3P97Paint);
        canvas.drawPath(pathP15, percentileP15P85Paint);
        canvas.drawPath(pathP85, percentileP15P85Paint);
        canvas.drawPath(pathP50, medianP50Paint);

        // Legend label on Median
        float medEndX = paddingLeft + width - 10;
        float medEndY = paddingTop + height - ((P50_WEIGHTS[MAX_MONTHS] - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * height;
        canvas.drawText("50th %ile", medEndX - 100, medEndY - 12, axisTextPaint);

        // 3. Draw Baby's Actual Growth Curve
        if (!growthData.isEmpty()) {
            actualCurvePath.reset();
            List<float[]> points = new ArrayList<>();

            for (BabyGrowthEntity record : growthData) {
                int month = Math.min(record.getAgeMonths(), MAX_MONTHS);
                float x = paddingLeft + (month / (float) MAX_MONTHS) * width;
                float clampedWeight = Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, (float) record.getWeightKg()));
                float y = paddingTop + height - ((clampedWeight - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * height;
                points.add(new float[]{x, y});
            }

            for (int i = 0; i < points.size(); i++) {
                float[] pt = points.get(i);
                if (i == 0) {
                    actualCurvePath.moveTo(pt[0], pt[1]);
                } else {
                    float[] prev = points.get(i - 1);
                    float midX = (prev[0] + pt[0]) / 2f;
                    actualCurvePath.cubicTo(midX, prev[1], midX, pt[1], pt[0], pt[1]);
                }
            }

            canvas.drawPath(actualCurvePath, actualGrowthPaint);

            // Draw point dots with rings
            for (float[] pt : points) {
                canvas.drawCircle(pt[0], pt[1], 12f, actualPointPaint);
                canvas.drawCircle(pt[0], pt[1], 6f, actualPointInnerPaint);
            }
        }
    }

    private void buildCurvePath(Path path, float[] weights, float pLeft, float pTop, float w, float h) {
        path.reset();
        for (int m = 0; m <= MAX_MONTHS; m++) {
            float x = pLeft + (m / (float) MAX_MONTHS) * w;
            float y = pTop + h - ((weights[m] - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * h;
            if (m == 0) {
                path.moveTo(x, y);
            } else {
                float prevX = pLeft + ((m - 1) / (float) MAX_MONTHS) * w;
                float prevY = pTop + h - ((weights[m - 1] - MIN_WEIGHT) / (MAX_WEIGHT - MIN_WEIGHT)) * h;
                float midX = (prevX + x) / 2f;
                path.cubicTo(midX, prevY, midX, y, x, y);
            }
        }
    }
}

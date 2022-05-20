package org.doncc.testslideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

/**
 * @author doncc
 * date  2022/5/18
 */
public class SlideView extends View implements View.OnTouchListener {

    private SlideViewListener listener;

    private Paint backPaint;//滑块背景画笔
    private Paint caliperPaint;//卡尺画笔
    private Paint textPaint;//卡尺文字画笔
    private Paint sliderPaint;//滑块画笔
    private Paint sliderTextPaint;//滑块文字画笔


    private Paint circlePaint1;//参考点1
    private Paint circlePaint2;//参考点2

    private RectF slideBackRect;

    //圆球滑块半径
    private int sliderRadius;
    //圆球滑块区域
    private float mIconLeft, mIconTop, mIconRight, mIconBottom;

    //屏幕宽度
    private int screenWidth;
    //最小、大点横向位置
    private int MIN_POINT, MAX_POINT;
    //卡尺距离
    private int CALIPER_DISTANCE;
    //当前滑块坐标
    private float sliderPoint;
    //滑块上文字
    private float sliderTextValue = 1.0f;

    //当前视图宽度
    private int viewWidth, viewHeight;

    //最大刻度
    private int MAX_SCALE = 8;
    //刻度线长度偏移量
    private int CALIPER_SCALE_LENGTH = 9;
    //绘制点个数
    private int POINT_COUNT = 4;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private final DecimalFormat decimalFormatShowText = new DecimalFormat("0.0");
    private final int[] pointArr = new int[POINT_COUNT];

    public SlideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initPaint();
        initParams(context, attrs);
        setOnTouchListener(this);
    }

    private void initParams(Context context, AttributeSet attrs) {
        //获取屏幕信息
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DividingRule);

        MAX_SCALE = typedArray.getInt(R.styleable.DividingRule_max_scale, MAX_SCALE);
        CALIPER_SCALE_LENGTH = typedArray.getInt(R.styleable.DividingRule_caliper_scale_length, CALIPER_SCALE_LENGTH);
        POINT_COUNT = typedArray.getInt(R.styleable.DividingRule_point_count, POINT_COUNT);

        typedArray.recycle();
    }

    private void initPaint() {
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.FILL);
        backPaint.setColor(getResources().getColor(R.color.black_alpha_20));

        caliperPaint = new Paint();
        caliperPaint.setAntiAlias(true);
        caliperPaint.setStyle(Paint.Style.FILL);
        caliperPaint.setColor(Color.WHITE);
        caliperPaint.setStrokeWidth(3);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(ScreenUtils.px2sp(getContext(), 99));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        sliderPaint = new Paint();
        sliderPaint.setAntiAlias(true);
        sliderPaint.setStyle(Paint.Style.FILL);
        sliderPaint.setColor(getResources().getColor(R.color.black_alpha_60));

        sliderTextPaint = new Paint();
        sliderTextPaint.setAntiAlias(true);
        sliderTextPaint.setTextSize(ScreenUtils.px2sp(getContext(), 109));
        sliderTextPaint.setColor(Color.WHITE);
        sliderTextPaint.setTextAlign(Paint.Align.CENTER);
        sliderTextPaint.setStyle(Paint.Style.FILL);

        //参考点1
        circlePaint1 = new Paint();
        circlePaint1.setColor(Color.argb(100, 255, 0, 0));
        //参考点2
        circlePaint2 = new Paint();
        circlePaint2.setColor(Color.argb(100, 0, 255, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(width, height);
        viewWidth = measureDimension(300, widthMeasureSpec);
        viewHeight = measureDimension(60, heightMeasureSpec);

        sliderRadius = viewHeight / 2;

        //获取移动极限位置锚点
        MIN_POINT = sliderRadius;
        MAX_POINT = viewWidth - MIN_POINT;
        //获取卡尺极端距离
        CALIPER_DISTANCE = MAX_POINT - MIN_POINT;

        //默认min点
        sliderPoint = MIN_POINT;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        slideBackRect = new RectF(
                0, 0, viewWidth, viewHeight
        );
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize; // UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {//wrap_content
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBaseInfo(canvas);
        drawSlider(canvas);
//        //绘制参考点
//        canvas.drawCircle(minPoint, viewHeight / 2, minPoint, circlePaint1);
//        canvas.drawCircle(viewWidth - minPoint, viewHeight / 2, minPoint, circlePaint2);
    }

    /**
     * 绘制滑块
     *
     * @param canvas
     */
    private void drawSlider(Canvas canvas) {
        //绘制滑块，偏移6
        canvas.drawCircle(sliderPoint, viewHeight / 2, sliderRadius - 6, sliderPaint);
        //绘制滑块文字，偏移12
        canvas.drawText(String.format("%sx", decimalFormatShowText.format(sliderTextValue)), sliderPoint, sliderRadius + 12, sliderTextPaint);
    }

    /**
     * 绘制基础信息
     *
     * @param canvas
     */
    private void drawBaseInfo(Canvas canvas) {
        /*画背景条*/
        canvas.drawRoundRect(slideBackRect, sliderRadius, sliderRadius, backPaint);
        //绘制卡尺
        canvas.drawLine(MIN_POINT, MIN_POINT, viewWidth - MIN_POINT, MIN_POINT, caliperPaint);

        int lastI = 1;
        //绘制卡尺刻度线
        for (int i = 1; i <= POINT_COUNT; i++) {

            final int CALIPER_SCALE_X = (i - 1) * CALIPER_DISTANCE / (POINT_COUNT - 1) + MIN_POINT;

            //画刻度
            canvas.drawLine(CALIPER_SCALE_X,
                    MIN_POINT - CALIPER_SCALE_LENGTH,
                    CALIPER_SCALE_X,
                    MIN_POINT + CALIPER_SCALE_LENGTH,
                    caliperPaint);

            //画刻度文字
            canvas.drawText(i == 1 ? i + "" : lastI + "",
                    CALIPER_SCALE_X,
                    MIN_POINT - CALIPER_SCALE_LENGTH - 12,
                    textPaint);
            if (i == 1) {
                lastI = 1;
            }
            lastI *= 2;

            //点加入坐标数组
            pointArr[i - 1] = CALIPER_SCALE_X;

        }
        //绘制'1x'文字，偏移12
        canvas.drawText("1x", MIN_POINT / 2, MIN_POINT + 12, textPaint);
        //绘制'8x'文字，偏移12
        canvas.drawText(MAX_SCALE + "x", MAX_POINT + MIN_POINT / 2, MIN_POINT + 12, textPaint);

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        float touchX = event.getX();
        float disX = 0f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //超过操作范围
                if (touchX < MIN_POINT || touchX > MAX_POINT) {
                    return super.onTouchEvent(event);
                }
                disX = touchX - sliderPoint;
                break;
            case MotionEvent.ACTION_MOVE:
                sliderPoint = touchX - disX;
                if (sliderPoint < MIN_POINT) {
                    sliderPoint = MIN_POINT;
                } else if (sliderPoint > MAX_POINT) {
                    sliderPoint = MAX_POINT;
                }

                for (int i = 0; i < pointArr.length; i++) {
                    int pl = i, pr = pl + 1;
                    if (pl == (POINT_COUNT - 1)) {
                        break;
                    }
                    int minPoint = pointArr[pl];
                    int maxPoint = pointArr[pr];

                    if (sliderPoint >= minPoint && sliderPoint <= maxPoint) {
                        float percentage = (sliderPoint - minPoint) / (maxPoint - minPoint);
                        //偏移步进
                        sliderTextValue = (Float.parseFloat(decimalFormat.format(percentage))) * ((int) Math.pow(2, i)) + ((int) Math.pow(2, i));
                    }
                }

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    listener.valueFeedback(Float.parseFloat(decimalFormatShowText.format(sliderTextValue)));
                }
                break;
        }

        return true;
    }

    public void setSlideViewListener(SlideViewListener listener) {
        this.listener = listener;
    }

    public interface SlideViewListener {
        void valueFeedback(float value);
    }
}

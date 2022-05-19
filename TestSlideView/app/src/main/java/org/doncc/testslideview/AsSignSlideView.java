package org.doncc.testslideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


public class AsSignSlideView extends View implements View.OnTouchListener {

    private static final int IDEL = 1;          //默认状态
    private static final int CHANGE = 2;        //变化状态
    private static final int DEFAULT_TIME = 10; //默认延时时间

    private final int MIN_DISTANCE = 20;
    private final int UI_SLIDE_BACK_WIDTH = 170;
    private final int UI_SLIDE_BACK_HEIGHT = 60;
    private final int UI_SLIDE_BACK_TOP = 124;


    private int mDistance_30;

    private Bitmap mIconBitmap;          //滑动按钮
    //    private Bitmap mBgBitmap;            //滑动背景
    private Bitmap mRightArrowBitmap;    //右侧箭头图片
    //    private Bitmap mLeftArrowBitmap;     //左侧箭头图片
    private String mAsSignTxt;
    private Bitmap mRightOKBitmap;       //右侧确定图片
    private Bitmap mLeftCancelBitmap;    //左侧取消图片

    private Paint mIconPaint;            //滑动按钮画笔
    private Paint mBackPaint;            //滑块背景画笔
    private Paint mRightArrowPaint;      //右侧箭头画笔
    private Paint mSignTextPaint;      //打点按钮画笔
    private Paint mRightOKPaint;         //右侧ok图标画笔

    private Paint point1, point2, point3, point4;//参考点

    private float mIconLeft, mIconTop, mIconRight, mIconBottom;

    private Rect grayRect;//

    private SlideListener mListener;     //滑动监听
    private float mSlideDistance;          //滑动距离
    private float mStartX, mStartY;                 //开始点XY坐标
    private float mMaxDistance;            //滑动的最大距离
    private int mDrawStatus = IDEL;

    private Handler mHandler = new Handler();
    private int mViewWidth;
    private float mSlideBgWidth;
    private float mSlideBgHeight;
    private float mIconWidth;
    private int mRightArrowHeight;
    private int mRightArrowWidth;
    private int mRightOkWidth;
    private float mSlideBgTop;
    private RectF slideBackRectF;
    private float mRightOkLeft;
    private float mRightOkTop;

    public AsSignSlideView(Context context) {
        super(context, null);
    }

    public AsSignSlideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ScreenUtils mScreenUtils = new ScreenUtils(context);
        mDistance_30 = (int) (mScreenUtils.getScreenWidth() / UIConstants.UI_BASE_WIDTH_LAND * 30);

        initPaint();

        grayRect = new Rect();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.manualTeachSlideView);
        int mSlideIcon = typedArray.getResourceId(R.styleable.manualTeachSlideView_slideMenu_icon, 0);
        int mRightArrowIcon = typedArray.getResourceId(R.styleable.manualTeachSlideView_slideMenu_right_arrow, 0);
        mAsSignTxt = typedArray.getString(R.styleable.manualTeachSlideView_slideMenu_assign_txt);
        int mRightOKIcon = typedArray.getResourceId(R.styleable.manualTeachSlideView_slideMenu_right_icon, 0);

        typedArray.recycle();

        if (mSlideIcon != 0) {
            mIconBitmap = ((BitmapDrawable) ContextCompat.getDrawable(getContext(), mSlideIcon)).getBitmap();
        }
        if (mRightArrowIcon != 0) {
            mRightArrowBitmap = ((BitmapDrawable) ContextCompat.getDrawable(getContext(), mRightArrowIcon)).getBitmap();
        }
        if (mRightOKIcon != 0) {
            mRightOKBitmap = ((BitmapDrawable) ContextCompat.getDrawable(getContext(), mRightOKIcon)).getBitmap();
        }


        setOnTouchListener(this);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        //滑动背景画笔
        mBackPaint = new Paint();
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.FILL);

        //滑动图标画笔
        mIconPaint = new Paint();
        mIconPaint.setAntiAlias(true);
        mIconPaint.setStyle(Paint.Style.FILL);
        //右箭头画笔
        mRightArrowPaint = new Paint();
        mRightArrowPaint.setAntiAlias(true);
        mRightArrowPaint.setStyle(Paint.Style.FILL);
        //ok画笔
        mRightOKPaint = new Paint();
        mRightOKPaint.setAntiAlias(true);
        mRightOKPaint.setStyle(Paint.Style.FILL);

        //ok打点滑动按钮
        mSignTextPaint = new Paint();
        mSignTextPaint.setAntiAlias(true);
        mSignTextPaint.setTextSize(sp2px(12));
        mSignTextPaint.setTextAlign(Paint.Align.CENTER);
        mSignTextPaint.setStyle(Paint.Style.FILL);
        mSignTextPaint.setColor(getResources().getColor(R.color.black));

    }

    private void initData() {
        //View 的宽高
        mViewWidth = getWidth();

        //滑块的宽高
        mSlideBgWidth = dp2px(UI_SLIDE_BACK_WIDTH);
        mSlideBgHeight = dp2px(UI_SLIDE_BACK_HEIGHT);

        //滑动按钮的宽高 宽高相等
        mIconWidth = mIconBitmap.getWidth();

        //滑动最大距离
        mMaxDistance = mSlideBgWidth / 2f;


        mSlideBgTop = (screenHeight - dp2px(UI_SLIDE_BACK_TOP)) + mDistance_30;

        slideBackRectF = new RectF(
                mViewWidth / 2 - mIconWidth / 3,
                mSlideBgTop,
                (mViewWidth - mIconWidth) / 2f + mSlideBgWidth,
                (mSlideBgTop + dp2px(UI_SLIDE_BACK_HEIGHT)));

    }

    private int screenWidth, screenHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = measureDimension(600, widthMeasureSpec);
        screenHeight = measureDimension(140, heightMeasureSpec);
        setMeasuredDimension(screenWidth, screenHeight);
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
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //触摸点的x y坐标
                mStartX = event.getX();
                mStartY = event.getY();

                //判断焦点在按钮上
                if (mStartX > mIconLeft && mStartX < mIconRight && mStartY > mIconTop && mStartY < mIconBottom) {
                    mHandler.removeCallbacks(mRightResetRunnable);
                    mDrawStatus = CHANGE;
                } else {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDrawStatus == CHANGE) {
                    mSlideDistance = event.getX() - mStartX;

                    if (mSlideDistance > mMaxDistance) {
                        mSlideDistance = mMaxDistance;
                    }
                    if (mSlideDistance < 0) {
                        mSlideDistance = 0;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mSlideDistance < MIN_DISTANCE &&
                        event.getX() > mIconLeft && event.getX() < mIconRight && event.getY() > mIconTop && event.getY() < mIconBottom) {
                    if (mListener != null) {
                        mListener.asSign();
                    }
                } else {
//                    if (mSlideDistance > 0) {
//                        if (mSlideDistance < mMaxDistance) {
//                            mHandler.post(mRightResetRunnable);
//                        } else {
//                            mDrawStatus = IDEL;
//                            if (mListener != null) {
//                                mListener.asSignDone();
//                                mSlideDistance = 0;
//                            }
//                        }
//
//                        return true;
//                    }
                }

//                mDrawStatus = IDEL;
                break;
            case MotionEvent.ACTION_CANCEL:
                mDrawStatus = IDEL;
                break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initData();
//        /*参考点*/
//        canvas.drawCircle(mRightOkLeft, mRightOkTop, 15, point3);
//        canvas.drawCircle(mRightOkLeft + mRightOkWidth, mRightOkTop, 15, point4);
        /*画背景条*/
        mBackPaint.setColor(getResources().getColor(R.color.black_alpha_40));
        canvas.drawRoundRect(slideBackRectF, mIconWidth / 2f, mIconWidth / 2f, mBackPaint);

        /*画中间指示条*/
        canvas.drawBitmap(mRightArrowBitmap,
                mViewWidth / 2f - mIconWidth / 3f + ((mViewWidth - mIconWidth) / 2f + mSlideBgWidth - mViewWidth / 2f - mIconWidth / 3f) - mRightArrowWidth / 2f,
                mSlideBgTop + dp2px(60) / 2f, mRightArrowPaint);

        /*画右侧ok*/
        mRightOkLeft = (mViewWidth - mIconWidth) / 2f + mSlideBgWidth - mRightOkWidth;
        mRightOkTop = mSlideBgTop + (mSlideBgHeight - mRightOkWidth) / 2;
        /*画右面complete*/
        canvas.drawBitmap(mRightOKBitmap, mRightOkLeft, mRightOkTop, mRightOKPaint);

        switch (mDrawStatus) {
            case IDEL:
//                /*参考点*/
//                canvas.drawCircle((mViewWidth - mIconWidth) / 2f, mSlideBgTop + (mSlideBgHeight - mIconWidth) / 2f, 15, point1);
//                canvas.drawCircle((mViewWidth - mIconWidth) / 2f + mIconWidth, mSlideBgTop + (mSlideBgHeight - mIconWidth) / 2f, 15, point2);
                drawNormalState(canvas);
                break;
            case CHANGE:
//                /*参考点*/
//                canvas.drawCircle(mIconLeft + mSlideDistance, mIconTop, 15, point1);
//                canvas.drawCircle(mIconLeft + mSlideDistance + mIconWidth, mIconTop, 15, point2);
                if (mSlideDistance < MIN_DISTANCE) {
                    drawNormalState(canvas);
                    return;
                }

                //右箭头宽高
                mRightArrowHeight = mRightArrowBitmap.getHeight();
                mRightArrowWidth = mRightArrowBitmap.getWidth();
                //右图标宽高 宽高相等
                mRightOkWidth = mRightOKBitmap.getWidth();

                /*画滑动按钮*/
                canvas.drawBitmap(mIconBitmap, mIconLeft + mSlideDistance, mIconTop, mIconPaint);

                /*画打点文字*/
                canvas.drawText(mAsSignTxt, mIconLeft + mSlideDistance + mIconWidth / 2, mIconTop, mSignTextPaint);
                break;
        }


    }

    private void drawNormalState(Canvas canvas) {
        /*画背景条*/
        mBackPaint.setColor(Color.argb(0, 0, 0, 0));
        canvas.drawRoundRect(slideBackRectF, mIconWidth / 2f, mIconWidth / 2f, mBackPaint);

        /*画滑动按钮*/
        mIconLeft = (mViewWidth - mIconWidth) / 2f;
        mIconTop = mSlideBgTop + (mSlideBgHeight - mIconWidth) / 2f;
        mIconRight = mIconLeft + mIconWidth;
        mIconBottom = mIconTop + mIconWidth;
        canvas.drawBitmap(mIconBitmap, mIconLeft, mIconTop, mIconPaint);

        /*画打点文字*/
        canvas.drawText(mAsSignTxt, mIconLeft + mIconWidth / 2, mIconTop, mSignTextPaint);
    }

    /**
     * 大于零~~0
     */
    private final Runnable mRightResetRunnable = new Runnable() {
        @Override
        public void run() {
            mSlideDistance -= 10;
            if (mSlideDistance <= 0) {
//                mSlideDistance = 0;
                mDrawStatus = CHANGE;
            } else {
                mDrawStatus = CHANGE;
//                invalidate();
            }
            mHandler.postDelayed(this, DEFAULT_TIME);
            invalidate();
        }
    };

    private float dp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float sp2px(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 设置滑动监听
     *
     * @param listener 监听器
     */
    public void setSlideListener(SlideListener listener) {
        this.mListener = listener;
    }

    /**
     * 滑动回调接口
     */
    public interface SlideListener {

        /**
         * 完成打点
         */
        void asSignDone();

        /**
         * 打普通点
         */
        void asSign();
    }
}
package org.doncc.testslideview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


public class ScreenUtils {
    private Context context;
    private int width = 0;
    private int height = 0;
    private int originalWidth = 0;
    private int originalHeight = 0;

    public ScreenUtils(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        WindowManager w = ((Activity) context).getWindowManager();
        Display d = w.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                originalHeight = (Integer) Display.class
                        .getMethod("getRawHeight").invoke(d);
                originalWidth = (Integer) Display.class
                        .getMethod("getRawWidth").invoke(d);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            // includes window decorations (statusbar bar/navigation bar)
        else if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize",
                        Point.class).invoke(d, realSize);
                originalHeight = realSize.y;
                originalWidth = realSize.x;
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        if (originalHeight > originalWidth) {
            if (originalHeight / originalWidth > 2 || originalHeight / originalWidth < 1.55) {
                width = (int) (originalHeight / 1.78);
                height = originalHeight;
            } else {
                width = originalWidth;
                height = originalHeight;
            }
        } else if (originalWidth > originalHeight) {
            if (originalWidth / originalHeight > 2 || originalWidth / originalHeight < 1.55) {
                height = (int) (originalWidth / 1.78);
                width = originalWidth;
            } else {
                width = originalWidth;
                height = originalHeight;
            }
        } else {
            width = (int) (originalHeight / 1.78);
            height = originalHeight;
        }


    }

    /**
     * ????????????
     */
    public static void setFullScreen(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * ????????????
     */
    public static void noActionBar(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public int getScreenOriginalHeight() {
        //  context.getResources().getDisplayMetrics().heightPixels
        return originalHeight;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public int getScreenHeight() {
        return height;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public int getScreenOriginalWidth() {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(outMetrics);
//        return outMetrics.widthPixels;
        // context.getResources().getDisplayMetrics().widthPixels
        return originalWidth;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public int getScreenWidth() {

        return width;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public int getStatusHeight() {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * ???????????? navigation bar ??????
     *
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param activity
     * @return
     */
    public Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param activity
     * @return
     */
    public Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param view
     */
    public void setSizeWithWidthAndBackground(int width, View view, int bgwidth, int bgheight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = (int) ((double) width / bgwidth * bgheight);
        view.setLayoutParams(params);
    }

//    /**
//     * ??????????????????????????????????????????
//     *
//     * @param view
//     */
//    public void setSizeWithBackground(View view, int bgwidth, int bgheight) {
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.width = (int) (width / Constant.UI_BASE_WIDTH_LAND * bgwidth);
//        params.height = (int) (height / Constant.UI_BASE_HEIGHT_LAND * bgheight);
//        view.setLayoutParams(params);
//    }

//    /**
//     * ??????????????????
//     *
//     * @param view
//     */
//    public void setViewWidth(View view, int bgwidth) {
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.width = (int) (width / Constant.UI_BASE_WIDTH_LAND * bgwidth);
//        view.setLayoutParams(params);
//    }

    /**
     * ????????????DPI
     *
     * @param context
     * @return
     */
    public static int getScreenDensityDpi(Context context) {
//        DisplayMetrics dm = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int densityDpi = dm.densityDpi; // ????????????DPI????20 / 160 / 240????240
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * ??????????????????
     *
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context) {
//        DisplayMetrics dm = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
//        float density = dm.density; // ????????????DPI????20 / 160 / 240????240
        return context.getResources().getDisplayMetrics().density;
    }

    public int dip2px(float f) {
        return (int) (0.5D + (double) (f * getDensity(context)));
    }

    public static int dpToPx(Context mContext, int dps) {
        return Math.round(mContext.getResources().getDisplayMetrics().density * dps);
    }

    public int dip2px(int i) {
        return (int) (0.5D + (double) (getDensity(context) * (float) i));

    }

    public int px2dip(float f) {
        float f1 = getDensity(context);
        return (int) (((double) f - 0.5D) / (double) f1);
    }

    public int px2dip(int i) {
        float f = getDensity(context);
        return (int) (((double) i - 0.5D) / (double) f);
    }

    public float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * ???px????????????sp??????????????????????????????
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * ????????????????????????
     *
     * @param context ?????????
     * @return ??????????????????????????????
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * ????????????????????????
     *
     * @param context ?????????
     * @return ??????????????????????????????
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasNavigationBar(WindowManager windowManager) {
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;
        return (realWidth - displayWidth) > 0 /*|| (realHeight - displayHeight) > 0*/;
    }

    //???????????????
    public static void removeThenavigationBar(Window window) {
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * ????????????????????????
     *
     * @param context
     * @return
     */
    public static int getFollowScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * ????????????????????????
     *
     * @param context
     * @return
     */
    public static int getFollowScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * ??????????????????ActionBarSize?????????
     *
     * @param context
     * @return
     */
    public static int getActionBarSize(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
}

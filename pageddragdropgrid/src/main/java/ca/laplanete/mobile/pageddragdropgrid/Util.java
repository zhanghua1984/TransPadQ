package ca.laplanete.mobile.pageddragdropgrid;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Kongxiaojun on 2015/4/8.
 */
public class Util {
    /**
     * dp转px
     *
     * @param dpValue
     *            dp
     * @return int px
     * @throws
     */
    public static int dp2px(Context context,float dpValue) {
        return (int) (dpValue * getDensity(context) + 0.5f);
    }

    /**
     *
     * 获取屏幕密度
     *
     * @return float
     * @throws
     */
    public static float getDensity(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }
}

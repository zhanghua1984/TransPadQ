package cn.transpad.transpadui.cache;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import cn.transpad.transpadui.util.L;

/**
 * Created by Kongxiaojun on 2015/4/15.
 */
public class GrayBitmapDisplayer implements BitmapDisplayer {
    private static final String TAG = GrayBitmapDisplayer.class.getSimpleName();

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        try {
            Bitmap grayImg = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(grayImg);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorMatrixFilter);
            canvas.drawBitmap(bitmap, 0, 0, paint);
//            bitmap.recycle();
            imageAware.setImageBitmap(grayImg);
        } catch (Exception e) {
            // e.printStackTrace();
            L.e(TAG, "display", "erro=" + e);
        }
    }
}

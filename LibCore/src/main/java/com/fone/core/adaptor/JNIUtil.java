package com.fone.core.adaptor;

import android.graphics.Bitmap;
import android.os.Environment;

import com.fone.player.FonePlayer;
import com.fone.player.L;

import java.io.File;

public class JNIUtil {

	private static final String TAG = "JNIUtil";

	public static int getSdkVersion() {
		int v = android.os.Build.VERSION.SDK_INT;
		return v;
	}

	public static int getScreenHeight() {
		return FonePlayer.getScreenHeight();
	}

	public static int getScreenWidth() {
		return FonePlayer.getScreenWidth();
	}

	/**
	 *
	 * 判断网络连接类型(C层会调用该方法,修改名称后,请通知郑建明,否则调用异常)
	 *
	 * @return int 网络连接类型<br>
	 *         0 无网<br>
	 *         1 wifi网<br>
	 *         2 2G网<br>
	 *         3 3G网<br>
	 *         4 4G网<br>
	 */
	public static int getNetworkType() {
		return FonePlayer.getNetWorkType();
	}

	/**
	 *
	 * 返回缓存路径(C层会调用该方法,修改名称后,请通知郑建明,否则调用异常)
	 *
	 * @return String 缓存路径
	 */
	public static String getStorePath() {

		String path = Environment.getExternalStorageDirectory()
				+ "/100tv/cache";
		// 如果路径不存在,则创建
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		L.v(TAG, "getStorePath", "path=" + path);
		return path;
	}

	/**
	 *
	 * 返回缓存路径(C层会调用该方法,修改名称后,请通知郑建明,否则调用异常)
	 *
	 * @return String 缓存路径
	 */
	public static String getStorePath(int type) {

		String path = null;
		switch (type){
			case 1:
				path = Environment.getExternalStorageDirectory()
						+ "/100tv/cache";
				break;
			case 4:
				path = Environment.getExternalStorageDirectory()
						+ "/100tv/download";
				break;
			default:
				path = Environment.getExternalStorageDirectory()
						+ "/100tv/cache";
				break;
		}
		// 如果路径不存在,则创建
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		L.v(TAG, "getStorePath(int type)", "type = "+ type +" path=" + path);
		return path;
	}

	/**
	 * 是否支持省电加速（底层回调）
	 *
	 * @return
	 * @return int 1 YES , 0 NO
	 * @throws
	 */
	public static int get_setting_use_hw_decoder() {
		L.v(TAG,
				"get_setting_use_hw_decoder >>>>>>>"
						+ FonePlayer.getHwPlusSupport());
		return FonePlayer.getHwPlusSupport();
	}

	// return 1 YES , 0 NO
	public static int get_setting_use_sys_decoder() {
		L.v(TAG, "get_setting_use_sys_decoder >>>>>>> " + FonePlayer.getSystemDecoderSupport());
		return FonePlayer.getSystemDecoderSupport();
	}

	public static Object Bitmapbuffer_Create(int width, int height) {
		if (width > 0 && height > 0) {
			try {
				Object obj = Bitmap.createBitmap(width, height,
						Bitmap.Config.RGB_565);
				return obj;
			} catch (Throwable e)// Exception e
			{
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

}
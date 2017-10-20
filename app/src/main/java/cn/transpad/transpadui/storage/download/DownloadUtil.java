package cn.transpad.transpadui.storage.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import java.io.File;

import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;

public class DownloadUtil {

	private static final String TAG = DownloadUtil.class.getSimpleName();
	public static final int SD_NO_AVAIL_SIZE = 1;
	public static final int FLASH_NO_AVAIL_SIZE = 2;
	public static final int NO_STORE = 0;
	public static final int FULL_SPACE = 3;
	private static final String FILE_PATH_SEPERATOR = "/";
	public static String FILE_PATH_EXTENTION = ".fem";
	private static Context sContext = null;
	private static final DownloadUtil sDownloadUtil = new DownloadUtil();

	public static DownloadUtil getInstance() {
		return sDownloadUtil;
	}

	public static void init(Context context) {
		sContext = context;
	}

	public static void deleteDirectory(String path) {
		File file = new File(path);
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) {// 判断是否是文件
				file.delete();
			} else if (file.isDirectory()) { // 否则如果它是一个目录

				// 声明目录下所有的文件 files[];
				File files[] = file.listFiles();

				// 遍历目录下所有的文件
				for (int i = 0; i < files.length; i++) {

					deleteDirectory(files[i].getAbsolutePath());
				}
			}
			file.delete();

		}
	}

	/**
	 * 
	 * 删除空目录
	 * 
	 * @param path
	 *            待判断目录
	 * @param hierarchyNum
	 *            层级数
	 * @return void
	 */
	public static void deleteEmptyDirectory(int hierarchyNum, String path) {
		File file = new File(path);
		for (int i = 1; i < hierarchyNum; i++) {
			file = file.getParentFile();
			if (file.exists() && file.isDirectory()) { // 判断文件是否存在,并且是目录

				// 声明目录下所有的文件 files[];
				File files[] = file.listFiles();
				if (files != null && files.length == 0) {

					if (!file.getAbsolutePath().endsWith("file")) {

						file.delete();

					}

				}
			}
		}

	}

	public static String formatFragmentTempPath(String folderPath, String name,
			int fragmentIndex) {
		StringBuffer sb = new StringBuffer();
		sb.append(folderPath).append(FILE_PATH_SEPERATOR).append(name);

		if (fragmentIndex >= 0) {
			sb.append(String.format("_%03d", fragmentIndex + 1));
		}

		sb.append(FILE_PATH_EXTENTION);
		// .append(FILE_PATH_SUBFIX_TEMP);

		return sb.toString();
	}

	/**
	 * 
	 * 获取可用空间实际大小
	 * 
	 * @return long 可用空间(字节)
	 */
	public static long getStoreFreeSpaceByte() {
		long freeSpaceByte = 0;
		// google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
		// 所以大于4.4的直接显示机身空间
		if (VERSION.SDK_INT >= 19) {

			// 机身
			freeSpaceByte = SystemUtil.getInstance()
					.getFreeSpaceByteByPhoneBody();

		} else {

			if (SystemUtil.getInstance().isSDCardMounted()) {
				// sdcard
				freeSpaceByte = SystemUtil.getInstance()
						.getFreeSpaceByteBySDCard();
			} else {

				// 机身
				freeSpaceByte = SystemUtil.getInstance()
						.getFreeSpaceByteByPhoneBody();
			}

		}

		return freeSpaceByte;
	}

	/**
	 * 
	 * 获取总空间实际大小
	 * 
	 * @return long 总空间(字节)
	 */
	public static long getStoreTotalSpaceByte() {
		long totalSpaceByte = 0;
		// google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
		// 所以大于4.4的直接显示机身空间
		if (VERSION.SDK_INT >= 19) {

			// 机身
			totalSpaceByte = SystemUtil.getInstance()
					.getTotalSpaceByteByPhoneBody();

		} else {

			if (SystemUtil.getInstance().isSDCardMounted()) {

				// SD卡
				totalSpaceByte = SystemUtil.getInstance()
						.getTotalSpaceByteBySDCard();
			} else {
				// 机身
				totalSpaceByte = SystemUtil.getInstance()
						.getTotalSpaceByteByPhoneBody();
			}
		}

		return totalSpaceByte;
	}

	/**
	 * 检测存储空间
	 * 
	 * @return
	 */
	public static int getStoreSurplusSpace(long AVAIL_SIZE) {

		long blockSize = 0;
		long availCount = 0;

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) { // sd card
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			blockSize = sf.getBlockSize();
			availCount = sf.getAvailableBlocks();
			v("store", " sd space :" + blockSize * availCount);

			if (blockSize * availCount < AVAIL_SIZE) {
				return SD_NO_AVAIL_SIZE;
			}

		} else {

			File file = Environment.getDataDirectory(); // flash card
			if (file.exists()) {
				StatFs stat = new StatFs(file.getPath());
				availCount = stat.getAvailableBlocks();
				blockSize = stat.getBlockSize();
				// v("store"," flash space :"+ blockSize * availCount );

				if (blockSize * availCount < AVAIL_SIZE) {
					return FLASH_NO_AVAIL_SIZE;
				}

			} else {
				return NO_STORE;
			}
		}

		return FULL_SPACE;
	}

	/**
	 * 
	 * 判断存储空间状态
	 * 
	 * @return boolean
	 */
	public static boolean checkStorageSpace() {

		int state = getStoreSurplusSpace(50 * 1024 * 1024);

		if (state != FULL_SPACE) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 
	 * 判断是否有网络
	 * 
	 * @return boolean 是否有网
	 */
	public boolean isNetwork() {
		boolean isNet = false;
		int netState = getNetType();
		switch (netState) {
		case StorageModule.MSG_NO_NETWORK_TYPE:
			isNet = false;
			break;
		default:
			isNet = true;
			break;
		}
		return isNet;
	}

	/**
	 * 
	 * 获取网络类型
	 * 
	 * @return int
	 */
	public static int getNetType() {
		if (sContext == null) {
			L.e(TAG, "getNetType", "sContext=null netType=MSG_NO_NETWORK_TYPE");
			return StorageModule.MSG_NO_NETWORK_TYPE;
		}
		final ConnectivityManager connectivityManager = (ConnectivityManager) sContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
		if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
			// 注意一：
			// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
			// 但是有些电信机器，仍可以正常联网，
			// 所以当成net网络处理依然尝试连接网络。
			// （然后在socket中捕捉异常，进行二次判断与用户提示）。
			v("getNetType", "netType=MSG_NO_NETWORK_TYPE");
			return StorageModule.MSG_NO_NETWORK_TYPE;
		} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

			// 先判断手机网络,在判断是否是wifi,否则判断会出现错乱(考虑wifi和手机网络同时存在的情况)
			TelephonyManager mTelephonyManager = (TelephonyManager) sContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			int type = mTelephonyManager.getNetworkType();
			switch (type) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				v("getNetType", "mobile netType=MSG_2G_NETWORK_TYPE");
				return StorageModule.MSG_2G_NETWORK_TYPE;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				// 判断3g网
				v("getNetType", "mobile netType=MSG_3G_NETWORK_TYPE");
				return StorageModule.MSG_3G_NETWORK_TYPE;
			case TelephonyManager.NETWORK_TYPE_LTE:
				// 判断4g网
				v("getNetType", "mobile netType=MSG_4G_NETWORK_TYPE");
				return StorageModule.MSG_4G_NETWORK_TYPE;
			default:
				// 未知
				v("getNetType", "mobile netType=MSG_UNKOWN_NETWORK_TYPE");
				return StorageModule.MSG_UNKOWN_NETWORK_TYPE;
			}

		} else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			// 判断WIFI网
			v("getNetType", "netType=MSG_WIFI_NETWORK_TYPE");
			return StorageModule.MSG_WIFI_NETWORK_TYPE;
		}
		L.v(TAG, "getNetType",
				"non wifi and non mobile,netType=MSG_UNKOWN_NETWORK_TYPE");
		return StorageModule.MSG_UNKOWN_NETWORK_TYPE;
	}

	/**
	 * Log wrapper
	 * 
	 * @param type
	 * @param msg
	 */
	private static void v(String type, String msg) {
		L.v(TAG, type, msg);
	}

}
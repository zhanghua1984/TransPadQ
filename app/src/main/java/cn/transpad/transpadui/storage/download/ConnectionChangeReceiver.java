package cn.transpad.transpadui.storage.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Message;

import java.util.Locale;

import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 监听网络变化
 * 
 * @author wangyang
 * 
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
	private static final String TAG = ConnectionChangeReceiver.class
			.getSimpleName();
	public static final Uri CURRENT_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");

	@Override
	public synchronized void onReceive(Context context, Intent intent) {
		int netType = DownloadUtil.getNetType();
		Message msg = new Message();
		// switch (netType) {
		// case StorageModule.MSG_NO_NETWORK_TYPE:
		// break;
		// case StorageModule.MSG_WIFI_NETWORK_TYPE:
		// break;
		// default:
		// netType = StorageModule.MSG_UNWIFI_NETWORK_TYPE;
		// break;
		// }
		msg.what = netType;
		L.v(TAG, "netType=" + netType);
//		long preTime = SharedPreferenceModule.getInstance().getLong("preTime");
//		if (System.currentTimeMillis() - preTime >= 5000) {
//			SharedPreferenceModule.getInstance().setLong("preTime",
//					System.currentTimeMillis());
//		}
		EventBus.getDefault().post(msg);

	}

	/**
	 * 判断Network具体类型（联通移动wap，电信wap，其他net）
	 * 
	 */
	static int checkNetworkType(Context context) {
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo mobNetInfoActivity = connectivityManager
					.getActiveNetworkInfo();
			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
				// 注意一：
				// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
				// 但是有些电信机器，仍可以正常联网，
				// 所以当成net网络处理依然尝试连接网络。
				// （然后在socket中捕捉异常，进行二次判断与用户提示）。
				L.i(TAG, "checkNetworkType =====================>无网络");
				return StorageModule.MSG_NO_NETWORK_TYPE;
			} else {
				// NetworkInfo不为null开始判断是网络类型
				int netType = mobNetInfoActivity.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					// wifi net处理
					L.i(TAG, "checkNetworkType =====================>wifi网络");
					return StorageModule.MSG_WIFI_NETWORK_TYPE;
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					// 注意二：
					// 判断是否电信wap:
					// 不要通过getExtraInfo获取接入点名称来判断类型，
					// 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
					// 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
					// 所以可以通过这个进行判断！
					final Cursor cr = context.getContentResolver().query(
							CURRENT_APN_URI, null, null, null, null);
					try {
						if (cr != null) {
							cr.moveToFirst();
							final String user = cr.getString(cr
									.getColumnIndex("user"));
							if (user != null && !user.equals("")) {
								L.v(TAG,
										"checkNetworkType =====================>代理："
												+ cr.getString(cr
												.getColumnIndex("proxy")));
								if (user.startsWith(StorageModule.WAP_CT)) {
									L.i(TAG,
											"checkNetworkType =====================>电信wap网络");
									return StorageModule.MSG_DIANXIN_NETWORK_TYPE;
								}
							}
						}
					} finally {
						if (null != cr) {
							cr.close();
						}
					}

					// 注意三：
					// 判断是移动联通wap:
					// 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip
					// 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在
					// 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...
					// 所以采用getExtraInfo获取接入点名字进行判断
					String netMode = mobNetInfoActivity.getExtraInfo();
					L.v(TAG, "checkNetworkType netMode ================== "
							+ netMode);
					if (netMode != null) {
						// 通过apn名称判断是否是联通和移动wap
						netMode = netMode.toLowerCase(Locale.getDefault());
						if (netMode.equals(StorageModule.WAP_CM)) {
							L.i(TAG,
									"checkNetworkType =====================>移动wap网络");
							return StorageModule.MSG_YIDONG_NETWORK_TYPE;
						} else if (netMode.equals(StorageModule.WAP_UNI)
								|| netMode.equals(StorageModule.WAP_3G)) {
							L.i(TAG,
									"checkNetworkType =====================>联通wap网络");
							return StorageModule.MSG_LIANTONG_NETWORK_TYPE;
						}
					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 6;
		}
		return 5;
	}

}

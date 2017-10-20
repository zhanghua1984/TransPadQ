package cn.transpad.transpadui.storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import de.greenrobot.event.EventBus;

/**
 * 
 * 扫描过程监听
 * 
 * @author wangyang
 * @since 2014年5月4日
 */
public class ScanBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			// 开始扫描
			Message message = new Message();
			message.what = StorageModule.MSG_ACTION_MEDIA_SCANNER_STARTED;
			EventBus.getDefault().post(message);

		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			// 扫描完成
			Message message = new Message();
			message.what = StorageModule.MSG_ACTION_MEDIA_SCANNER_FINISHED;
			EventBus.getDefault().post(message);
		}
	}
}

package cn.transpad.transpadui.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AudioPlayerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){
            //SD卡移除了
        }
    }
}

package cn.transpad.transpadui.storage.download;

import android.content.Context;
import android.os.PowerManager;

import cn.transpad.transpadui.util.L;


/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and released in the
 * AlarmAlert activity
 */

public class CPUWakeLock {
	private static final String TAG = CPUWakeLock.class.getSimpleName();
	private static PowerManager.WakeLock sCpuWakeLock;

	/**
	 * ACQUIRE_CAUSES_WAKEUP==>Normal wake locks don't actually turn on the
	 * illumination. Instead, they cause the illumination to remain on once it
	 * turns on (e.g. from user activity). This flag will force the screen
	 * and/or keyboard to turn on immediately, when the WakeLock is acquired. A
	 * typical use would be for notifications which are important for the user
	 * to see immediately.
	 **/
	// PARTIAL_WAKE_LOCK On* Off Off
	// SCREEN_DIM_WAKE_LOCK On Dim Off
	// SCREEN_BRIGHT_WAKE_LOCK On Bright Off
	// FULL_WAKE_LOCK On Bright Bright
	public static void acquireCpuWakeLock(Context context) {
		L.v(TAG, "acquireCpuWakeLock", "start");
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = (sCpuWakeLock != null) ? sCpuWakeLock : pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,// Cpu on
				TAG);
		sCpuWakeLock.acquire();
		sCpuWakeLock.setReferenceCounted(false);
	}

	public static void releaseCpuWakeLock() {
		L.v(TAG, "releaseCpuWakeLock", "start");
		if (sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}
}

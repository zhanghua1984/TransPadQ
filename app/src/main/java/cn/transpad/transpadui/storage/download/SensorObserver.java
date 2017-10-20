package cn.transpad.transpadui.storage.download;

import java.util.HashMap;

public abstract interface SensorObserver{
	public void xyz_updated(HashMap<String, Object> event);
}

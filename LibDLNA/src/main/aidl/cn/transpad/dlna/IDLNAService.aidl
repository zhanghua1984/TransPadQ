// IDLNAService.aidl.aidl
package cn.transpad.dlna;

import cn.transpad.dlna.IDeviceChangedCallback;
import cn.transpad.dlna.entity.DLNADevice;
// Declare any non-default types here with import statements

interface IDLNAService {
	 void start();
	 void stop();
	 boolean isStarted();
	 void reSearchDevs();

	 boolean dlna_open(String uuid, String file,String title,String icon,int pos);
	 boolean dlna_openPicture(String playurl,String devuuid,String playTitle);
	 boolean dlna_openAudio(String playurl,String devuuid,String playTitle,String icon,int pos);
	 boolean dlna_pause(String uuid);
	 boolean dlna_resume(String uuid);
	 boolean dlna_seek(String uuid, long seconds);
	 boolean dlna_stop(String uuid);
	 boolean dlna_set_volume(String uuid, int volume);
	 int dlna_get_play_state(String uuid);
	 int dlna_get_play_volume(String uuid) ;
	 long dlna_get_duration(String uuid);
	 long dlna_get_play_curtime(String uuid);
	 List<DLNADevice> getDevices();
	 DLNADevice getDevice(String server_uid);
	 String getDeviceManufactur(String devuuid);

     void registerCallback(IDeviceChangedCallback cb);
     void unRegisterCallback(IDeviceChangedCallback cb);

}

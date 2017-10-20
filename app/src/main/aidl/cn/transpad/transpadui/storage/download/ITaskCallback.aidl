package cn.transpad.transpadui.storage.download;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Download;

interface ITaskCallback 
{   
	void sendAddSuccess(in List<OfflineCache> offlineCacheList);
	void sendStartAllSuccess();
	void sendPauseAllSuccess();
	void sendDownloading(in OfflineCache offlineCache);
	void sendFinish(in OfflineCache offlineCache);
	void sendDeleteSuccess();
} 
package cn.transpad.transpadui.storage.download;

import cn.transpad.transpadui.storage.download.ITaskCallback;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Download;
interface IDownloadService{

    int addFile(in Download download);
    
    int updateFile(in Download download);
    
    void pauseFile(in Download download);
    
    void deleteFile(in Download download);
    
	void addCacheList(in List<OfflineCache> offlineCacheList);
	
   void startCache(in OfflineCache offlineCache);
     
    void startCacheAll();

     void startAuto();

    void pauseCache(in OfflineCache offlineCache);
    
    void pauseCacheAll(int operateState);
    
    void deleteCache(in List<OfflineCache> offlineCacheList);
    
    void unregisterCallback(ITaskCallback cb);
    
    void registerCallback(ITaskCallback cb); 
    
    
    
}

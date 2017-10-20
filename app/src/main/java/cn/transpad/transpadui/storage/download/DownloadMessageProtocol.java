package cn.transpad.transpadui.storage.download;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.entity.Download;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.OfflineCacheModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

public class DownloadMessageProtocol {

    private static final String TAG = DownloadMessageProtocol.class
            .getSimpleName();

    public static final RemoteCallbackList<ITaskCallback> mCallbacks = new RemoteCallbackList<ITaskCallback>();

    /**
     * Log wrapper
     *
     * @param type
     * @param msg
     */
    public static void v(String type, String msg) {
        L.v(TAG, type, msg);
    }

    // /////////////////////////////////////////////////////
    // UI -------> service protocl //
    // ////////////////////////////////////////////////////

    public IBinder mServiceStub = new IDownloadService.Stub() {

        @Override
        public void addCacheList(List<OfflineCache> offlineCacheList)
                throws RemoteException {

            L.v(TAG, "addCacheList", "start size=" + offlineCacheList.size());
            for (OfflineCache offlineCache : offlineCacheList) {
                L.v(TAG, "addCacheList", "name=" + offlineCache.getCacheName());
            }
            // 添加下载
            OfflineCacheDownloadManager.getInstance().addList(offlineCacheList);

            // 返回消息
            sendAddSuccessMessage(offlineCacheList);

        }

        @Override
        public void pauseCache(OfflineCache offlineCache)
                throws RemoteException {

            L.v(TAG, "pauseCache", "start");

            OfflineCacheDownloadManager.getInstance().pause(offlineCache);

        }

        @Override
        public void startCache(OfflineCache offlineCache)
                throws RemoteException {

            L.v(TAG, "startCache", "start");

            OfflineCacheDownloadManager.getInstance().start(offlineCache);

        }

        @Override
        public void pauseCacheAll(int operateState) throws RemoteException {

            L.v(TAG, "pauseCacheAll", "start");

            OfflineCacheDownloadManager.getInstance().pauseAll(operateState);

            sendPauseAllSuccess();
        }

        @Override
        public void startCacheAll() throws RemoteException {

            L.v(TAG, "startCacheAll", "start");

            OfflineCacheDownloadManager.getInstance().startAll();

            sendStartAllSuccess();

        }

        @Override
        public void startAuto() throws RemoteException {

            L.v(TAG, "startAuto", "start");

            OfflineCacheDownloadManager.getInstance().startAuto();

        }

        @Override
        public void deleteCache(List<OfflineCache> offlineCacheList)
                throws RemoteException {

            L.v(TAG, "deleteCache", "start");

            OfflineCacheDownloadManager.getInstance().delete(offlineCacheList);

            sendDeleteSuccessMessage();
        }

        @Override
        public void unregisterCallback(ITaskCallback itaskCallback)
                throws RemoteException {

            if (itaskCallback != null) {

                mCallbacks.unregister(itaskCallback);

            } else {

                L.e(TAG, "unregisterCallback", "itaskCallback=null");

            }
        }

        @Override
        public void registerCallback(ITaskCallback itaskCallback)
                throws RemoteException {

            if (itaskCallback != null) {

                mCallbacks.register(itaskCallback);

            } else {

                L.e(TAG, "registerCallback", "itaskCallback=null");

            }

        }

        @Override
        public int addFile(Download download) throws RemoteException {

            L.v(TAG, "addFile", "start");

            //return AppDownloadManager.getInstance().addDownloadFile(download);
            return 0;
        }

        @Override
        public void pauseFile(Download download) throws RemoteException {

        }

        @Override
        public void deleteFile(Download download) throws RemoteException {

        }

        @Override
        public int updateFile(Download download) throws RemoteException {

            L.v(TAG, "updateFile", "start");

            //return AppDownloadManager.getInstance()
            //      .updateDownloadFile(download);
            return 0;
        }
    };

    // /////////////////////////////////////////////////////
    // service -------> UI protocl //
    // ////////////////////////////////////////////////////

    public static ITaskCallback mCallback = new ITaskCallback.Stub() {
        @Override
        public void sendFinish(OfflineCache offlineCache)
                throws RemoteException {
            L.v(TAG,
                    "sendFinish",
                    "start MSG_DOWNLOAD_CACHE_SUCCESS cid="
                            + offlineCache.getCacheID() + " name="
                            + offlineCache.getCacheName());

            Message msg = new Message();
            msg.what = StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS;
            Bundle bundle = new Bundle();
            bundle.putParcelable(OfflineCache.OFFLINE_CACHE,
                    (OfflineCache) offlineCache);
            msg.setData(bundle);
            EventBus.getDefault().post(msg);
        }

        @Override
        public void sendDownloading(OfflineCache offlineCache)
                throws RemoteException {

            OfflineCacheModule.getInstance().mergeOfflineCache(offlineCache);

        }

        @Override
        public void sendDeleteSuccess() throws RemoteException {

            L.v(TAG, "sendDeleteSuccess", "start");
            // 通知UI,删除成功
            Message message = new Message();
            message.what = StorageModule.MSG_DELETE_CACHE_SUCCESS;
            EventBus.getDefault().post(message);

            // 通知内部组件,删除成功
            Message msg = new Message();
            msg.what = OfflineCacheModule.MSG_INTERNAL_FILE_CACHE_DELETE_SUCCESS;
            EventBus.getDefault().post(msg);
        }

        @Override
        public void sendStartAllSuccess() throws RemoteException {

            L.v(TAG, "sendStartAllSuccess", "start");

            Message msg = new Message();
            msg.what = StorageModule.MSG_CACHE_START_ALL_SUCCESS;
            EventBus.getDefault().post(msg);
        }

        @Override
        public void sendPauseAllSuccess() throws RemoteException {

            L.v(TAG, "sendPauseAllSuccess", "start");

            Message msg = new Message();
            msg.what = StorageModule.MSG_CACHE_PAUSE_ALL_SUCCESS;
            EventBus.getDefault().post(msg);

        }

        @Override
        public void sendAddSuccess(List<OfflineCache> offlineCacheList)
                throws RemoteException {

            L.v(TAG, "sendAddSuccess", "start(main process) size="
                    + offlineCacheList.size());

            // 发送成功消息
            Message msg = new Message();
            msg.what = StorageModule.MSG_ADD_CACHE_SUCCESS;
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST,
                    (ArrayList<OfflineCache>) offlineCacheList);
            msg.setData(bundle);
            EventBus.getDefault().post(msg);

            // 发送详情页成功消息
//            VideoPlayerMessage videoPlayerMessage = new VideoPlayerMessage();
//            videoPlayerMessage.what = StorageModule.MSG_ADD_CACHE_SUCCESS;
//            EventBus.getDefault().post(videoPlayerMessage);
        }

    };

    /**
     * 发送正在下载消息(同步方法,beginBroadcast和finishBroadcast配对处理,否则会报错)
     *
     * @param offlineCache 正在下载集合
     */
    public void sendDownloadingMessage(OfflineCache offlineCache) {

        if (mCallbacks != null) {
            try {
                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        mCallbacks.getBroadcastItem(i).sendDownloading(
                                offlineCache);

                    }

                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendDownloadingMessage", "mCallbacks=null");
        }
    }

    /**
     * 发送下载完成消息
     *
     * @param offlineCache 下载完成集合
     */
    public void sendFinishMessage(OfflineCache offlineCache) {

        L.v(TAG, "sendFinishMessage", "start");

        if (mCallbacks != null) {
            try {
                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        mCallbacks.getBroadcastItem(i).sendFinish(offlineCache);

                    }
                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendDownloadFinishMessage", "mCallbacks=null");
        }

    }

    /**
     * 发送删除成功消息
     */
    public void sendDeleteSuccessMessage() {

        L.v(TAG, "sendDeleteSuccessMessage", "start");

        if (mCallbacks != null) {
            try {
                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        mCallbacks.getBroadcastItem(i).sendDeleteSuccess();

                    }
                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendDownloadDeleteFinishMessage", "mCallbacks=null");
        }
    }

    /**
     * 发送开始全部缓存成功消息
     */
    public void sendStartAllSuccess() {

        L.v(TAG, "sendStartAllSuccess", "start");

        if (mCallbacks != null) {
            try {

                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        mCallbacks.getBroadcastItem(i).sendStartAllSuccess();

                    }
                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendStartAllSuccess", "mCallbacks=null");
        }
    }

    /**
     * 发送暂停全部缓存成功消息
     */
    public void sendPauseAllSuccess() {
        L.v(TAG, "sendPauseAllSuccess", "start");
        if (mCallbacks != null) {
            try {
                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        mCallbacks.getBroadcastItem(i).sendPauseAllSuccess();

                    }
                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendPauseAllSuccess", "mCallbacks=null");
        }
    }

    /**
     * 发送添加任务成功消息
     */
    public void sendAddSuccessMessage(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "sendAddSuccessMessage", "start(cache service process) size="
                + offlineCacheList.size());
        if (mCallbacks != null) {
            try {
                synchronized (mCallbacks) {
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {

                        L.v(TAG, "sendAddSuccessMessage",
                                "getBroadcastItem start(cache service process)");
                        mCallbacks.getBroadcastItem(i).sendAddSuccess(
                                offlineCacheList);

                    }
                    mCallbacks.finishBroadcast();
                }
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "sendAddSuccessMessage", "mCallbacks=null");
        }

    }

}

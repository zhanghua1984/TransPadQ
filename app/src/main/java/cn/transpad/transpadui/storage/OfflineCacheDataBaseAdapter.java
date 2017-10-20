package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.util.L;


/**
 * 缓存文件表适配器
 *
 * @author wangyang
 * @since 2014年4月29日
 */
public class OfflineCacheDataBaseAdapter implements IFoneDatabase {
    private static final String TAG = OfflineCacheDataBaseAdapter.class
            .getSimpleName();
    private static final OfflineCacheDataBaseAdapter mInstance = new OfflineCacheDataBaseAdapter();
    private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
            .getInstance();

    private OfflineCacheDataBaseAdapter() {

    }

    public static OfflineCacheDataBaseAdapter getInstance() {
        return mInstance;
    }


    public int addOfflineCacheFileList(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "addOfflineCacheFileList", "start size=" + offlineCacheList.size());
        int count = 0;
        try {

            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            for (OfflineCache offlineCache : offlineCacheList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(OFFLINE_CACHE_FILE_ID,
                        offlineCache.getCacheID());
                contentValues.put(OFFLINE_CACHE_FILE_IMAGE_URL,
                        offlineCache.getCacheImageUrl());
                contentValues.put(OFFLINE_CACHE_FILE_NAME,
                        offlineCache.getCacheName());
                contentValues.put(OFFLINE_CACHE_FILE_VERSION_CODE,
                        offlineCache.getCacheVersionCode());
                contentValues.put(OFFLINE_CACHE_FILE_PACKAGE_NAME,
                        offlineCache.getCachePackageName());
                contentValues.put(OFFLINE_CACHE_FILE_KEYWORD,
                        offlineCache.getCacheKeyword());
                contentValues.put(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE,
                        offlineCache.getCacheDownloadType());
                contentValues.put(OFFLINE_CACHE_FILE_DOWNLOAD_STATE,
                        offlineCache.getCacheDownloadState());
                contentValues.put(OFFLINE_CACHE_FILE_ALREADY_SIZE,
                        offlineCache.getCacheAlreadySize());
                contentValues.put(OFFLINE_CACHE_FILE_TOTAL_SIZE,
                        offlineCache.getCacheTotalSize());
                contentValues.put(OFFLINE_CACHE_FILE_STORAGE_PATH,
                        offlineCache.getCacheStoragePath());
                contentValues.put(OFFLINE_CACHE_FILE_DETAIL_URL,
                        offlineCache.getCacheDetailUrl());
                contentValues.put(OFFLINE_CACHE_FILE_CREATE_TIME,
                        System.currentTimeMillis());
                contentValues.put(OFFLINE_CACHE_FILE_ERROR_CODE,
                        offlineCache.getCacheErrorCode());
                contentValues.put(OFFLINE_CACHE_FILE_IS_INSTALL,
                        offlineCache.getCacheIsInstall() ? 1 : 0);
                // 重复的文件夹做忽略处理
                long result = mGeneralDataBaseTemplate.insertWithOnConflict(
                        TB_OFFLINE_CACHE_FILE, null, contentValues,
                        SQLiteDatabase.CONFLICT_IGNORE);
                L.v(TAG, "addOfflineCacheFileList", "result=" + result);
            }
            mGeneralDataBaseTemplate.setTransactionSuccessful();
            count = 1;
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        } finally {
            mGeneralDataBaseTemplate.endTransaction();
        }
        return count;
    }

    /**
     * 获取缓存对象集合
     *
     * @return List<OfflineCache> 缓存对象
     */
    public ArrayList<OfflineCache> getOfflineCacheList() {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(TB_OFFLINE_CACHE_FILE);
        sql.append(" where ");
        sql.append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE);
        sql.append("!=");
        sql.append(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
        sql.append(" order by ");
        sql.append(OFFLINE_CACHE_FILE_CREATE_TIME);
        sql.append(" asc ");
        OfflineCache offlineCache = null;
        Cursor cursor = null;
        ArrayList<OfflineCache> offlineCacheList = new ArrayList<>();
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            while (cursor.moveToNext()) {
                offlineCache = getOfflineCacheFileByCursor(cursor);
                offlineCacheList.add(offlineCache);
            }
        } catch (Exception e) {
            // TODO: handle exception
            L.v(TAG, "getOfflineCacheList", "e=" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return offlineCacheList;
    }

    /**
     * 根据缓存Id获取缓存对象
     *
     * @return OfflineCache 缓存对象
     */
    public OfflineCache getOfflineCacheById(long Id) {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(TB_OFFLINE_CACHE_FILE);
        sql.append(" where ");
        sql.append(OFFLINE_CACHE_FILE_ID);
        sql.append("=");
        sql.append(Id);
        OfflineCache offlineCache = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                L.v(TAG, "getOfflineCacheById", "cursor=null");
                return null;
            }

            // 读取数据
            while (cursor.moveToNext()) {
                offlineCache = getOfflineCacheFileByCursor(cursor);
                File file = new File(offlineCache.getCacheStoragePath());
                if (!file.exists()) {
                    deleteOfflineCache(offlineCache);
                    offlineCache = null;
                }
            }
            L.v(TAG, "getOfflineCacheById", "offlineCache=" + offlineCache);
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return offlineCache;
    }

    /**
     * 根据缓存CID获取缓存对象
     *
     * @param Id 缓存Id
     * @return OfflineCache 缓存对象
     */
    public boolean isOfflineCacheById(long Id) {
        boolean result = false;
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(TB_OFFLINE_CACHE_FILE);
        sql.append(" where ");
        sql.append(OFFLINE_CACHE_FILE_ID);
        sql.append("=");
        sql.append(Id);
        sql.append(" and ");
        sql.append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE).append("!=").append(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                result = false;
            } else {
                if (cursor.getCount() > 0) {
                    result = true;
                } else {
                    result = false;
                }
            }

        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return result;
    }

    /**
     * 获取已缓存文件列表的数量
     *
     * @return int 已缓存文件数量
     */
    public int getOfflineCacheFileFinishCount() {
        int result = 0;
        StringBuilder sqlFolder = new StringBuilder("select * from ");
        sqlFolder.append(TB_OFFLINE_CACHE_FILE);
        sqlFolder.append(" where ");
        sqlFolder.append(OFFLINE_CACHE_FILE_DOWNLOAD_STATE);
        sqlFolder.append("=");
        sqlFolder.append(OfflineCache.CACHE_STATE_FINISH);
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sqlFolder.toString());

            if (cursor != null) {

                result = cursor.getCount();

            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        // 由C层返回(暂时不用,用的时候再解开,调用CacheDownloadManager.getInstance().getCacheFinishCount())
        // result = SharedPreferenceModule.getInstance().getInt(
        // OfflineCache.OFFLINE_CACHE_FINISH_COUNT);
        return result;
    }

    /**
     * 获取文件列表的总数量
     *
     * @return int 文件数量
     */
    public int getOfflineCacheFileCount() {
        int result = 0;
        StringBuilder sqlFolder = new StringBuilder("select * from ");
        sqlFolder.append(TB_OFFLINE_CACHE_FILE);
        sqlFolder.append(" where ");
        sqlFolder.append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE);
        sqlFolder.append("!=");
        sqlFolder.append(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
        Cursor cursor = null;
        int count = 0;
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sqlFolder.toString());

            if (cursor != null) {
                List<OfflineCache> deleteList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor
                            .getColumnIndex(OFFLINE_CACHE_FILE_ID));
                    String path = cursor.getString(cursor
                            .getColumnIndex(OFFLINE_CACHE_FILE_STORAGE_PATH));
                    File file = new File(path);
                    if (file.exists()) {
                        count++;
                    } else {
                        OfflineCache offlineCache = new OfflineCache();
                        offlineCache.setCacheID(id);
                        deleteList.add(offlineCache);
                    }
                }
                if (deleteList.size() > 0) {
                    deleteOfflineCacheList(deleteList);
                }

            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return count;
    }

    /**
     * 获取未缓存完成文件列表
     *
     * @return int 未缓存文件列表
     */
    public List<OfflineCache> getOfflineCacheFileNotFinishList() {

        StringBuilder sqlFolder = new StringBuilder("select * from ");
        sqlFolder.append(TB_OFFLINE_CACHE_FILE);
        sqlFolder.append(" where ");
        sqlFolder.append(OFFLINE_CACHE_FILE_DOWNLOAD_STATE);
        sqlFolder.append("!=");
        sqlFolder.append(OfflineCache.CACHE_STATE_FINISH);
        sqlFolder.append(" and ");
        sqlFolder.append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE);
        sqlFolder.append("!=");
        sqlFolder.append(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
        Cursor cursor = null;
        List<OfflineCache> offlineCacheList = new ArrayList<OfflineCache>();
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sqlFolder.toString());

            if (cursor == null) {
                return offlineCacheList;
            }
            // 读取数据
            while (cursor.moveToNext()) {
                OfflineCache offlineCache = getOfflineCacheFileByCursor(cursor);
                offlineCacheList.add(offlineCache);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return offlineCacheList;
    }

    /**
     * 根据缓存文件Id列表删除文件对象 <br>
     * 同步方法
     *
     * @param offlineCacheList 缓存列表
     * @return int 操作结果<br>
     * 1 删除成功<br>
     * -1 删除异常<br>
     */
    public int deleteOfflineCacheList(
            List<OfflineCache> offlineCacheList) {
        L.v(TAG, "deleteOfflineCacheList", "start size=" + offlineCacheList.size());
        int result = 0;
        try {

            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            for (OfflineCache offlineCacheFile : offlineCacheList) {
                StringBuilder where = new StringBuilder();
                where.append(OFFLINE_CACHE_FILE_ID).append("=?");
                int number = mGeneralDataBaseTemplate.delete(TB_OFFLINE_CACHE_FILE, where
                        .toString(), new String[]{String
                        .valueOf(offlineCacheFile.getCacheID())});
                result = number > 0 ? 0 : -1;
                L.v(TAG, "deleteOfflineCacheList", "number=" + number);
            }
            mGeneralDataBaseTemplate.setTransactionSuccessful();
        } catch (Exception e) {
            result = -1;
        } finally {

            mGeneralDataBaseTemplate.endTransaction();
            mGeneralDataBaseTemplate.close();
        }
        return result;
    }

    public int deleteOfflineCache(
            OfflineCache offlineCache) {
        L.v(TAG, "deleteOfflineCache", "start");
        int result = 0;
        try {
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            StringBuilder where = new StringBuilder();
            where.append(OFFLINE_CACHE_FILE_ID).append("=?");
            int number = mGeneralDataBaseTemplate.delete(TB_OFFLINE_CACHE_FILE, where
                    .toString(), new String[]{String
                    .valueOf(offlineCache.getCacheID())});
            result = number > 0 ? 0 : -1;
            L.v(TAG, "deleteOfflineCache", "number=" + number);
            mGeneralDataBaseTemplate.setTransactionSuccessful();
        } catch (Exception e) {
            result = -1;
        } finally {

            mGeneralDataBaseTemplate.endTransaction();
            mGeneralDataBaseTemplate.close();
        }
        return result;
    }

    /**
     * 删除下载类型的缓存对象 <br>
     * 同步方法
     *
     * @param downloadType 缓存类型
     * @return int 操作结果<br>
     * 1 删除成功<br>
     * -1 删除异常<br>
     */
    public int deleteOfflineCacheByDownloadType(
            int downloadType) {
        L.v(TAG, "deleteOfflineCacheByDownloadType", "start downloadType=" + downloadType);
        int result = 0;
        try {
            mGeneralDataBaseTemplate.open();
            StringBuilder where = new StringBuilder();
            where.append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE).append("=?");
            int number = mGeneralDataBaseTemplate.delete(TB_OFFLINE_CACHE_FILE, where
                    .toString(), new String[]{String
                    .valueOf(downloadType)});
            result = number > 0 ? 0 : -1;
            L.v(TAG, "deleteOfflineCacheByDownloadType", "number=" + number);
        } catch (Exception e) {
            result = -1;
        } finally {
            mGeneralDataBaseTemplate.close();
        }
        return result;
    }

    /**
     * 批量更新文件表进度
     *
     * @param offlineCache 缓存文件列表
     * @return int 0失败,1成功
     */
    public synchronized int updateOfflineCacheFileProgress(
            OfflineCache offlineCache) {
        int count = 0;
        try {
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            String where = OFFLINE_CACHE_FILE_ID + "=?";
            String[] whereValue = {String.valueOf(offlineCache.getCacheID())};

            ContentValues contentValues = new ContentValues();
            contentValues.put(OFFLINE_CACHE_FILE_DOWNLOAD_STATE,
                    offlineCache.getCacheDownloadState());
            contentValues.put(OFFLINE_CACHE_FILE_ALREADY_SIZE,
                    offlineCache.getCacheAlreadySize());
            contentValues.put(OFFLINE_CACHE_FILE_TOTAL_SIZE,
                    offlineCache.getCacheTotalSize());
            count = mGeneralDataBaseTemplate.update(TB_OFFLINE_CACHE_FILE,
                    contentValues, where, whereValue);
            mGeneralDataBaseTemplate.setTransactionSuccessful();
            count = 1;
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        } finally {
            mGeneralDataBaseTemplate.endTransaction();
        }
        return count;
    }

    /**
     * 单个更新文件表
     *
     * @param offlineCache 缓存文件
     * @return
     */
    public int updateOfflineCacheFile(OfflineCache offlineCache) {
        L.v(TAG,
                "updateOfflineCacheFile",
                "start name=" + offlineCache.getCacheName()
                        + " downloadState="
                        + offlineCache.getCacheDownloadState());
        String where = OFFLINE_CACHE_FILE_ID + "=?";
        String[] whereValue = {
                String.valueOf(offlineCache.getCacheID())};

        ContentValues contentValues = new ContentValues();
        contentValues.put(OFFLINE_CACHE_FILE_DOWNLOAD_STATE,
                offlineCache.getCacheDownloadState());
        contentValues.put(OFFLINE_CACHE_FILE_ALREADY_SIZE,
                offlineCache.getCacheAlreadySize());
        if (offlineCache.getCacheTotalSize() != 0) {
            contentValues.put(OFFLINE_CACHE_FILE_TOTAL_SIZE,
                    offlineCache.getCacheTotalSize());
        }
        contentValues.put(OFFLINE_CACHE_FILE_STORAGE_PATH,
                offlineCache.getCacheStoragePath());
        contentValues.put(OFFLINE_CACHE_FILE_ERROR_CODE,
                offlineCache.getCacheErrorCode());
        int count = mGeneralDataBaseTemplate.update(TB_OFFLINE_CACHE_FILE,
                contentValues, where, whereValue);
        return count;
    }

    /**
     * 批量更新文件表
     *
     * @param offlineCacheList 缓存文件列表
     * @return int 0失败,1成功
     */
    public int updateOfflineCacheFileList(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "updateOfflineCacheFileList", "start offlineCacheList.size="
                + offlineCacheList.size());
        int count = 0;
        try {
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            for (OfflineCache offlineCache : offlineCacheList) {
                String where = OFFLINE_CACHE_FILE_ID + "=?";
                String[] whereValue = {
                        String.valueOf(offlineCache.getCacheID())};

                ContentValues contentValues = new ContentValues();
                contentValues.put(OFFLINE_CACHE_FILE_DOWNLOAD_STATE,
                        offlineCache.getCacheDownloadState());
                contentValues.put(OFFLINE_CACHE_FILE_ALREADY_SIZE,
                        offlineCache.getCacheAlreadySize());
                contentValues.put(OFFLINE_CACHE_FILE_TOTAL_SIZE,
                        offlineCache.getCacheTotalSize());
                contentValues.put(OFFLINE_CACHE_FILE_STORAGE_PATH,
                        offlineCache.getCacheStoragePath());
                contentValues.put(OFFLINE_CACHE_FILE_ERROR_CODE,
                        offlineCache.getCacheErrorCode());
                count = mGeneralDataBaseTemplate.update(TB_OFFLINE_CACHE_FILE,
                        contentValues, where, whereValue);
            }
            mGeneralDataBaseTemplate.setTransactionSuccessful();
            count = 1;
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        } finally {
            mGeneralDataBaseTemplate.endTransaction();
            mGeneralDataBaseTemplate.close();
        }
        return count;
    }

    /**
     * 根据游标读取字段信息
     *
     * @param cursor
     * @return OfflineCache
     */
    private OfflineCache getOfflineCacheFileByCursor(Cursor cursor) {
        OfflineCache offlineCache = new OfflineCache();
        offlineCache.setCacheID(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_ID)));
        offlineCache.setCacheName(cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_NAME)));
        offlineCache.setCacheImageUrl(cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_IMAGE_URL)));
        offlineCache.setCacheVersionCode(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_VERSION_CODE)));
        offlineCache.setCachePackageName(cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_PACKAGE_NAME)));
        offlineCache.setCacheKeyword(cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_KEYWORD)));
        offlineCache.setCacheTotalSize(cursor.getLong(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_TOTAL_SIZE)));
        offlineCache.setCacheDownloadState(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_DOWNLOAD_STATE)));
        offlineCache.setCacheDownloadType(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE)));
        offlineCache.setCacheDetailUrl(cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_DETAIL_URL)));
        offlineCache.setCacheErrorCode(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_ERROR_CODE)));
        offlineCache.setCacheIsInstall(cursor.getInt(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_IS_INSTALL)) == 1);
        String path = cursor.getString(cursor
                .getColumnIndex(OFFLINE_CACHE_FILE_STORAGE_PATH));
        offlineCache.setCacheStoragePath(path);
        if (path != null && !path.equals("") && !path.equals("null")) {
            File file = new File(path);
            if (file.exists()) {
                offlineCache.setCacheAlreadySize(file.length());
            }
        }

        return offlineCache;
    }
}

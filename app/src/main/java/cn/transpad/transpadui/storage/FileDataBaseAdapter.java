package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 文件表适配器
 *
 * @author wangyang
 * @since 2014年4月29日
 */
class FileDataBaseAdapter implements IFoneDatabase {
    private static final String TAG = FileDataBaseAdapter.class.getSimpleName();
    private static final FileDataBaseAdapter mInstance = new FileDataBaseAdapter();
    private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
            .getInstance();
    private static Context sContext = null;

    private FileDataBaseAdapter() {
    }

    static void init(Context context) {
        sContext = context;
    }

    static FileDataBaseAdapter getInstance() {
        return mInstance;
    }

    public boolean addFullMediaFileList(List<MediaFile> fileList) {
        L.v(TAG, "addFullMediaFileList", "start");
        // 需要添加的集合
        ArrayList<MediaFile> addMediaFileList = new ArrayList<MediaFile>();
        // 需要删除的集合
        ArrayList<String> deleteMediaFileList = new ArrayList<String>();
        // 临时表
        HashMap<String, String> mediaPathHashMap = new HashMap<String, String>();

        StringBuilder folderListSql = new StringBuilder("select * from ");
        folderListSql.append(TB_DIRECTORY_TREE);
        mGeneralDataBaseTemplate.open();
        Cursor cursor = mGeneralDataBaseTemplate.select(folderListSql
                .toString());
        if (cursor != null && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor
                        .getColumnIndex(DIRECTORY_PATH));
                if (path != null && new File(path).exists()) {
                    mediaPathHashMap.put(path, path);
                } else {
                    deleteMediaFileList.add(path);
                }
            }
            cursor.close();
        }
        // 文件
        for (MediaFile mediaFile : fileList) {
            String path = mediaFile.getMediaFilePath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    if (!mediaPathHashMap.containsKey(path)) {
                        // 添加
                        addMediaFileList.add(mediaFile);
                    }
                }
            }
        }

        //删除
        if (deleteMediaFileList.size() > 0) {
            deleteFileList(deleteMediaFileList);
        }

        // 添加

        boolean isFirstStart = SharedPreferenceModule.getInstance().getBoolean("is_first_start", true);
        if (isFirstStart) {
            SharedPreferenceModule.getInstance().setBoolean("is_first_start", false);
            if (addMediaFileList.size() != 0) {
                addFileList(addMediaFileList);
            }
            L.v(TAG, "addFullMediaFileList", "isVisible=true");
        } else {
            Message message = new Message();
            message.what = StorageModule.MSG_SCANNER_FILE_LIST_SUCCESS;
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST, addMediaFileList);
            message.setData(bundle);
            EventBus.getDefault().post(message);
            L.v(TAG, "addFullMediaFileList", "isVisible=false");
        }

        return true;
    }

    public boolean addFastMediaFileList(List<MediaFile> fileList) {
        L.v(TAG, "addFastMediaFileList", "fileList.size=" + fileList.size());
        // 需要添加的集合
        ArrayList<MediaFile> addMediaFileList = new ArrayList<MediaFile>();
        // 临时表
        HashMap<String, String> mediaPathHashMap = new HashMap<String, String>();

        StringBuilder folderListSql = new StringBuilder("select * from ");
        folderListSql.append(TB_DIRECTORY_TREE);
        mGeneralDataBaseTemplate.open();
        Cursor cursor = mGeneralDataBaseTemplate.select(folderListSql
                .toString());
        if (cursor != null && cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor
                        .getColumnIndex(DIRECTORY_PATH));
                if (path != null) {
                    mediaPathHashMap.put(path, path);
                }
            }
            cursor.close();
        }
        // 文件
        for (MediaFile mediaFile : fileList) {
            String path = mediaFile.getMediaFilePath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    if (!mediaPathHashMap.containsKey(path)) {
                        // 添加
                        addMediaFileList.add(mediaFile);
                    }
                }
            }
        }

        //插入
        addFileList(addMediaFileList);
        return true;
    }

    public void addFileList(List<MediaFile> fileList) {
        mGeneralDataBaseTemplate.open();
        for (MediaFile mediaFile : fileList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DIRECTORY_NAME,
                    mediaFile.getMediaFileName());
            contentValues.put(DIRECTORY_PARENT_NAME,
                    mediaFile.getMediaFileParentName());
            contentValues.put(DIRECTORY_PATH,
                    mediaFile.getMediaFilePath());
            contentValues.put(DIRECTORY_AUTHOR,
                    mediaFile.getMediaFileAuthor());
            contentValues.put(DIRECTORY_DATE_MODIFIED,
                    mediaFile.getMediaFileDateModified());
            contentValues.put(DIRECTORY_DURATION,
                    mediaFile.getMediaFileDuration());
            contentValues.put(DIRECTORY_TYPE,
                    mediaFile.getMediaFileDirectoryType());
            contentValues.put(FILE_TYPE,
                    mediaFile.getMediaFileType());
            contentValues.put(FILE_SIZE,
                    mediaFile.getMediaFileSize());
            mGeneralDataBaseTemplate.insert(TB_DIRECTORY_TREE,
                    null, contentValues);
        }
    }

    /**
     * 根据文件路径删除文件 <br>
     * 同步方法
     *
     * @param filePath 文件路径
     * @return int 操作结果<br>
     * 1 成功<br>
     * -1 删除异常<br>
     */
    public int deleteFileByFilePath(String filePath) {
        List<String> deleteList = new ArrayList<String>();
        deleteList.add(filePath);
        return deleteFileList(deleteList);
    }

    /**
     * 删除文件列表
     *
     * @param pathList 文件集合
     * @return int 操作状态
     */
    public int deleteFileList(List<String> pathList) {
        synchronized (TAG) {

            try {

                mGeneralDataBaseTemplate.open();

                mGeneralDataBaseTemplate.beginTransaction();

                // 创建sql语句
                for (String path : pathList) {

                    // 删除数据库
                    StringBuilder where = new StringBuilder();
                    where.append(DIRECTORY_PATH + "=?");
                    mGeneralDataBaseTemplate.delete(TB_DIRECTORY_TREE,
                            where.toString(), new String[]{path});
                    // 删除媒体库
                    sContext.getContentResolver().delete(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "=?",
                            new String[]{path});

                    // 删除文件
                    File deleteFile = new File(path);
                    if (deleteFile.exists()) {
                        deleteFile.delete();
                    }
                }

                mGeneralDataBaseTemplate.setTransactionSuccessful();

            } catch (Exception e) {
                e.printStackTrace();
                L.v(TAG, e.getMessage());
                return -1;
            } finally {
                mGeneralDataBaseTemplate.endTransaction();
                mGeneralDataBaseTemplate.close();
            }
            return 1;
        }
    }

    /**
     * 更新文件列表
     *
     * @param fileList 文件集合
     * @return boolean 是否成功
     */
    public boolean updateFileList(List<MediaFile> fileList) {

        try {

            mGeneralDataBaseTemplate.open();

            mGeneralDataBaseTemplate.beginTransaction();

            // 文件

            for (MediaFile mediaFile : fileList) {

                // 创建文件sql语句
                ContentValues contentValues = new ContentValues();
                contentValues.put(DIRECTORY_DATE_MODIFIED,
                        System.currentTimeMillis());
                contentValues.put(DIRECTORY_NAME,
                        mediaFile.getMediaFileName());
                contentValues.put(DIRECTORY_PARENT_NAME,
                        mediaFile.getMediaFileParentName());
                contentValues.put(DIRECTORY_AUTHOR,
                        mediaFile.getMediaFileAuthor());

                //重命名
//                String oldPath = mediaFile.getMediaFilePath();
//                File file = new File(oldPath);
//                if (file.exists()) {
//                    String fileName = file.getName();
//                    String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(
//                            Locale.getDefault());
//                    File newFile = new File(file.getParent() + File.separator + mediaFile.getMediaFileName() + extension);
//                    file.renameTo(newFile);
//                    mediaFile.setMediaFilePath(newFile.getAbsolutePath());
//                }
//                contentValues.put(DIRECTORY_PATH,
//                        mediaFile.getMediaFilePath());
                StringBuilder where = new StringBuilder();
                where.append(DIRECTORY_PATH).append("=?");
                mGeneralDataBaseTemplate.update(TB_DIRECTORY_TREE,
                        contentValues, where.toString(),
                        new String[]{mediaFile.getMediaFilePath()});
            }

            mGeneralDataBaseTemplate.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            L.v(TAG, e.getMessage());
            return false;
        } finally {
            mGeneralDataBaseTemplate.endTransaction();
            mGeneralDataBaseTemplate.close();
        }
        return true;
    }


    /**
     * 根据文件夹路径获得文件集合
     *
     * @param mediaType 列表类型
     * @return ArrayList<MediaFile> 文件集合
     */
    public ArrayList<MediaFile> getCustomMediaFileList(String parentName, int mediaType) {
        StringBuilder sql = new StringBuilder("select * from "
                + TB_DIRECTORY_TREE + " where ");
        sql.append(DIRECTORY_PARENT_NAME + "='" + parentName + "'");
        sql.append(" and ");
        //目录类型
        sql.append(DIRECTORY_TYPE + "=").append(MediaFile.MEDIA_FILE_TYPE);
        sql.append(" and ");
        //文件类型
        sql.append(FILE_TYPE + "=").append(mediaType);
        ArrayList<MediaFile> mediaFileList = null;
        Cursor cursor = null;
        try {

            mGeneralDataBaseTemplate.open();

            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            mediaFileList = new ArrayList<MediaFile>();
            while (cursor.moveToNext()) {
                MediaFile mediaFile = getMediaFileByCursor(cursor);
                if (mediaFile != null) {
                    mediaFileList.add(mediaFile);
                }
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        return mediaFileList;
    }

    /**
     * 获得目录集合(包括文件和文件夹)
     *
     * @param parames 0目录类型;1文件类型;2是否加密
     * @return ArrayList<MediaFile> 目录集合
     */
    public ArrayList<MediaFile> getMediaFileList(int... parames) {
        int mediaDirectoryType = parames[0];
        int fileIsVisible = 0;
        StringBuilder sqlFile = new StringBuilder("select * from "
                + TB_DIRECTORY_TREE + " where ");
        //目录类型
        sqlFile.append(DIRECTORY_TYPE + "=").append(mediaDirectoryType);

        switch (mediaDirectoryType) {
            case MediaFile.MEDIA_FILE_TYPE:
                int mediaFileType = parames[1];
                sqlFile.append(" and ");
                //文件类型
                switch (mediaFileType) {
                    case MediaFile.MEDIA_VIDEO_TYPE:
                        sqlFile.append(FILE_TYPE + " in (").append(mediaFileType);
                        sqlFile.append(",");
                        sqlFile.append(MediaFile.MEDIA_VIDEO_100TV_TYPE + ")");
                        break;
                    default:
                        sqlFile.append(FILE_TYPE + "=").append(mediaFileType);
                        break;
                }
                break;
        }
//        switch (mediaDirectoryType) {
//            case MediaFile.MEDIA_FILE_TYPE:
//                int isEncrypt = parames[2];
//                sqlFile.append(" and ");
//                //是否加密
//                sqlFile.append(FILE_IS_ENCRYPT + "=").append(isEncrypt);
//                break;
//        }
        ArrayList<MediaFile> mediaFileList = new ArrayList<MediaFile>();
        ArrayList<String> deleteMediaFileList = new ArrayList<String>();
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();
            L.v(TAG, "getMediaFileList", "sqlFile=" + sqlFile.toString());
            cursor = mGeneralDataBaseTemplate.select(sqlFile.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            while (cursor.moveToNext()) {
                MediaFile mediaFile = getMediaFileByCursor(cursor);
                if (mediaFile != null) {
                    mediaFileList.add(mediaFile);
                } else {
                    String path = cursor.getString(cursor
                            .getColumnIndex(DIRECTORY_PATH));
                    deleteMediaFileList.add(path);
                }
            }

            if (deleteMediaFileList.size() > 0) {
                deleteFileList(deleteMediaFileList);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        return mediaFileList;
    }

    /**
     * 获得目录集合
     *
     * @return List<MediaFile> 目录集合
     */
    public ArrayList<MediaFile> getUnAddMediaFileList() {
        StringBuilder sql = new StringBuilder("select * from "
                + TB_DIRECTORY_TREE + " where ");
        sql.append(FILE_IS_VISIBLE + "=0");
        Cursor cursor = null;
        ArrayList<MediaFile> mediaFileList = new ArrayList<>();
        try {
            mGeneralDataBaseTemplate.open();
            cursor = mGeneralDataBaseTemplate.select(sql.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            while (cursor.moveToNext()) {
                MediaFile mediaFile = getMediaFileByCursor(cursor);
                if (mediaFile != null) {
                    mediaFileList.add(mediaFile);
                }
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }
        return mediaFileList;
    }

    /**
     * 更新显示状态
     *
     * @param mediaFileList 文件路径列表
     * @return int
     */
    int updateVisibleState(List<MediaFile> mediaFileList) {
        int result = 1;
        try {
            mGeneralDataBaseTemplate.open();
            mGeneralDataBaseTemplate.beginTransaction();
            for (MediaFile mediaFile : mediaFileList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FILE_IS_VISIBLE, "1");
                StringBuilder where = new StringBuilder();
                where.append(DIRECTORY_PATH).append("=?");
                result = mGeneralDataBaseTemplate.update(TB_DIRECTORY_TREE, contentValues, where.toString(), new String[]{mediaFile.getMediaFilePath()});
                L.v(TAG, "updateVisibleState", "path=" + mediaFile.getMediaFilePath() + " result=" + result);
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

    private MediaFile getMediaFileByCursor(Cursor cursor) {
        MediaFile mediaFile = new MediaFile();
        int fileType = cursor.getInt(cursor
                .getColumnIndex(FILE_TYPE));
        String path = cursor.getString(cursor
                .getColumnIndex(DIRECTORY_PATH));
        switch (fileType) {
            case MediaFile.MEDIA_VIDEO_100TV_TYPE:
                MediaFile tempMediaFile = MediaFileConfigManager.getInstance().getMediaFileByFilePath(path);
                mediaFile.setMediaFileFragment(tempMediaFile.getMediaFileFragmentList());
                break;
        }
        File file = new File(path);
        if (file.exists()) {
            mediaFile.setMediaFileType(fileType);
            mediaFile.setMediaFileId(cursor.getLong(cursor
                    .getColumnIndex(DIRECTORY_ID)));
            mediaFile.setMediaFileName(cursor.getString(cursor
                    .getColumnIndex(DIRECTORY_NAME)));
            mediaFile.setMediaFileDirectoryType(cursor.getInt(cursor
                    .getColumnIndex(DIRECTORY_TYPE)));
            mediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_CUSTOM_TYPE);
            mediaFile.setMediaFileType(fileType);
            mediaFile.setMediaFilePath(path);
            mediaFile.setMediaFileOriginalPath(cursor.getString(cursor
                    .getColumnIndex(DIRECTORY_ORIGINAL_PATH)));
            mediaFile.setMediaFileAuthor(cursor.getString(cursor
                    .getColumnIndex(DIRECTORY_AUTHOR)));
            mediaFile.setMediaFileDateModified(cursor.getLong(cursor
                    .getColumnIndex(DIRECTORY_DATE_MODIFIED)));
            mediaFile.setMediaFileDuration(cursor.getLong(cursor
                    .getColumnIndex(DIRECTORY_DURATION)));
            mediaFile.setMediaFileSize(cursor.getLong(cursor
                    .getColumnIndex(FILE_SIZE)));
        }

        return mediaFile;
    }
}

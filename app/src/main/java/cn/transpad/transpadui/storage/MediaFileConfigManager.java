package cn.transpad.transpadui.storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.MediaFileFragment;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;

/**
 * 离线缓存配置文件管理
 *
 * @author wangyang
 * @since 2014-11-15
 */
public class MediaFileConfigManager {
    public static final String TAG = MediaFileConfigManager.class
            .getSimpleName();
    public static final String DOWNLOAD_FILE_NAME = "new_download_info";
    private static final MediaFileConfigManager sInstance = new MediaFileConfigManager();
    private static String sFilesDir = "";

    public static MediaFileConfigManager getInstance() {
        return sInstance;
    }

    public MediaFileConfigManager() {

    }

    /**
     * 根据json获取离线缓存信息
     *
     * @param filePath 路径
     * @return OfflineCache 视频信息
     */
    public MediaFile getMediaFileByFilePath(String filePath) {

        filePath += File.separator + DOWNLOAD_FILE_NAME;

        String json = getInputFileStringByFilePath(filePath);

        if (json == null || json.trim().equals("")) {
            return null;
        }
        MediaFile mediaFile = new MediaFile();
        try {

            L.v(TAG, "getOfflineCacheByJson", "json=" + json);

            JSONObject jsonObject = new JSONObject(json);

            // 视频必须信息,用户界面显示,加快读取速度
            int state = jsonObject
                    .optInt("download_state");
            if (state == 2) {
                JSONArray fragmentArray = jsonObject
                        .optJSONArray("fragment_list");
                if (fragmentArray != null) {

                    L.v(TAG, "getOfflineCacheByJson", "fragmentArray="
                            + fragmentArray.toString());

                    for (int i = 0; i < fragmentArray.length(); i++) {
                        MediaFileFragment offlineCacheFragment = new MediaFileFragment();
                        JSONObject unitObject = (JSONObject) fragmentArray
                                .get(i);
                        offlineCacheFragment.setMediaFileDuration(unitObject
                                .optLong("duration"));
                        offlineCacheFragment.setMediaFileID(unitObject
                                .optLong("cid"));
                        offlineCacheFragment
                                .setMediaFileFragmentStoragePath(unitObject
                                        .optString("fragment_store_path"));
                        mediaFile
                                .addMediaFileFragment(offlineCacheFragment);
                    }
                } else {
                    L.e(TAG, "getOfflineCacheByJson", "fragmentArray=null");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    /**
     * 卸载应用后,再次安装时,加载旧数据
     *
     * @return void
     */
    public ArrayList<MediaFile> get100TVViewFileList() {

        sFilesDir = SystemUtil.getInstance().get100TVPath();

        ArrayList<MediaFile> mediaFileList = new ArrayList<MediaFile>();
        ArrayList<MediaFileFragment> mediaFileFragmentList = new ArrayList<MediaFileFragment>();
        L.v(TAG, "initOfflineCacheDatabase", "sFilesDir=" + sFilesDir);
        getOfflineCacheListByFilePath(sFilesDir, mediaFileList);

        L.v(TAG, "initOfflineCacheDatabase", "mediaFileList.size()="
                + mediaFileList.size());
        for (MediaFile mediaFile : mediaFileList) {
            if (mediaFile != null) {
                L.v(TAG, "initCacheDB",
                        "offlineCache.getOfflineCacheFragmentList().size()="
                                + mediaFile.getMediaFileFragmentList()
                                .size());
                if (mediaFile.getMediaFileFragmentList().size() != 0) {
                    for (MediaFileFragment offlineCacheFragment : mediaFile
                            .getMediaFileFragmentList()) {
                        if (offlineCacheFragment != null) {
                            mediaFileFragmentList.add(offlineCacheFragment);
                        }
                    }
                }
            }
        }
        return mediaFileList;
    }

    /**
     * 初始化数据库时调用
     *
     * @param folderPath    json文件夹
     * @param mediaFileList 视频集合
     */
    private void getOfflineCacheListByFilePath(String folderPath,
                                               ArrayList<MediaFile> mediaFileList) {
        L.v(TAG, "getOfflineCacheListByFilePath", "start folderPath="
                + folderPath);

        File folder = new File(folderPath);
        if (!folder.exists()) {
            L.e(TAG, "getOfflineCacheListByFilePath", "no exists folderPath="
                    + folderPath);
            return;
        }

        String[] fileNameArray = folder.list();

        if (fileNameArray == null || fileNameArray.length == 0) {
            return;
        }

        for (String folderName : fileNameArray) {

            String specifiedPath = folderPath + File.separator + folderName;

            File specifiedFile = new File(specifiedPath);

            if (specifiedFile.isDirectory()) {

                getOfflineCacheListByFilePath(specifiedPath, mediaFileList);

            } else {

                if (folderName.equals(DOWNLOAD_FILE_NAME)) {

                    MediaFile mediaFile = getOfflineCacheByFilePath(specifiedPath);

                    if (mediaFile != null) {
                        mediaFileList.add(mediaFile);
                    }

                } else {

                    continue;

                }
            }

        }
    }

    /**
     * 根据缓存配置文件路径获取缓存对象
     *
     * @param filePath 缓存配置文件路径
     * @return MediaFile 缓存对象
     */
    public MediaFile getOfflineCacheByFilePath(String filePath) {
        MediaFile mediaFile = null;

        L.v(TAG, "getOfflineCacheByFilePath", "start filePath=" + filePath);

        String json = getInputFileStringByFilePath(filePath);

        if (json != null && !json.trim().equals("")) {

            mediaFile = getOfflineCacheByJson(json);

        } else {
            L.e(TAG, "getOfflineCacheByFilePath", "content is null filePath="
                    + filePath);
        }

        return mediaFile;
    }

    /**
     * 根据json获取离线缓存信息
     *
     * @param json 字符串
     * @return OfflineCache 视频信息
     */
    private MediaFile getOfflineCacheByJson(String json) {
        MediaFile mediaFile = new MediaFile();
        try {

            L.v(TAG, "getOfflineCacheByJson", "json=" + json);

            JSONObject jsonObject = new JSONObject(json);

            // 视频必须信息,用户界面显示,加快读取速度

            mediaFile.setMediaFileId(jsonObject.optLong("cid"));
            mediaFile.setMediaFileName(jsonObject.optString("name") + jsonObject.optString("episode_num"));
            int state = jsonObject
                    .optInt("download_state");
            if (state == 2) {
                mediaFile.setMediaFilePath(jsonObject
                        .optString("store_path"));
                mediaFile.setMediaFileType(MediaFile.MEDIA_VIDEO_100TV_TYPE);
                mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    /**
     * 根据文件路径返回文件内容
     *
     * @param filePath 文件路径
     * @return String 文件内容
     */
    public String getInputFileStringByFilePath(String filePath) {

        L.v(TAG, "getInputFileStringByFilePath", "start");

        StringBuffer sb = new StringBuffer();
        BufferedReader bufferedReader = null;

        File file = new File(filePath);
        if (!file.exists()) {
            L.e(TAG, "getInputFileStringByFilePath", "no exists filePath="
                    + filePath);
            return "";
        }

        try {

            bufferedReader = new BufferedReader(new FileReader(file));

            String line = bufferedReader.readLine();
            while (line != null) {
                sb.append(line);
                line = bufferedReader.readLine();
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private void getOutputFileByFilePath(String folderPath, String fileName,
                                         String json) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            File parentFolder = new File(folderPath);
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            String filePath = parentFolder.getAbsolutePath() + File.separator
                    + fileName;
            File newFile = new File(filePath);
            if (!newFile.exists()) {
                L.e(TAG, "getOutputFileByFilePath", "filePath=" + filePath
                        + " exists=" + new File(filePath).exists());
                newFile.createNewFile();
            }
            fileWriter = new FileWriter(newFile, false);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(json);

            bufferedWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }

    }
}

package cn.transpad.transpadui.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.transpad.transpadui.R;

/**
 * Created by user on 2015/1/22.
 */
public class ExtensionUtil {
    private static HashMap<String, String> sVideoFormatHashMap = new HashMap<String, String>();
    private static HashMap<String, String> sAudioFormatHashMap = new HashMap<String, String>();
    private static HashMap<String, String> sImageFormatHashMap = new HashMap<String, String>();
    private static HashMap<String, String> sFolderFormatHashMap = new HashMap<String, String>();
    private static List<String> sFolderList = new ArrayList<>();
    private static Context sContext = null;
    private static final ExtensionUtil sExtensionUtil = new ExtensionUtil();

    public static ExtensionUtil getInstance() {
        return sExtensionUtil;
    }

    public static void init(Context context) {
        sContext = context;
        initVideoFormat();
        initAudioFormat();
        initImageFormat();
        initFolderMap();
        initFolderList();
    }

    private static void initVideoFormat() {
        String[] videoFormatArray = sContext.getResources().getStringArray(R.array.video_format);
        sVideoFormatHashMap.clear();
        for (int i = 0; i < videoFormatArray.length; i++) {
            String videoExtension = videoFormatArray[i];
            sVideoFormatHashMap.put(videoExtension, videoExtension);
        }
    }

    private static void initAudioFormat() {
        String[] audioFormatArray = sContext.getResources().getStringArray(R.array.audio_format);
        sAudioFormatHashMap.clear();
        for (int i = 0; i < audioFormatArray.length; i++) {
            String audioExtension = audioFormatArray[i];
            sAudioFormatHashMap.put(audioExtension, audioExtension);
        }
    }

    private static void initImageFormat() {
        String[] imageFormatArray = sContext.getResources().getStringArray(R.array.image_format);
        sImageFormatHashMap.clear();
        for (int i = 0; i < imageFormatArray.length; i++) {
            String imageExtension = imageFormatArray[i];
            sImageFormatHashMap.put(imageExtension, imageExtension);
        }
    }

    private static void initFolderMap() {
        String[] folderFormatArray = sContext.getResources().getStringArray(R.array.illegal_folder);
        sFolderFormatHashMap.clear();
        for (int i = 0; i < folderFormatArray.length; i++) {
            String imageExtension = folderFormatArray[i];
            sFolderFormatHashMap.put(imageExtension, imageExtension);
        }
    }

    private static void initFolderList() {
        String[] folderFormatArray = sContext.getResources().getStringArray(R.array.illegal_folder);
        sFolderList.clear();
        for (int i = 0; i < folderFormatArray.length; i++) {
            String imageExtension = folderFormatArray[i];
            sFolderList.add(imageExtension);
        }
    }

    public boolean isVideoExtension(String extension) {
        return sVideoFormatHashMap.containsKey(extension);
    }

    public boolean isAudioExtension(String extension) {
        return sAudioFormatHashMap.containsKey(extension);
    }

    public boolean isImageExtension(String extension) {
        return sImageFormatHashMap.containsKey(extension);
    }

    public boolean isFolderExtension(String extension) {
        if (extension.startsWith(".")) {
            return true;
        }
        return sFolderFormatHashMap.containsKey(extension);
    }

    public boolean isFolderPath(String path) {
        for (String extension : sFolderList) {
            if (path.contains(extension)) {
                return true;
            }
        }
        return false;
    }
}

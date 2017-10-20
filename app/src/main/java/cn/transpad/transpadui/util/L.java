package cn.transpad.transpadui.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.SharedPreferenceModule;

public class L {
    // TODO: set it true when build for release version
    public final static boolean mode_for_release = false;
    public final static String TAG = "TransPadQ";
    private static String cmd;

    public static void v(String tag, String msg) {
        if (!mode_for_release) {
            Log.v(TAG, "[" + tag + "]" + msg);
        }
    }

    public static void v(String tag, String type, String msg) {
        if (!mode_for_release) {
            Log.v(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
            writeDownloadErrorLog("V", tag, type, msg, null);
        }
    }

    public static void v(String tag, String type, boolean msg) {
        if (!mode_for_release) {
            Log.v(TAG, "[" + tag + "]" + "[" + type + "]" + msg + "");
        }
    }

    public static void i(String tag, String type, String msg) {
        if (!mode_for_release) {
            Log.i(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
            writeDownloadErrorLog("I", tag, type, msg, null);
        }
    }

    public static void i(String tag, String type, int msg) {
        if (!mode_for_release) {
            Log.i(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (!mode_for_release) {
            Log.i(TAG, "[" + tag + "]" + msg);
        }
    }

    public static void i(String tag, String type, boolean msg) {
        if (!mode_for_release) {
            Log.i(TAG, "[" + tag + "]" + "[" + type + "]" + (msg ? "true"
                    : "false"));
        }
    }

    public static void e(String tag, String type, String msg) {
        if (!mode_for_release) {
            Log.e(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
            writeDownloadErrorLog("E", tag, type, msg, null);
        }
    }

    public static void e(String tag, String type, int msg) {
        if (!mode_for_release) {
            Log.e(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
        }
    }

    public static void e(String tag, String type, boolean msg) {
        if (!mode_for_release) {
            Log.e(TAG, "[" + tag + "]" + "[" + type + "]" + (msg ? "true"
                    : "false"));
        }
    }

    public static void e(String tag, String type, String msg, Exception e) {
        if (!mode_for_release) {
            Log.e(TAG, "[" + tag + "]" + "[" + type + "]" + msg, e);
            writeDownloadErrorLog("E", tag, type, msg, e);
        }
    }

    public static void w(String tag, String type, String msg) {
        if (!mode_for_release) {
            Log.w(TAG, "[" + tag + "]" + "[" + type + "]" + msg);
            writeDownloadErrorLog("W", tag, type, msg, null);
        }
    }

    /**
     * 写log到存储设备
     *
     * @return void
     */
    public static void writeDownloadTimeLog(OfflineCache offlineCache) {
        long time = SharedPreferenceModule.getInstance().getLong(offlineCache.getCacheID() + "");
        String timeStr = "";
        long second = time / 1000;
        if (second > 59) {
            long minute = second / 60;
            second = second - 60 * minute;
            timeStr = minute + ":" + second + "s";
        } else {
            timeStr = second + "s";
        }
        String content = "time=" + timeStr + " name=" + offlineCache.getCacheName() + " downloadState=" + offlineCache.getCacheDownloadState() + " errorCode=" + offlineCache.getCacheErrorCode() + " errorMessage=" + offlineCache.getCacheErrorMessage() + " url=" + offlineCache.getCacheDetailUrl() + "\r\n";
        File folder = new File(Environment.getExternalStorageDirectory()
                + File.separator + "TransPad" + File.separator + "Q");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + File.separator
                + "download_time_log.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        try {
            boolean isFirstLog = SharedPreferenceModule.getInstance().getBoolean("is_first_log", true);
            if (isFirstLog) {
                SharedPreferenceModule.getInstance().setBoolean("is_first_log", false);
                out = new FileOutputStream(file, false);
            } else {
                out = new FileOutputStream(file, true);
            }
            out.write(content.getBytes());
        } catch (FileNotFoundException e) {
            if (e != null) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
            }

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }

    /**
     * 写log到存储设备
     *
     * @return void
     */
    public static void writeDownloadErrorLog(String p, String tag, String type, String message, Throwable throwable) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        Date now = new Date(System.currentTimeMillis());
        String nowDate = sdf.format(now);
        String content = nowDate + " " + p + " [" + tag + "] [" + type + "] " + message + "\r\n";
        File folder = new File(Environment.getExternalStorageDirectory()
                + File.separator + "TransPad" + File.separator + "Q");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + File.separator
                + "download_log.txt");

        OutputStream out = null;
        try {

            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isFirstLog = SharedPreferenceModule.getInstance().getBoolean("is_first_log", true);
            if (isFirstLog) {
                SharedPreferenceModule.getInstance().setBoolean("is_first_log", false);
                out = new FileOutputStream(file, false);
            } else {
                out = new FileOutputStream(file, true);
            }
            out.write(content.getBytes());

            if (throwable != null) {
                final StackTraceElement[] stack = throwable.getStackTrace();
                final String errorMsg = "    " + throwable + "\r\n";
                out.write(errorMsg.getBytes());
                for (int i = 0; i < stack.length; i++) {
                    out.write(("           at " + stack[i].toString() + "\r\n").getBytes());
                }
            }
        } catch (FileNotFoundException e) {
            if (e != null) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
            }
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }

}

package cn.transpad.transpadui.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.letv.sdk.onehundredtv.video.BDVideoPartner;
import com.letv.sdk.onehundredtv.video.IVideo;
import com.letv.sdk.onehundredtv.video.LetvSdk;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.wav.WavTag;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.asf.AsfTag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.MultipleVideo;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.player.activity.AudioPlayActivity;
import cn.transpad.transpadui.player.activity.VideoPlayActivity;
import cn.transpad.transpadui.player.entity.AudioInfo;
import cn.transpad.transpadui.player.sohu.AppConst;
import cn.transpad.transpadui.player.sohu.SohuPlayerActivity;

/**
 * Created by Kongxiaojun on 2015/1/23.
 * 播放器工具类
 */
public class PlayerUtil {

    public static void readAudioHeader(Context context, AudioInfo info) {

        if (TextUtils.isEmpty(info.path)) {
            return;
        }

        if (info.path.startsWith("file://")) {
            info.path = info.path.substring(7);
        }
        Cursor cursor = null;
        try {

            String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.YEAR, MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST};

            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.DATA + " like '" + info.path + "%'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                //读取系统数据库中数据
                info.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                info.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                info.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                info.year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
                long songid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                info.image = getArtwork(context, songid, album_id);
            } else {
                try {
                    org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(new File(info.path));
                    AudioHeader header = audioFile.getAudioHeader();
                    info.bitRate = header.getBitRate();
                    info.encodingType = header.getEncodingType();
                    info.format = header.getFormat();
                    info.channels = header.getChannels();
                    info.sampleRate = header.getSampleRate();
                    if (info.mediaDuration == 0) {
                        info.mediaDuration = header.getTrackLength() * 1000;
                    }
                    Tag tag = audioFile.getTag();

                    //防止乱码
                    if (tag instanceof AsfTag) {
                        AsfTag asfTag = (AsfTag) tag;
                        info.artist = asfTag.getFirst(FieldKey.ARTIST);
                        info.album = asfTag.getFirst(FieldKey.ALBUM);
                        info.title = asfTag.getFirst(FieldKey.TITLE);
                    } else if (audioFile instanceof MP3File) {
                        MP3File mp3File = (MP3File) audioFile;
                        tag = mp3File.getTag();
                        info.artist = tag.getFirst(FieldKey.ARTIST);
                        info.album = tag.getFirst(FieldKey.ALBUM);
                        info.title = tag.getFirst(FieldKey.TITLE);
                    } else if (tag instanceof VorbisCommentTag) {
                        VorbisCommentTag vcTag = (VorbisCommentTag) tag;
                        info.artist = vcTag.getFirst(FieldKey.ARTIST);
                        info.album = vcTag.getFirst(FieldKey.ALBUM);
                        info.title = vcTag.getFirst(FieldKey.TITLE);
                    } else if (tag instanceof WavTag) {
                        WavTag wavTag = (WavTag) tag;
                        info.artist = wavTag.getFirst(FieldKey.ARTIST);
                        info.album = wavTag.getFirst(FieldKey.ALBUM);
                        info.title = wavTag.getFirst(FieldKey.TITLE);
                    } else if (tag instanceof FlacTag) {
                        FlacTag flacTag = (FlacTag) tag;
                        info.artist = flacTag.getFirst(FieldKey.ARTIST);
                        info.album = flacTag.getFirst(FieldKey.ALBUM);
                        info.title = flacTag.getFirst(FieldKey.TITLE);
                    }

                    if (tag != null) {
                        info.year = tag.getFirst(FieldKey.YEAR);
                        Artwork artwork = tag.getFirstArtwork();
                        if (artwork != null) {
                            byte[] ib = artwork.getBinaryData();
                            info.image = BitmapFactory.decodeByteArray(ib, 0, ib.length);
                        }
                    }
                } catch (Exception e) {
                }
            }
            if (!TextUtils.isEmpty(info.album)) {
                if (info.album.contains("unknown")) {
                    info.album = null;
                }
            }

            if (!TextUtils.isEmpty(info.artist)) {
                if (info.artist.contains("unknown")) {
                    info.artist = null;
                }
            }

            if (!TextUtils.isEmpty(info.title)) {
                if (info.title.contains("unknown")) {
                    info.title = null;
                }
            }

            if (!TextUtils.isEmpty(info.year)) {
                if (info.year.contains("unknown")) {
                    info.year = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public static Bitmap getArtwork(Context context, long song_id, long album_id) {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, new BitmapFactory.Options());
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                    }
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte[] art = null;
        String path = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        return bm;
    }

    /**
     * 把秒钟转换成分钟"hh:mm:ss"显示
     *
     * @return void
     * @throws
     */
    public static String second2HourStr(int second) {
        int hour = second / 3600;
        int min = (second / 60)%60;
        int sec = second - hour*3600 - min*60;
        StringBuffer sbf = new StringBuffer();

        if (hour < 10) {
            sbf.append(0);
            sbf.append(hour);
        } else {
            sbf.append(hour);
        }
        sbf.append(":");
        if (min < 10) {
            sbf.append("0");
            sbf.append(min);
        } else {
            sbf.append(min);
        }
        sbf.append(":");
        if (sec < 10) {
            sbf.append(0);
            sbf.append(sec);
        } else {
            sbf.append(sec);
        }
        return sbf.toString();
    }

    /**
     * 把秒钟转换成分钟"mm:ss"显示
     *
     * @return void
     * @throws
     */
    public static String second2MinuteStr(int second) {
        int min = second / 60;
        int sec = second % 60;
        StringBuffer sbf = new StringBuffer();
        if (min < 10) {
            sbf.append("0");
            sbf.append(min);
        } else {
            sbf.append(min);
        }
        sbf.append(":");
        if (sec < 10) {
            sbf.append(0);
            sbf.append(sec);
        } else {
            sbf.append(sec);
        }
        return sbf.toString();
    }

    /**
     * 把图片变成圆角
     *
     * @param bitmap 需要修改的图片
     * @param angle  圆角的弧度
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int angle) {
        if (bitmap == null) {
            return null;
        }
        int sideSize = bitmap.getWidth() < bitmap.getHeight() ? bitmap
                .getWidth() : bitmap.getHeight();
        Bitmap output = null;
        try {
            output = Bitmap.createBitmap(sideSize, sideSize, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Canvas canvas;
        try {
            canvas = new Canvas(output);
        } catch (NullPointerException e) {
            return null;
        }

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sideSize, sideSize);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, angle, angle, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 把图片变成圆形图片
     *
     * @param bitmap      原始图片
     * @param width       生成图片宽度
     * @param border      边框宽度
     * @param borderColor 边框颜色
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int width, int border, int borderColor) {
        if (bitmap == null) {
            return null;
        }
        if (border < 0) {
            border = 0;
        }
        Bitmap output = null;
        try {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        Canvas canvas;
        try {
            canvas = new Canvas(output);
        } catch (NullPointerException e) {
            return null;
        }

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(border, border, width - border, width - border);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, width / 2, width / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        int squareWidth = 0, squareHeight = 0;
        int x = 0, y = 0;
        Bitmap squareBitmap;
        Matrix matrix = new Matrix();
        if (bmpHeight > bmpWidth) {// 高大于宽
            squareWidth = squareHeight = bmpWidth;
            x = 0;
            y = (bmpHeight - bmpWidth) / 2;
        } else if (bmpHeight < bmpWidth) {// 宽大于高
            squareWidth = squareHeight = bmpHeight;
            x = (bmpWidth - bmpHeight) / 2;
            y = 0;
        } else {
            squareWidth = squareHeight = bmpHeight;
        }
        matrix.setScale((float) width / (float) squareWidth, (float) width / (float) squareWidth);
        squareBitmap = Bitmap.createBitmap(bitmap, x, y, squareWidth,
                squareHeight, matrix, true);

        canvas.drawBitmap(squareBitmap, rect, rect, paint);
        if (border > 0) {
            final Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(borderColor);
            p.setFilterBitmap(true);
            p.setDither(true);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(border);
            canvas.drawCircle(width / 2, width / 2, width / 2 - border, p);
        }
        return output;
    }

    /**
     * 获取屏幕密度
     *
     * @return float
     * @throws
     */
    public static float getDensity() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = TransPadApplication.getTransPadApplication().getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * dp转px
     *
     * @param dpValue dp
     * @return int px
     * @throws
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    /**
     * px 转 dp
     *
     * @param pxValue px
     * @return int dp
     * @throws
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / getDensity() + 0.5f);
    }

    /**
     * 格式化文件大小显示方式
     *
     * @param fileSize
     * @return
     */
    public static String formatFileLength(long fileSize) {
        String showSize = "";
        if (fileSize >= 0 && fileSize < 1024) {
            showSize = fileSize + "B";
        } else if (fileSize >= 1024 && fileSize < (1024 * 1024)) {
            showSize = Long.toString(fileSize / 1024) + "KB";
        } else if (fileSize >= (1024 * 1024) && fileSize < (1024 * 1024 * 1024)) {
            showSize = formatNumber((double) fileSize / (1024 * 1024), "#.##") + "MB";
        } else if (fileSize >= (1024 * 1024 * 1024)) {
            showSize = formatNumber((double) fileSize / (1024 * 1024 * 1024), "#.##") + "GB";
        }
        return showSize;
    }

    public static String formatNumber(double d, String pattern) {
        String s = "";
        try {
            if (pattern == null)
                pattern = "0.00";
            DecimalFormat df = new DecimalFormat(pattern);
            s = df.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void openVideoPlayer(Context context, ArrayList<MediaFile> mediaFiles, int playindex) {
        ArrayList<MultipleVideo> multipleVideos = mediafileList2MultipleVideoList(mediaFiles);
        if (multipleVideos != null && multipleVideos.size() > 0) {
            openMultipleVideoPlayer(context, multipleVideos, playindex);
        }
    }

    public static void openVideoPlayer(Context context, MediaFile mediaFile) {
        MultipleVideo multipleVideo = mediaFile2MultipleVideo(mediaFile);
        if (multipleVideo != null) {
            ArrayList<MultipleVideo> multipleVideos = new ArrayList<>();
            multipleVideos.add(multipleVideo);
            openMultipleVideoPlayer(context, multipleVideos, 0);
        }
    }

    public static void openVideoPlayer(Context context, String xyzplay) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra("xyzplay", xyzplay);
        context.startActivity(intent);
    }

    /**
     * 打开搜狐视频SDK播放
     *
     * @param context
     * @param id
     * @param keyword
     * @param ourl
     * @param isFromNotify 是否来自通知
     * @param xyzplay
     * @return void
     * @throws
     */
    public static void openSohuPlayer(Context context, String id,
                                      String keyword, String ourl, boolean isFromNotify, String xyzplay) {
        try {
            ourl = URLEncoder.encode(TPUtil.Base64Encode(ourl), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.putExtra(AppConst.INTENT_KEY_ID, id);
        intent.putExtra(AppConst.INTENT_KEY_KEYWORD, keyword);
        intent.putExtra(AppConst.INTENT_KEY_OURL, ourl);
        intent.putExtra(AppConst.INTENT_FROM_NOTIFY, isFromNotify);
        intent.putExtra(AppConst.INTENT_XYZPLAY_URL, xyzplay);

        intent.setClass(context, SohuPlayerActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开乐视SDK播放器
     *
     * @param context
     * @param ourl
     * @return boolean
     * @throws
     */
    public static boolean openLetvSdkPlayer(final Context context, String ourl) {
        long vid = getLetvVidByUrl(ourl);
        if (vid <= 0) {
            return false;
        }
        LetvSdk.getInstance().setConetxt(context);// 设置context
        LetvSdk.getInstance().registerCallBack(
                new com.letv.sdk.onehundredtv.video.BDVideoPartner.Callback() {
                    @Override
                    public void onEvent(final int event, final String name,
                                        IVideo video) {
                        // 播放器回调
                        if (event == BDVideoPartner.EVENT_PLAY_START) {
                        } else if (event == BDVideoPartner.EVENT_DLNA_PLAY) {
                        } else if (event == BDVideoPartner.EVENT_PLAY_PAUSE) {
                        } else if (event == BDVideoPartner.EVENT_PLAY_STOP) {// 播放器退出
                        } else if (event == BDVideoPartner.EVENT_PLAY_RESUME) {
                        } else if (event == BDVideoPartner.EVENT_PLAY_NEXT) {
                        } else if (event == BDVideoPartner.EVENT_PLAY_PREV) {
                        } else if (event == BDVideoPartner.EVENT_FAVORITE) {// 收藏
                        } else if (event == BDVideoPartner.EVENT_FAVORITE_CANCEL) {// 取消收藏
                        } else if (event == BDVideoPartner.EVENT_START_DOWNLOAD) {// 下载
                            // 下载地址获取地方
                            LetvSdk.getInstance().changeDownState(1);// 0是下载
                            // 1是已下载
                        } else if (event == BDVideoPartner.EVENT_PLAY_PREV) {

                        }
                    }
                });// 注册回调

        IVideo video1 = new IVideo();
        video1.vID = vid;// 只有vid
        video1.mCurrentTime = 0;// 设置播放进度 单位s
        video1.mIsFavorite = true;
        video1.isShowDownload = false;
        LetvSdk.getInstance().play(context, video1);

        return true;
    }

    /**
     * 打开视频播放多片视频
     *
     * @param context
     * @param multipleVideos
     * @param playindex
     */
    public static void openMultipleVideoPlayer(Context context, ArrayList<MultipleVideo> multipleVideos, int playindex) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putParcelableArrayListExtra("playlist", multipleVideos);
        intent.putExtra("playindex", playindex);
        context.startActivity(intent);
    }

    /**
     * MediaFile List 转 MultipleVideo List
     *
     * @param mediaFiles
     * @return
     */
    public static ArrayList<MultipleVideo> mediafileList2MultipleVideoList(ArrayList<MediaFile> mediaFiles) {
        if (mediaFiles != null && mediaFiles.size() > 0) {
            ArrayList<MultipleVideo> multipleVideos = new ArrayList<MultipleVideo>();
            for (MediaFile mediaFile : mediaFiles) {
                MultipleVideo multipleVideo = mediaFile2MultipleVideo(mediaFile);
                if (multipleVideo != null) {
                    multipleVideos.add(multipleVideo);
                }
            }
            return multipleVideos;
        }
        return null;
    }

    /**
     * mediaFile转MultipleVideo
     *
     * @param mediaFile
     * @return
     */
    public static MultipleVideo mediaFile2MultipleVideo(MediaFile mediaFile) {
        if (mediaFile != null && !TextUtils.isEmpty(mediaFile.getMediaFilePath())) {
            File file = new File(mediaFile.getMediaFilePath());
            if (file.exists()) {
                MultipleVideo multipleVideo = new MultipleVideo();
                multipleVideo.setUrls(new String[]{mediaFile.getMediaFilePath()});
                multipleVideo.setDurations(new int[]{0});
                multipleVideo.setName(file.getName());
                return multipleVideo;
            }
        }
        return null;
    }

    public static void openAudioPLayer(Context context, ArrayList<MediaFile> mediaFiles, int playindex) {
        Intent intent = new Intent(context, AudioPlayActivity.class);
        intent.putParcelableArrayListExtra("playlist", mediaFiles);
        intent.putExtra("playindex", playindex);
        context.startActivity(intent);
    }

    public static void openAudioPLayer(Context context, MediaFile mediaFile) {
        ArrayList<MediaFile> mediaFiles = new ArrayList<>();
        mediaFiles.add(mediaFile);
        Intent intent = new Intent(context, AudioPlayActivity.class);
        intent.putParcelableArrayListExtra("playlist", mediaFiles);
        intent.putExtra("playindex", 0);
        context.startActivity(intent);
    }

    /**
     * 根据URI返回文件路径
     *
     * @param uri 文件URI
     * @return String 文件路径
     */
    public static String getFilePathByMediaUri(String uri) {
        String path = null;
        Cursor cursor = TransPadApplication.getTransPadApplication().getContentResolver().query(Uri.parse(uri),
                null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            }
        } finally {
            cursor.close();
        }
        return path;
    }

    /**
     * 根据乐视url获取乐视视频vid
     *
     * @param ourl
     * @return long
     * @throws
     */
    public static long getLetvVidByUrl(String ourl) {
        if (ourl.contains("vplay_")) {
            // m.letv.com解析方式
            long videoId = 0;
            try {
                String vid = ourl.substring(ourl.indexOf("vplay_") + 6,
                        ourl.indexOf(".html"));
                videoId = Long.parseLong(vid);
            } catch (Exception e) {
                videoId = 0;
            }
            return videoId;
        } else if (ourl.contains("vplay/")) {
            // www.letv.com解析方式
            long videoId = 0;
            try {
                String vid = ourl.substring(ourl.indexOf("vplay/") + 6,
                        ourl.indexOf(".html"));
                videoId = Long.parseLong(vid);
            } catch (Exception e) {
                videoId = 0;
            }
            return videoId;
        } else {
            return 0;
        }
    }
}

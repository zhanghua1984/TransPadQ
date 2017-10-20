package com.fone.player;

/**
 * Created by Kongxiaojun on 2015/1/16.
 * 播放状态监听
 */
public interface PlayStateListenter {
    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param fp   the FonePlayer that reached the end of the file
     * @param type 1:videoplater completion 0:one frag play completion
     */
    void onCompletion(FonePlayer fp, int type);

    /**
     * Called to update status in buffering a media stream received through
     * progressive HTTP download. The received buffering percentage
     * indicates how much of the content has been buffered or played. For
     * example a buffering update of 80 percent when half the content has
     * already been played indicates that the next 30 percent of the content
     * to play has been buffered.
     *
     * @param fp      the FonePlayer the update pertains to
     * @param percent the percentage (0-100) of the content that has been
     *                buffered or played thus far
     */
    void onBufferingUpdate(FonePlayer fp, int percent);

    /**
     * Called when the media file is ready for playback.
     *
     * @param mediaInfoObj
     * @param fp  the FonePlayer that is ready for playback
     */
    void onPrepared(FonePlayer fp, TagFoneMediaDesc mediaInfoObj);

    /**
     * Called when the media file has open percent
     *
     * @param fp
     * @param percent media file open percent
     */
    void onOpenPercent(FonePlayer fp, int percent);

    /**
     * Called when the media file has new subtitle
     *
     * @param subtitle the content of subtitle
     */
    void onNewSubtitle(String subtitle);

    /**
     * Called when the media file open done
     *
     * @param fp
     */
    void onOpenDone(FonePlayer fp);

    /**
     * Called when the media file open failed
     *
     * @param fp
     * @param type open failed type 0:省电加速打开失败 1:硬解打开失败 2:软解打开失败
     */
    void onOpenFailed(FonePlayer fp, int type);

    /**
     * Called when the media file open Success
     *
     * @param fp
     */
    void onOpenSuccess(FonePlayer fp);

    /**
     * Called open file engineType
     *
     * @param fp
     * @param engineType 0:省电加速 1:硬解(系统播放器) 2:软解
     */
    void onEngineType(FonePlayer fp, int engineType);

    /**
     * Called close player success
     *
     * @param fp
     */
    void onCloseSuccess(FonePlayer fp);

    /***
     * Called when progress change
     * @param fp
     * @param progress
     */
    void onProgressChanged(FonePlayer fp, int progress);

    /***
     * Called when progress change
     * @param fp
     * @param progress
     */
    void onCacheProgressChanged(FonePlayer fp, int progress);

    /**
     * Interface definition for a callback to be invoked when an message is
     * availabl.
     *
     * @param fp
     * @param msg
     * @param arg1
     * @param arg2
     * @return
     */
    void onPlayerMessage(FonePlayer fp, int msg, int arg1, int arg2);
}

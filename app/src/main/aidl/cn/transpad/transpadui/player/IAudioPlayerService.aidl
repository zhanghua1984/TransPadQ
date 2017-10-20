// IAudioPlayerService.aidl
package cn.transpad.transpadui.player;

import cn.transpad.transpadui.player.IPlayStateCallback;
import cn.transpad.transpadui.entity.MediaFile;

// Declare any non-default types here with import statements

interface IAudioPlayerService {

    boolean open(in MediaFile file);

    boolean play();

    boolean pause();

    void stop();

    boolean seetTo(int pos);

    String getCurrentPath();

    int getCurrentPostion();

    int getDuration();

    boolean isPlaying();

    void registerCallback(IPlayStateCallback cb);

    void unRegisterCallback(IPlayStateCallback cb);

}

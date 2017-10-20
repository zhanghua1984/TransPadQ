// IPlayProgressCallback.aidl
package cn.transpad.transpadui.player;

// Declare any non-default types here with import statements

interface IPlayStateCallback {

    void onPostionChanged(int postion);

    void onOpenFailed();

    void onPrepared(int duration);

    void onCompletion();

}

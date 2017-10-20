// IDeviceChangedCallback.aidl
package cn.transpad.dlna;
// Declare any non-default types here with import statements

interface IDeviceChangedCallback {
    void onDeviceChanged(String server_uid,boolean add);
}

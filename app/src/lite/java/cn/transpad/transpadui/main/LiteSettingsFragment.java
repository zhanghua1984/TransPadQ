package cn.transpad.transpadui.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.enrique.stackblur.StackBlurManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.SystemUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.util.UpdateUtil;
import cn.transpad.transpadui.view.LiteAboutDialog;
import cn.transpad.transpadui.view.SuggestUpdateDialog;
import cn.transphone.utp.lib.TranspadDevice;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kongxiaojun on 2015/4/7.
 */
public class LiteSettingsFragment extends BaseFragment {
    private static final String TAG = LiteSettingsFragment.class.getSimpleName();
    private static final int PICKED_PICTURE = 123;
    private static final int CROPED_PICTURE = 124;

    private static final String SUGGEST_UPDATE = "0"; // 推荐升级
    private static final String FORCE_UPDATE = "1"; // 强制升级
    private static final String ALREADY_NEWLEST = "2"; // 已是最新版本
    private static final String FIRST_LAUNCH_SETTING = "first_launch_setting"; // 第一次启动写入背景图

    private boolean landscape;
    private boolean notifunction;

    private SuggestUpdateDialog updateDialog;
    private LiteAboutDialog aboutDialog;
    private Link link;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        landscape = SharedPreferenceModule.getInstance().getBoolean(TransPadService.LANDSCAPE_MODE_NAME, true);
        updateDialog = new SuggestUpdateDialog(getActivity(), R.style.myDialog);
        aboutDialog = new LiteAboutDialog(getActivity(), R.style.myDialog);

        boolean isFirstLaunch = SharedPreferenceModule.getInstance().getBoolean(FIRST_LAUNCH_SETTING, true);
        L.v(TAG, "onCreate", isFirstLaunch);
        if (isFirstLaunch) {
            saveDefaultBackground(context);
        }
    }

    private void saveDefaultBackground(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_main_background);
//                BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.default_main_background);
//                Bitmap bitmap = drawable.getBitmap();

                File file = new File(SystemUtil.getInstance().getRootPath(), "bg.png");
                L.v(TAG, "saveDefaultBackground", file.getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    SharedPreferenceModule.getInstance().setBoolean(FIRST_LAUNCH_SETTING, false);
//                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
//                            + SystemUtil.getInstance().getRootPath())));
                    MediaScannerConnection.scanFile(context,
                            new String[]{SystemUtil.getInstance().getRootPath()
                                    + File.separator + "bg.png"}, null, null);
                } catch (IOException e) {
//                    e.printStackTrace();
                    L.e(TAG, "saveDefaultBackground", e.toString());
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout, container, false);
        ButterKnife.inject(this, view);
        new Screen(screenView);
        new Picture(pictureView);
        new About(aboutView);
        new Update(updateView);
        new Feedback(feedbackView);
        new Help(helpView);
        link = new Link(linkDevice);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onResume() {
        super.onResume();
        notifunction = TPUtil.getNotifuactionAccessibilityState();
        L.v(TAG, "onResume", notifunction);
        String freq = "";
        if (TransPadService.getDevicesFreq() == TranspadDevice.FREQ_24GHZ) {
            freq = getString(R.string.connect_freq_24);
        } else if (TransPadService.getDevicesFreq() == TranspadDevice.FREQ_5GHZ) {
            freq = getString(R.string.connect_freq_5);
        }
        String deviceName = TransPadService.getConnectDeviceName();
        L.v(TAG, "onResume", "deviceName=" + TransPadService.getConnectDeviceName());
//        link.linkText.setText(deviceName == null ? getString(R.string.settings_link_unlink) : freq + " " + deviceName);
        L.v(TAG, "onResume", "isConnected = " + TransPadService.isConnected());
        if (TransPadService.isConnected()) {
            link.linkText.setText(R.string.settings_link_link);
        } else {
            link.linkText.setText(R.string.settings_link_unlink);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (aboutDialog != null && aboutDialog.isShowing()) {
            aboutDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
    }

    @InjectView(R.id.linkDevice)
    RelativeLayout linkDevice;
    @InjectView(R.id.screen)
    RelativeLayout screenView;
    @InjectView(R.id.picture)
    RelativeLayout pictureView;
    @InjectView(R.id.about)
    RelativeLayout aboutView;
    @InjectView(R.id.update)
    RelativeLayout updateView;
    @InjectView(R.id.feedback)
    RelativeLayout feedbackView;
    @InjectView(R.id.help)
    RelativeLayout helpView;

    @OnClick(R.id.app_back)
    void back() {
        onBack();
    }

    @OnClick(R.id.picture)
    void picture() {
        //            打开壁纸
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICKED_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        L.v(TAG, "onActivityResult", "reqcod=" + requestCode + "recod=" + resultCode + "data=" + data);
        switch (requestCode) {
            case PICKED_PICTURE:
                if (data != null) {
                    Uri uri = data.getData();
                    File fileSrc = new File(getAbsoluteImagePath(uri));
                    File defaultBackground = new File(SystemUtil.getInstance().getRootPath(), "bg.png");
                    L.v(TAG, "onActivityResult", fileSrc.equals(defaultBackground));
//                    if (fileSrc.getPath().equals(defaultBackground.getPath()) && fileSrc.length() == defaultBackground.length()) {
                    if (fileSrc.equals(defaultBackground)) {
                        File fileTarget = new File(getActivity().getCacheDir(), HomeActivity.WALLPAGER_FILENAME);
                        try {
                            TPUtil.copyFile(fileSrc, fileTarget);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        msg.what = HomeActivity.MSG_WHAT_BACKAGE_GROUND_CHANGED;
                        EventBus.getDefault().post(msg);
                        break;
                    }
                    if (fileSrc.getName().toUpperCase().endsWith("PNG") || fileSrc.getName().toUpperCase().endsWith("JPG") || fileSrc.getName().toUpperCase().endsWith("JPEG")) {
                        cropPicture(uri);
                    } else {
                        //TODO 提示不支持格式
                        Toast.makeText(getActivity(), R.string.settings_picture_unsupport, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case CROPED_PICTURE:
                final File file = new File(getActivity().getExternalCacheDir(), "temp.png");
                if (file.exists()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            L.v(TAG, "start blur image time = " + System.currentTimeMillis());
//                                Bitmap bitmap = TPUtil.compressImageFromFile(filesrc.getAbsolutePath(), 800, 480);
                            StackBlurManager stackBlurManager = new StackBlurManager(BitmapFactory.decodeFile(file.getAbsolutePath()));
                            stackBlurManager.process(10);
                            stackBlurManager.saveIntoFile(new File(getActivity().getCacheDir(), HomeActivity.WALLPAGER_FILENAME).getAbsolutePath());
                            L.v(TAG, "end blur image time = " + System.currentTimeMillis());
                            Message msg = new Message();
                            msg.what = HomeActivity.MSG_WHAT_BACKAGE_GROUND_CHANGED;
                            EventBus.getDefault().post(msg);
                        }
                    }).start();
                }
                break;
        }
    }

    public void cropPicture(Uri uri) {
        File temp = new File(getActivity().getExternalCacheDir(), "temp.png");
        if (temp.exists()) {
            temp.delete();
        }
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 5);// 裁剪框比例
        intent.putExtra("aspectY", 3);
        intent.putExtra("outputX", 800);// 输出图片大小
        intent.putExtra("outputY", 480);
        intent.putExtra("output", Uri.fromFile(temp));
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);//去黑边
        intent.putExtra("scaleUpIfNeeded", true);//去黑边
        startActivityForResult(intent, CROPED_PICTURE);
    }

    protected String getAbsoluteImagePath(Uri uri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getActivity().managedQuery(uri,
//                proj,                 // Which columns to return
//                null,       // WHERE clause; which rows to return (all rows)
//                null,       // WHERE clause selection arguments (none)
//                null);                 // Order-by clause (ascending by name)
//
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String path=cursor.getString(column_index);
//        cursor.close();

        Cursor cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @OnClick(R.id.about)
    void about() {
        //            关于页面
        aboutDialog.show();

//        AllAppView allAppView = new AllAppView(context);
    }

    @OnClick(R.id.help)
    void help() {
        //关于帮助
        HomeActivity.switchFragment(new HelpFragement());
    }

    @OnClick(R.id.update)
    void update() {
        Request.getInstance().login(1, new Callback<LoginRst>() {
            @Override
            public void success(final LoginRst loginRst, Response response) {
                L.v(TAG, "success", "loginRst=" + loginRst);
                if (loginRst != null && loginRst.softupdate != null) {
                    L.v(TAG, "success", "updateflag" + loginRst.softupdate.updateflag);
                    switch (loginRst.softupdate.updateflag) {
                        case SUGGEST_UPDATE:
//                            checkSuggestUpdate(loginRst);
                            UpdateUtil.checkSuggestUpdate(loginRst);
                            suggestUpdate(loginRst);
                            break;
                        case FORCE_UPDATE:
                        case ALREADY_NEWLEST:
                            Toast.makeText(context, R.string.version_dialog_new, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Toast.makeText(context, R.string.version_dialog_fail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                L.v(TAG, "failure", "error=" + error);
                if (getActivity() != null) {
                    if (TPUtil.isNetOkWithToast()) {
                        Toast.makeText(context, R.string.version_dialog_fail, Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity(), R.string.settings_version_fail_noNetwork, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void suggestUpdate(final LoginRst loginRst) {
        Reporter.logEvent(Reporter.EventId.UPGRADE_POPUP);
        updateDialog.setUpdateMessage(loginRst);
        updateDialog.setClickListener(new SuggestUpdateDialog.ClickListener() {
            @Override
            public void onOk() {
                Reporter.logEvent(Reporter.EventId.UPGRADE_CLICK_OK);
                UpdateUtil.startDownloadUpdateFile(loginRst);
            }

            @Override
            public void onCancel() {
                Reporter.logEvent(Reporter.EventId.UPGRADE_CLICK_CANCEL);
                UpdateUtil.deleteUpdateFile(loginRst);
                updateDialog.dismiss();
            }
        });
        Window dialogWindow = updateDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        dialogWindow.setGravity(Gravity.CENTER);
//        lp.width= ScreenUtil.dp2px(227);
//        lp.height=ScreenUtil.dp2px(123);
        lp.dimAmount = 0.5f;
        lp.y = ScreenUtil.dp2px(-20);
//
//        dialogWindow.setAttributes(lp);
//        WindowManager m = getActivity().getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.4); // 高度设置为屏幕的0.6
//        p.width = (int) (d.getWidth() * 0.4); // 宽度设置为屏幕的0.65
//        dialogWindow.setAttributes(p);
        updateDialog.show();
    }


    @OnClick(R.id.feedback)
    void feedback() {
        FeedBackFragmentLite fragment = new FeedBackFragmentLite();
        HomeActivity.switchFragment(fragment);
    }

    class Link implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView linkImage;
        @InjectView(R.id.settingsName)
        TextView linkText;
        @InjectView(R.id.settingsVersion)
        TextView linkVersion;

        Link(View view) {
            ButterKnife.inject(this, view);
            linkImage.setImageResource(R.drawable.set_link);
            linkText.setText(R.string.settings_link);
            linkVersion.setVisibility(View.GONE);
        }
    }

    class Light implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView lightImage;
        @InjectView(R.id.settingsName)
        TextView lightText;

        Light(View view) {
            ButterKnife.inject(this, view);
            lightImage.setImageResource(R.drawable.set_light);
            lightText.setText(R.string.settings_light);
        }
    }

    class Picture implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView pictureImage;
        @InjectView(R.id.settingsName)
        TextView pictureText;

        Picture(View view) {
            ButterKnife.inject(this, view);
            pictureImage.setImageResource(R.drawable.set_picture);
            pictureText.setText(R.string.settings_backgroud);
        }
    }

    class Feedback implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView feedbackImage;
        @InjectView(R.id.settingsName)
        TextView feedbackText;

        Feedback(View view) {
            ButterKnife.inject(this, view);
            feedbackImage.setImageResource(R.drawable.set_feedback_information);
            feedbackText.setText(R.string.settings_feedback);
        }
    }

    class Quiet implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView quietImage;
        @InjectView(R.id.settingsName)
        TextView quietText;
        @InjectView(R.id.toggleButton)
        ToggleButton quietButton;

        @OnCheckedChanged(R.id.toggleButton)
        void checked(boolean checked) {
            //            通知助手
            if (!quietButton.isPressed()) return;
            L.v(TAG, "checked", "checked" + checked);
            TPUtil.openAccessibility(getActivity());
        }

        Quiet(View view) {
            ButterKnife.inject(this, view);
            quietImage.setImageResource(R.drawable.set_quiet);
            quietText.setText(R.string.settings_assistant);
        }
    }

    class Screen implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView aboutImage;
        @InjectView(R.id.settingsName)
        TextView aboutText;
        @InjectView(R.id.toggleButton)
        ToggleButton screenButton;

        @OnCheckedChanged(R.id.toggleButton)
        void checked(boolean checked) {
            SharedPreferenceModule.getInstance().setBoolean("is_land_screen", checked);
            TransPadService.setLandScreen(checked);
        }

        Screen(View view) {
            ButterKnife.inject(this, view);
            aboutImage.setImageResource(R.drawable.set_sreen);
            aboutText.setText(R.string.settings_landscape);
            boolean isLandScreen = SharedPreferenceModule.getInstance().getBoolean("is_land_screen", false);
            screenButton.setChecked(isLandScreen);
        }
    }

    class About implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView aboutImage;
        @InjectView(R.id.settingsName)
        TextView aboutText;

        About(View view) {
            ButterKnife.inject(this, view);
            aboutImage.setImageResource(R.drawable.set_about);
            aboutText.setText(R.string.settings_about);
        }
    }

    class Update implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView updateImage;
        @InjectView(R.id.settingsName)
        TextView updateText;

        Update(View view) {
            ButterKnife.inject(this, view);
            updateImage.setImageResource(R.drawable.set_update);
            updateText.setText(R.string.settings_version);
        }
    }

    class Help implements Serializable {
        @InjectView(R.id.settingsImage)
        ImageView helpImage;
        @InjectView(R.id.settingsName)
        TextView helpText;

        Help(View view) {
            ButterKnife.inject(this, view);
            helpImage.setImageResource(R.drawable.set_help);
            helpText.setText(R.string.settings_help);
        }
    }

}

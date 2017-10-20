package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import de.greenrobot.event.EventBus;

/**
 * 自定义对话框 需要传入dialog样式文件,dialog布局文件
 * <p/>
 * ps:在调用时注意mcontext报错,参数传入调用该对话框的activity
 *
 * @author wangyang
 */
public class CustomDialog extends Dialog {
    private static final String TAG = "CustomDialog";
    // public int layoutRes;// 布局文件
    public Context mContext;
    private View mView;
    private TextView mDescTextView;

    public CustomDialog(Context context) {
        this(context, null);
        EventBus.getDefault().register(this);
        mContext = context;
    }

    @Override
    protected void onStart() {
        super.onStart();

        View view = getLayoutInflater().inflate(
                R.layout.scan_media_dialog_view, new CustomLayout(mContext));
        mDescTextView = (TextView) view.findViewById(R.id.tv_dialog_desc);
        setContentView(view,
                new ViewGroup.LayoutParams(
                        (int) (ScreenUtil.getScreenWidthPix(mContext) * 0.8),
                        (int) (ScreenUtil.getScreenWidthPix(mContext) * 0.3)));
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                // 取消扫描
                StorageModule.getInstance().cancelScanning();
            }
        });
    }

    /**
     * 自定义布局的构造方法
     *
     * @param context
     */
    public CustomDialog(Context context, View mView) {
        super(context, R.style.comm_alertdialog);
        this.mView = mView;
    }

    /**
     * 自定义主题及布局的构造方法
     *
     * @param context
     * @param theme
     * @param resLayout
     */
    public CustomDialog(Context context, int theme, int resLayout) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mView != null)
            this.setContentView(mView);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case StorageModule.MSG_ACTION_SCANNER_PROCESSING:
                String media = (String) msg.obj;
                L.v(TAG, media);
                mDescTextView.setText(media);
                break;
        }
    }

    public static class CustomLayout extends RelativeLayout {

        public CustomLayout(Context context) {
            super(context);
        }

    }
}

package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.AppDetail;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import de.greenrobot.event.EventBus;

/**
 * Created by user on 2015/4/27.
 */
public class DownloadDetailDialog extends Dialog {

    private static final String TAG = DownloadDetailDialog.class.getSimpleName();
    private Context context;
    private OnDialogClickListener onDialogClickListener;
    private boolean check;
    private AppDetail appDetail;
    private static final int MAX_LINE_LIMIT = 3;

    public DownloadDetailDialog(Context context) {
        super(context);
    }

    public DownloadDetailDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @InjectView(R.id.detail_icon)
    RoundedImageView detailAppIcon;
    @InjectView(R.id.detail_name)
    TextView detailAppName;
    @InjectView(R.id.detail_type)
    TextView detailType;
    @InjectView(R.id.detail_size)
    TextView detailSize;
    @InjectView(R.id.detail_version)
    TextView detailVersion;
    @InjectView(R.id.detail_stars)
    LinearLayout detailStarsLayout;
    @InjectView(R.id.detail_amount)
    TextView detailAmount;
    @InjectView(R.id.detail_description)
    TextView detailDescription;
    @InjectView(R.id.detail_images)
    LinearLayout detailImagesLayout;
    @InjectView(R.id.detail_more)
    ToggleButton detailMore;
    @InjectView(R.id.download_ok)
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setContentView(R.layout.download_app_detail);
        ButterKnife.inject(this);
        initData();
    }

    private void initData() {
        check = false;
        detailMore.setChecked(false);
        detailDescription.setMaxLines(MAX_LINE_LIMIT);
        if (appDetail != null) {
            ImageDownloadModule.getInstance().displayImage(appDetail.getImageUrl(), detailAppIcon);
            detailAppName.setText(appDetail.getName());
            detailType.setText(appDetail.getType());
            detailSize.setText(appDetail.getSize());
            detailVersion.setText(appDetail.getVersion());
            detailAmount.setText(String.format(getContext().getString(R.string.download_detail_amount), appDetail.getDownloadAmount()));
            detailDescription.setText(appDetail.getDescription() + " ");
//            detailDescription.setText("asdfasdfjjj见附件附件附件附件aadfs家开始的法律框架在现场" +
//                    "v哦iuqwerlknzxcv dfklajsdf立刻将全额外人去玩儿去玩儿 阿士大夫  更换即可" +
//                    "让他也让他也认同将全额外人一。");
//                    "玩儿去玩儿 阿将全额外人去玩儿去玩儿 阿将全额外人" +
//                    "去玩儿去将全额外人去玩儿去玩儿 阿将全额外人去玩儿去玩儿 阿将全额外人去玩儿去玩儿 阿" +
//                    "将全额外人去玩儿去玩儿 阿将全额外人去玩儿去玩儿 阿玩儿 阿与");
            L.v(TAG, "initData", "mainthread Id=" + Thread.currentThread().getId());
            detailDescription.post(new Runnable() {
                @Override
                public void run() {
                    L.v(TAG, "initData", "thread Id=" + Thread.currentThread().getId());
                    Layout layout = detailDescription.getLayout();
                    if (layout != null) {
                        int lineCount = detailDescription.getLineCount();
                        if (lineCount > 0) {
                            int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                            if (ellipsisCount > 0) {
                                detailMore.setVisibility(View.VISIBLE);
                            } else {
                                detailMore.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });

            initRecommend(appDetail);
            initImages(appDetail);
            onResume();
        }
        ApplicationUtil.nonWiFiToast();
    }

    private void initRecommend(AppDetail appDetail) {
        detailStarsLayout.removeAllViews();
        int recommend = 0;
        if (appDetail.getRecommend() != null) {
            recommend = Integer.parseInt(appDetail.getRecommend());
        }
        for (int i = 0; i < 5; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtil.dp2px(11), ScreenUtil.dp2px(11)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageView.getLayoutParams());
            lp.setMargins(ScreenUtil.dp2px(1), 0, ScreenUtil.dp2px(1), 0);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(lp);
            if (recommend > 0) {
                imageView.setImageResource(R.drawable.star_solid);
                recommend--;
            } else {
                imageView.setImageResource(R.drawable.star_empty);
            }
            detailStarsLayout.addView(imageView);
        }
    }

    private void initImages(AppDetail appDetail) {
        detailImagesLayout.removeAllViews();
        ArrayList<String> imageUrlList = appDetail.getImageList();
        if (imageUrlList != null) {
            int imageCount = imageUrlList.size();
            for (int i = 0; i < imageCount; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.dp2px(100)));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageView.getLayoutParams());
                lp.setMargins(ScreenUtil.dp2px(4), 0, ScreenUtil.dp2px(4), 0);
                imageView.setLayoutParams(lp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageDownloadModule.getInstance().displayImage(imageUrlList.get(i), imageView);
                detailImagesLayout.addView(imageView);
                L.v(TAG, "initImages", "imageurl=" + imageUrlList.get(i) + "width" + imageView.getMeasuredWidth());
            }
        }
    }

    public void onResume() {
        if (ApplicationUtil.isDownloadFinish(appDetail.getId())) {
            download.setText(R.string.download_detail_install);
        } else {
            download.setText(R.string.download_detail_download);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @OnCheckedChanged(R.id.detail_more)
    void more(boolean checked) {
        if (checked) {
            detailDescription.setMaxLines(Integer.MAX_VALUE);
            check = true;
        } else {
            detailDescription.setMaxLines(MAX_LINE_LIMIT);
            check = false;
        }
    }

    @OnClick(R.id.detail_description)
    void clickDescription() {
        check = !check;
        detailMore.setChecked(check);
    }

    @OnClick(R.id.download_ok)
    public void startDownload() {
        if (appDetail != null) {
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(appDetail.getId());
            if (offlineCache != null && offlineCache.getCacheDownloadState() == OfflineCache.CACHE_STATE_FINISH) {
                StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());
                dismiss();
                return;
            }
        }
        if (onDialogClickListener != null) {
            onDialogClickListener.onClick();
            Toast.makeText(context, R.string.download_detail_toast, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    public void setAppDetail(AppDetail appDetail) {
        this.appDetail = appDetail;
    }

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }

    public interface OnDialogClickListener {
        public void onClick();
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case HomeActivity.MSG_WHAT_ACTIVITY_RESUME:
                onResume();
                break;
            default:
                break;
        }
    }

}

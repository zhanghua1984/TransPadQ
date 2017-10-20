package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MultimediaAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;

/**
 * Created by user on 2015/5/15.
 */
public class MediaAddDialog extends Dialog {
    private static final String TAG = "MediaAddDialog";
    Context mContext;
    ArrayList<MediaFile> mediaFileList;
    MultimediaAdapter multimediaAdapters;

    public MediaAddDialog(Context context) {
        super(context);
        mContext = context;
    }

    public MediaAddDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    public void setMediaList(ArrayList<MediaFile> mediaFileList) {
        this.mediaFileList = mediaFileList;
    }

    @InjectView(R.id.tv_media_description)
    TextView tv_media_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_add_dialog);
        ButterKnife.inject(this);

    }

    public void setAdapterNotifyChanged(MultimediaAdapter multimediaAdapter) {
        multimediaAdapters = multimediaAdapter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        tv_media_description.setText(String.format(mContext.getString(R.string.scanning_file_message), mediaFileList.size()));
    }

    @OnClick(R.id.media_add_ok)
    public void addMusics() {
        StorageModule.getInstance().addFileList(mediaFileList);
        multimediaAdapters.notifyDataSetChanged();
        dismiss();
    }

    @OnClick(R.id.media_button_cancle)
    public void mCancel() {
        dismiss();
    }

}

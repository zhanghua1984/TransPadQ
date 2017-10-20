package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;

/**
 * 音频专辑列表对话框
 * Created by wangyang on 2015/1/23.
 */
public class AudioAlbumListDialog extends Dialog {
    private static final String TAG = AudioAlbumListDialog.class.getSimpleName();
    private View content;
    @InjectView(R.id.rvAudioList)
    RecyclerView mAudioAlbumListRecyclerView;
    private OnAlbumDialogClickListener mOnAlbumDialogClickListener;
    private ArrayList<MediaFile> mMediaFolderList = null;
    private AlbumAdapter mAlbumAdapter = null;

    public AudioAlbumListDialog(Context context) {
        super(context, R.style.dialog_base);
        init(context);
    }

    public AudioAlbumListDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    protected AudioAlbumListDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_audio_album_list, null);
        super.setContentView(content);
        ButterKnife.inject(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        mAudioAlbumListRecyclerView.setLayoutManager(mLayoutManager);
        mAlbumAdapter = new AlbumAdapter(context);
        mAudioAlbumListRecyclerView.setAdapter(mAlbumAdapter);
    }

    @OnClick(R.id.btnSure)
    public void onSureClick() {
        if (mOnAlbumDialogClickListener != null) {
            ArrayList<MediaFile> parentNameSelectedList = new ArrayList<>();
            for (MediaFile mediaFile : mMediaFolderList) {
                if (mediaFile.getMediaFileIsCheckedChoise()) {
                    parentNameSelectedList.add(mediaFile);
                }
            }
            L.v(TAG, "onSureClick", "parentNameSelectedList.size=" + parentNameSelectedList.size());
            mOnAlbumDialogClickListener.onOkClick(parentNameSelectedList);
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
        mMediaFolderList = StorageModule.getInstance().getFolderList();
        if (mMediaFolderList == null) {
            mMediaFolderList = new ArrayList<>();
        }
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaFileName("秘密花园");
        mediaFile.setMediaFileIsEncrypt(true);
        mMediaFolderList.add(0, mediaFile);
        mediaFile = new MediaFile();
        mediaFile.setMediaFileName("aaaa");
        mediaFile.setMediaFileIsEncrypt(false);
        mMediaFolderList.add(mediaFile);
        mediaFile = new MediaFile();
        mediaFile.setMediaFileName("bbbb");
        mediaFile.setMediaFileIsEncrypt(false);
        mMediaFolderList.add(mediaFile);
        mediaFile = new MediaFile();
        mediaFile.setMediaFileName("cccc");
        mediaFile.setMediaFileIsEncrypt(false);
        mMediaFolderList.add(mediaFile);
        mediaFile = new MediaFile();
        mediaFile.setMediaFileName("dddd");
        mediaFile.setMediaFileIsEncrypt(false);
        mMediaFolderList.add(mediaFile);
        mAlbumAdapter.notifyDataSetChanged();
    }

    /**
     * Created by user on 2015/1/23.
     */
    public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

        private Context mContext = null;
        private LayoutInflater mLayoutInflater;

        public AlbumAdapter(Context context) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = mLayoutInflater.inflate(R.layout.item_dialog_audio_album_list,
                    viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            MediaFile mediaFile = mMediaFolderList.get(i);
            //设置文件名
            viewHolder.mAudioAlbumNameTextView.setText(mediaFile.getMediaFileName());

            //设置选择框
            if (mediaFile.getMediaFileIsCheckedChoise()) {
                viewHolder.mAudioAlbumChoiseCheckBox.setVisibility(View.VISIBLE);
                viewHolder.mAudioAlbumChoiseCheckBox.setChecked(true);
            } else {
                viewHolder.mAudioAlbumChoiseCheckBox.setVisibility(View.GONE);
            }

            if (i == mMediaFolderList.size() - 1) {
                viewHolder.mLineView.setVisibility(View.GONE);
            } else {
                viewHolder.mLineView.setVisibility(View.VISIBLE);
            }

            //选择专辑
            viewHolder.mAlbumItemRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MediaFile mediaFile = mMediaFolderList.get(i);
                    viewHolder.mAudioAlbumChoiseCheckBox.setChecked(!mediaFile.getMediaFileIsCheckedChoise());
                    mediaFile.setMediaFileIsCheckedChoise(!mediaFile.getMediaFileIsCheckedChoise());

                    if (mediaFile.getMediaFileIsCheckedChoise()) {
                        viewHolder.mAudioAlbumChoiseCheckBox.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.mAudioAlbumChoiseCheckBox.setVisibility(View.GONE);
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return mMediaFolderList != null ? mMediaFolderList.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View view) {
                super(view);
                ButterKnife.inject(this, view);
            }

            //音频名称
            @InjectView(R.id.tvAlbumName)
            TextView mAudioAlbumNameTextView;

            //是否选择音频
            @InjectView(R.id.cbAlbumChoise)
            CheckBox mAudioAlbumChoiseCheckBox;

            //是否选择音频
            @InjectView(R.id.line1)
            View mLineView;

            //背景
            @InjectView(R.id.rlAlbumItem)
            RelativeLayout mAlbumItemRelativeLayout;

        }
    }

    public void setOnAlbumDialogClickListener(OnAlbumDialogClickListener onAlbumDialogClickListener) {
        mOnAlbumDialogClickListener = onAlbumDialogClickListener;
    }

    public interface OnAlbumDialogClickListener {
        public void onOkClick(List<MediaFile> parentNameList);
    }
}

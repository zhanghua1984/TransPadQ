package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.player.AudioPlayer;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;

/**
 * Created by user on 2015/4/16.
 */
public class MusicListAdapter extends BaseAdapter {
    public static final String TAG = MusicListAdapter.class.getSimpleName();
    public static final int SORTBYDEFAULT = 0;
    public static final int SORTBYMODIFYTIME = 1;
    public static final int SORTBYNAME = 2;
    Context context;
    ListView listView;
    ArrayList<MediaFile> musicList;
    int preChoosePosition = -1;
    boolean isPlayMode = true;
    private HashMap<String, Integer> mMediaFileHashMap = new HashMap<>();

    public MusicListAdapter(Context context, ListView listView) {
        this.context = context;
        this.listView = listView;
        L.v(TAG, "MusicListAdapter", "listview=" + listView);
    }

    public void setMusicList(ArrayList<MediaFile> arrayList) {
//        sortMusicList(sortType);
        if (arrayList != null) {
//            int position = 0;
//            for (MediaFile mediaFile : arrayList) {
//
//                mMediaFileHashMap.put(mediaFile.getMediaFilePath(), position++);
//            }
            musicList = arrayList;
        }
    }

    public void setPlayMode(boolean isPlayMode) {
        this.isPlayMode = isPlayMode;
    }

    @Override
    public int getCount() {
        return musicList != null ? musicList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return musicList != null ? musicList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewData(viewHolder, position);

        final MediaFile mediaFile = musicList.get(position);
        viewHolder.musicListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayMode) {
//                播放模式
                    L.v(TAG, "getView", "playMode");
//                    //将上一次播放的音频状态置为未播放
//                    String url = SharedPreferenceModule.getInstance().getString(MediaFile.CURRENT_PLAY_URL,null);
//                    if (url!=null){
//                        int position = mMediaFileHashMap.get(url);
//                        MediaFile preMediaFile = musicList.get(position);
//                        preMediaFile.setMediaFileIsPlaying(false);
//                    }

                    //将本次播放的音频状态置为播放
//                    PlayerUtil.openAudioPLayer(context, mediaFile);
                    PlayerUtil.openAudioPLayer(context, musicList, position);
                } else {
//                    选择模式
                    MediaFile mediaFile = musicList.get(position);

                    if (mediaFile.getMediaFileIsCheckedChoise()) {
                        mediaFile.setMediaFileIsCheckedChoise(false);
                    } else {
                        mediaFile.setMediaFileIsCheckedChoise(true);
                    }
//                    MusicListAdapter.this.notifyDataSetChanged();
                    updateView(position);
                }
            }
        });
        viewHolder.musicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaFile.setMediaFileIsPlaying(true);
                MusicListAdapter.this.notifyDataSetChanged();
                AudioPlayer.getInstance().play();
            }
        });
        viewHolder.musicPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                暂停
                mediaFile.setMediaFileIsPlaying(false);
                MusicListAdapter.this.notifyDataSetChanged();
                AudioPlayer.getInstance().pause();
//                viewHolder.musicName.setTextColor(Color.parseColor("#ffffff"));
//                viewHolder.musicSinger.setTextColor(Color.parseColor("#ffffff"));
//                viewHolder.musicPause.setVisibility(ImageView.INVISIBLE);
            }
        });

        return convertView;

    }

    public void updateView(int position) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int choosePosition = position - firstVisiblePosition;
        View view = listView.getChildAt(choosePosition);
        if (view != null) {
            Object object = view.getTag();
            if (object instanceof ViewHolder) {
                ViewHolder viewHolder = (ViewHolder) object;
                setViewData(viewHolder, position);
            } else {
                L.e(TAG, "updateView", "object is not viewHolder " + object + "position=" + position);
            }
        } else {
            L.e(TAG, "updateView", "view = null" + "position=" + position);
        }
    }

    public void setViewData(ViewHolder viewHolder, int position) {
        MediaFile mediaFile = musicList.get(position);
//        L.v(TAG, "setViewData", "checked" + position + " 选择 " + mediaFile.getMediaFileIsCheckedChoise());
//        L.v(TAG, "setViewData", "播放" + isPlayMode + "文件" + position + mediaFile.getMediaFileIsPlaying());
        if (isPlayMode) {
            if (mediaFile.getMediaFileIsPlaying()) {
                viewHolder.musicName.setTextColor(Color.parseColor("#ff8400"));
                viewHolder.musicPause.setVisibility(ImageView.VISIBLE);
                viewHolder.musicPlay.setVisibility(ImageView.INVISIBLE);
            } else {
                viewHolder.musicName.setTextColor(Color.parseColor("#ffffff"));
                viewHolder.musicPause.setVisibility(ImageView.INVISIBLE);
//                播放三角
//                viewHolder.musicPlay.setVisibility(ImageView.INVISIBLE);
//                String url = SharedPreferenceModule.getInstance().getString(MediaFile.CURRENT_PLAY_URL);
//                int prePosition = mMediaFileHashMap.get(url);
//                if (AudioPlayer.getInstance().isPause()&&position==prePosition){
//                    viewHolder.musicPlay.setVisibility(ImageView.VISIBLE);
//                }

            }
            viewHolder.musicChoose.setVisibility(ImageView.INVISIBLE);
        } else {
            viewHolder.musicChoose.setVisibility(ImageView.VISIBLE);
            if (mediaFile.getMediaFileIsCheckedChoise()) {
//                L.v(TAG, "选择" + mediaFile.getMediaFileIsCheckedChoise());
                viewHolder.musicChoose.setImageResource(R.drawable.media_choose);
            } else {
                viewHolder.musicChoose.setImageResource(R.drawable.media_unchoose);
            }
            viewHolder.musicPause.setVisibility(ImageView.INVISIBLE);
            viewHolder.musicPlay.setVisibility(ImageView.INVISIBLE);
        }
        viewHolder.musicName.setText(mediaFile.getMediaFileName());
    }

    static class ViewHolder {

        @InjectView(R.id.music_playlist_item)
        RelativeLayout musicListItem;
        @InjectView(R.id.music_name)
        TextView musicName;
        @InjectView(R.id.music_play)
        ImageView musicPlay;
        @InjectView(R.id.music_pause)
        ImageView musicPause;
        @InjectView(R.id.music_choose)
        ImageView musicChoose;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }
}

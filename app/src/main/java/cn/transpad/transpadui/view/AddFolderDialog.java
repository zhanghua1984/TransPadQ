package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MultimediaAdapter;
import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by wangshaochun on 2015/4/21.
 */
public class AddFolderDialog extends Dialog {
    MultimediaAdapter multimediaAdapter;
    ArrayList<MediaFile> folderList;
    public AddFolderDialog(Context context){
        super(context);
    }
    public AddFolderDialog(Context context, int theme,MultimediaAdapter multimediaAdapter,ArrayList<MediaFile> folderList) {
        super(context, theme);
        this.multimediaAdapter = multimediaAdapter;
        this.folderList = folderList;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
    }
    //新建文件夹
    @OnClick(R.id.bt_ok)
    public void addFolder(){
        String folderName = et_folder_name.getText().toString().trim();
        if(TextUtils.isEmpty(folderName)){
//            Toast.makeText(getContext(),"文件夹名称不能为空",Toast.LENGTH_SHORT).show();
        }else{
//            MediaFile mediaFile = new MediaFile();
//            mediaFile.setMediaFileName(folderName);
//            mediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_NEW_TYPE);
//            folderList.add(0,mediaFile);
//            multimediaAdapter.notifyDataSetChanged();
//            Toast.makeText(getContext(),"添加未实现",Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
    //取消
    @OnClick(R.id.bt_cancel)
    public void cancelAdd(){
        dismiss();
    }
    @InjectView(R.id.et_folder_name)
    EditText et_folder_name;
}

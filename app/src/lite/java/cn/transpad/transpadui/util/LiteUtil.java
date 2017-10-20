package cn.transpad.transpadui.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.storage.SharedPreferenceModule;

/**
 * Created by left on 15/12/28.
 */
public class LiteUtil {

    public static void addMyApp(List<Shortcut> shortcuts) {
        List<Shortcut> myAppList = getMyAppList();
        if (myAppList != null && myAppList.size() > 0){
            for (Shortcut myApp : myAppList) {
                if (shortcuts.contains(myApp)){
                    shortcuts.remove(myApp);
                }
            }
        }
        if (myAppList == null){
            myAppList = shortcuts;
        }else {
            myAppList.addAll(0,shortcuts);
        }
        saveMyAppList(myAppList);
    }

    public static void saveMyAppList(List<Shortcut> shortcuts) {
        Gson gson = new Gson();
        if (shortcuts !=null && !shortcuts.isEmpty()) {
            String shortcutsList = gson.toJson(shortcuts);
            SharedPreferenceModule.getInstance().setString("shortcuts", shortcutsList);
        }else {
            SharedPreferenceModule.getInstance().setString("shortcuts", "");
        }
    }

    public static List<Shortcut> getMyAppList(){
        String shortcutsList = SharedPreferenceModule.getInstance().getString("shortcuts","");
        List<Shortcut> shortcuts = null;
        if (!TextUtils.isEmpty(shortcutsList)){
            Gson gson = new Gson();
            shortcuts = gson.fromJson(shortcutsList,new TypeToken<ArrayList<Shortcut>>(){}.getType());
        }
        return shortcuts;
    }

}

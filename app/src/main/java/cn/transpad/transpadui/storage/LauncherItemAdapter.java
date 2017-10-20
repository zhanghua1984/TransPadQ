package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import ca.laplanete.mobile.pageddragdropgrid.Item;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/4/9.
 */
public class LauncherItemAdapter implements IFoneDatabase {
    private static final String TAG = FileDataBaseAdapter.class.getSimpleName();
    private static final LauncherItemAdapter mInstance = new LauncherItemAdapter();
    private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
            .getInstance();
    private static Context sContext = null;

    private LauncherItemAdapter() {

    }

    static LauncherItemAdapter getInstance() {
        return mInstance;
    }

    static void init(Context context) {
        sContext = context;
    }

    /**
     * 添加桌面对象
     *
     * @param app 桌面对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int addItem(App app) {
        int id = -1;
        try {
            mGeneralDataBaseTemplate.open();

            // 添加
            long newId;
            ContentValues contentValues = new ContentValues();
            contentValues.put(LAUNCHER_PAGE_ID,
                    app.getPageId());
            contentValues.put(LAUNCHER_ITEM_NAME,
                    app.getName());
            contentValues.put(LAUNCHER_ITEM_IS_INSTALL,
                    app.isInstall());
            contentValues.put(LAUNCHER_ITEM_PACKAGE_NAME,
                    app.getPackageName());
            contentValues.put(LAUNCHER_ITEM_ACTIVITY_NAME,
                    app.getActivityName());
            contentValues.put(LAUNCHER_ITEM_DOWNLOAD_URL,
                    app.getDownloadUrl());
            contentValues.put(LAUNCHER_ITEM_IMAGE_URL,
                    app.getImageUrl());
            contentValues.put(LAUNCHER_ITEM_INDEX,
                    app.getIndex());
            newId = mGeneralDataBaseTemplate.insert(TB_LAUNCHER_ITEM, null,
                    contentValues);

            id = newId > 0 ? (int)newId : -1;
        } catch (Exception e) {
            e.printStackTrace();
            L.v(TAG, e.getMessage());
            return -1;
        } finally {
            mGeneralDataBaseTemplate.close();
        }
        return id;
    }

    /**
     * 获得页面集合
     *
     * @return ArrayList<App> 文件夹集合
     */
    public ArrayList<App> getAppList() {
        StringBuilder sqlForder = new StringBuilder("select * from "
                + TB_LAUNCHER_ITEM);
        ArrayList<App> appList = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();

            cursor = mGeneralDataBaseTemplate.select(sqlForder.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            appList = new ArrayList<App>();
            while (cursor.moveToNext()) {
                App app = new App();
                app.setId(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_ID)));
                app.setPageId(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_PAGE_ID)));
                app.setIsInstall(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_IS_INSTALL)) == 1);
                app.setName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_NAME)));
                app.setPackageName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_PACKAGE_NAME)));
                app.setActivityName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_ACTIVITY_NAME)));
                app.setDownloadUrl(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_DOWNLOAD_URL)));
                app.setImageUrl(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_IMAGE_URL)));
                app.setIndex(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_INDEX)));
                appList.add(app);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        return appList;
    }

    /**
     * 根据pageId获得页面集合
     * @param pageId 页面Id
     * @return ArrayList<App> 文件夹集合
     */
    public ArrayList<Item> getAppListByPageId(int pageId) {
        StringBuilder sqlForder = new StringBuilder("select * from "
                + TB_LAUNCHER_ITEM + " where " + LAUNCHER_PAGE_ID + " = " + pageId + " order by " + LAUNCHER_ITEM_INDEX + " asc");
        ArrayList<Item> appList = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();

            cursor = mGeneralDataBaseTemplate.select(sqlForder.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            appList = new ArrayList<Item>();
            while (cursor.moveToNext()) {
                App app = new App();
                app.setId(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_ID)));
                app.setPageId(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_PAGE_ID)));
                app.setIsInstall(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_IS_INSTALL)) == 1);
                app.setName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_NAME)));
                app.setPackageName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_PACKAGE_NAME)));
                app.setActivityName(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_ACTIVITY_NAME)));
                app.setDownloadUrl(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_DOWNLOAD_URL)));
                app.setImageUrl(cursor.getString(cursor
                        .getColumnIndex(LAUNCHER_ITEM_IMAGE_URL)));
                app.setIndex(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_ITEM_INDEX)));
                appList.add(app);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        return appList;
    }

    /**
     * 更新应用
     *
     * @param app 下载对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int updateApp(App app) {
        L.v(TAG, "updateApp", "start");
        int id = -1;
        try {
            mGeneralDataBaseTemplate.open();

            // 添加
            long newId = 0;
            ContentValues contentValues = new ContentValues();
            contentValues.put(LAUNCHER_ITEM_ID,
                    app.getId());
            contentValues.put(LAUNCHER_PAGE_ID,
                    app.getPageId());
            contentValues.put(LAUNCHER_ITEM_NAME,
                    app.getName());
            contentValues.put(LAUNCHER_ITEM_IS_INSTALL,
                    app.isInstall());
            contentValues.put(LAUNCHER_ITEM_PACKAGE_NAME,
                    app.getPackageName());
            contentValues.put(LAUNCHER_ITEM_ACTIVITY_NAME,
                    app.getActivityName());
            contentValues.put(LAUNCHER_ITEM_DOWNLOAD_URL,
                    app.getDownloadUrl());
            contentValues.put(LAUNCHER_ITEM_IMAGE_URL,
                    app.getImageUrl());
            contentValues.put(LAUNCHER_ITEM_INDEX,
                    app.getIndex());
            StringBuilder where = new StringBuilder();
            where.append(LAUNCHER_ITEM_ID);
            where.append("=?");
            newId = mGeneralDataBaseTemplate.update(TB_LAUNCHER_ITEM,
                    contentValues, where.toString(),
                    new String[]{app.getId() + ""});

            id = newId > 0 ? 1 : -1;
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "updateDownload", e.getMessage());
            return -1;
        } finally {

            mGeneralDataBaseTemplate.close();
        }
        return id;
    }

    /**
     * 根据pageId删除桌面对象
     *
     * @param itemId 页面Id
     * @return int 操作状态
     */
    public int deleteItem(int itemId) {

        try {
            mGeneralDataBaseTemplate.open();
            // 删除数据库
            StringBuilder where = new StringBuilder();
            where.append(LAUNCHER_ITEM_ID + "=?");
            mGeneralDataBaseTemplate.delete(TB_LAUNCHER_ITEM,
                    where.toString(), new String[]{itemId + ""});
        } catch (Exception e) {
            e.printStackTrace();
            L.v(TAG, e.getMessage());
            return -1;
        } finally {
            mGeneralDataBaseTemplate.close();
        }
        return 1;
    }
}

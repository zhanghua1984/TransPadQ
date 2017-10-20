package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import cn.transpad.transpadui.entity.Page;
import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/4/9.
 */
public class LauncherPageAdapter implements IFoneDatabase {
    private static final String TAG = FileDataBaseAdapter.class.getSimpleName();
    private static final LauncherPageAdapter mInstance = new LauncherPageAdapter();
    private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
            .getInstance();
    private static Context sContext = null;

    private LauncherPageAdapter() {

    }

    static LauncherPageAdapter getInstance() {
        return mInstance;
    }

    static void init(Context context) {
        sContext = context;
    }

    /**
     * 添加桌面
     *
     * @param page 桌面对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int addPage(Page page) {
        int id = -1;
        try {
            mGeneralDataBaseTemplate.open();

            // 添加
            long newId;
            ContentValues contentValues = new ContentValues();
            contentValues.put(LAUNCHER_PAGE_NAME,page.getName());
            newId = mGeneralDataBaseTemplate.insert(TB_LAUNCHER_PAGE, null,
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
     * @return ArrayList<Page> 文件夹集合
     */
    public ArrayList<Page> getPageList() {
        StringBuilder sqlForder = new StringBuilder("select * from "
                + TB_LAUNCHER_PAGE);
        ArrayList<Page> pageList = null;
        Cursor cursor = null;
        try {
            mGeneralDataBaseTemplate.open();

            cursor = mGeneralDataBaseTemplate.select(sqlForder.toString());

            if (cursor == null) {
                return null;
            }

            // 读取数据
            pageList = new ArrayList<Page>();
            while (cursor.moveToNext()) {
                Page page = new Page();
                page.setId(cursor.getInt(cursor
                        .getColumnIndex(LAUNCHER_PAGE_ID)));
                pageList.add(page);
            }

        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mGeneralDataBaseTemplate.close();
        }

        return pageList;
    }

    /**
     * 根据pageId删除桌面
     *
     * @param pageId 页面Id
     * @return int 操作状态
     */
    public int deletePage(int pageId) {

        try {
            mGeneralDataBaseTemplate.open();
            // 删除数据库
            StringBuilder where = new StringBuilder();
            where.append(LAUNCHER_PAGE_ID + "=?");
            mGeneralDataBaseTemplate.delete(TB_LAUNCHER_PAGE,
                    where.toString(), new String[]{pageId + ""});
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

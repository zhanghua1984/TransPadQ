package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import cn.transpad.transpadui.util.L;


/**
 * 数据库操作模板
 *
 * @author wangyang
 * @since 2014年4月29日
 */
public class OperateDataBaseTemplate {

    private static final String TAG = "CommonDataBaseTemplate";

    private static SQLiteDatabase sSQLiteDatabase;

    private static final OperateDataBaseTemplate mInstance = new OperateDataBaseTemplate();
    private static Context mContext;

    private OperateDataBaseTemplate() {

    }

    public static void init(Context context) {
        mContext = context;
    }

    public static OperateDataBaseTemplate getInstance() {
        return mInstance;
    }

    public SQLiteStatement getSQLiteStatement(String sql) {
        SQLiteStatement sqliteStatement = null;
        if (sSQLiteDatabase != null) {
            sqliteStatement = sSQLiteDatabase.compileStatement(sql);
        } else {
            e("getSQLiteStatement", "sSQLiteDatabase=null");
        }
        return sqliteStatement;
    }

    public synchronized void beginTransaction() {
        if (sSQLiteDatabase != null) {
            sSQLiteDatabase.beginTransaction();
        } else {
            e("beginTransaction", "sSQLiteDatabase=null");
        }
    }

    public synchronized void setTransactionSuccessful() {
        if (sSQLiteDatabase != null) {

            sSQLiteDatabase.setTransactionSuccessful();

        } else {
            e("setTransactionSuccessful", "sSQLiteDatabase=null");
        }
    }

    public synchronized void endTransaction() {

        if (sSQLiteDatabase != null) {

            sSQLiteDatabase.endTransaction();

        } else {
            e("endTransaction", "sSQLiteDatabase=null");
        }

    }

    public void open() {
        sSQLiteDatabase = FoneDatabase.getInstance().openDatabase(false);
        if (sSQLiteDatabase == null) {
            e("open", "sSQLiteDatabase=null");
        }
    }

    public void open(boolean isIgnoreLocked) {

        sSQLiteDatabase = FoneDatabase.getInstance().openDatabase(
                isIgnoreLocked);
        if (sSQLiteDatabase == null) {
            e("open(boolean isReadable)", "sSQLiteDatabase=null");
        }
    }

    public void close() {

        if (sSQLiteDatabase != null) {

            FoneDatabase.getInstance().close();

        }

    }

    public int delete(Uri uri, String where) {
        try {
            int count = mContext.getContentResolver().delete(uri, where, null);
            return count;
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues cv) {
        try {
            Uri newUri = mContext.getContentResolver().insert(uri, cv);
            return newUri;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean update(Uri uri, ContentValues cv, String where) {
        try {
            int count = mContext.getContentResolver().update(uri, cv, where,
                    null);
            return count > 0 ? true : false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cursor select(Uri uri, String where) {
        return mContext.getContentResolver()
                .query(uri, null, where, null, null);
    }

    public Cursor select(Uri uri, String where, String[] selectionArgs) {
        return mContext.getContentResolver().query(uri, null, where,
                selectionArgs, null);
    }

    protected int getDataNum(Uri uri, String where) {
        int num = 0;
        Cursor cursor = mContext.getContentResolver().query(uri, null, where,
                null, null);
        if (cursor != null) {
            num = cursor.getCount();
            cursor.close();
        }

        return num;
    }

    /**
     * Log wrapper
     *
     * @param type
     * @param msg
     */
    public void v(String type, String msg) {
        L.v(TAG, type, msg);
    }

    /**
     * Log wrapper
     *
     * @param type
     * @param msg
     */
    public void e(String type, String msg) {
        L.e(TAG, type, msg);
    }

    protected void insert(SQLiteStatement statement) {
        try {

            statement.executeInsert();

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "insert",
                    "CommonDataBaseTemplate insertBySql" + e.getMessage() + "");
        }
    }

    protected long insert(String table, String nullColumnHack,
                          ContentValues values) {
        try {
            if (sSQLiteDatabase != null) {
                return sSQLiteDatabase.insert(table, nullColumnHack, values);
            } else {
                e("insert(String table, String nullColumnHack,ContentValues values)",
                        "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "insert",
                    "CommonDataBaseTemplate insertBySql" + e.getMessage() + "");
        }
        return -1;
    }

    protected long insertWithOnConflict(String table, String nullColumnHack,
                                        ContentValues values, int conflictAlgorithm) {
        try {
            if (sSQLiteDatabase != null) {
                return sSQLiteDatabase.insertWithOnConflict(table,
                        nullColumnHack, values, conflictAlgorithm);
            } else {
                e("insertWithOnConflict(String table, String nullColumnHack,ContentValues values)",
                        "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            L.w(TAG, "insertWithOnConflict",
                    "CommonDataBaseTemplate insertBySql" + e.getMessage() + "");
        }
        return -1;
    }

    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        try {
            return mContext.getContentResolver().delete(uri, whereClause,
                    whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "delete", "CommonDataBaseTemplate delete" + e.getMessage()
                    + "");
        } finally {
        }
        return 0;
    }

    protected void delete(String sql) {
        try {
            if (sSQLiteDatabase != null) {
                sSQLiteDatabase.execSQL(sql);
            } else {
                e("delete(String sql)", "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "delete", "CommonDataBaseTemplate delete" + e.getMessage()
                    + "");
        } finally {
        }
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        int result = 0;
        try {
            if (sSQLiteDatabase != null) {

                result = sSQLiteDatabase.delete(table, whereClause, whereArgs);

                v("delete", "result=" + result);

            } else {
                e("delete", "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "delete", "CommonDataBaseTemplate delete" + e.getMessage()
                    + "");
        } finally {
        }
        return result;
    }

    public int update(Uri uri, ContentValues values, String whereClause,
                      String[] whereArgs) {
        try {
            return mContext.getContentResolver().update(uri, values,
                    whereClause, whereArgs);

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "update", "CommonDataBaseTemplate update" + e.getMessage()
                    + "");
        } finally {

        }
        return 0;
    }

    public void update(String sql) {
        try {
            if (sSQLiteDatabase != null) {
                sSQLiteDatabase.execSQL(sql);
            } else {
                e("update(String sql)", "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "update", "CommonDataBaseTemplate update" + e.getMessage()
                    + "");
        } finally {
        }
    }

    public int update(String table, ContentValues values, String whereClause,
                      String[] whereArgs) {
        try {
            if (sSQLiteDatabase != null) {
                return sSQLiteDatabase.update(table, values, whereClause,
                        whereArgs);
            } else {
                e("update(String table, ContentValues values, String whereClause,String[] whereArgs)",
                        "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "update", "CommonDataBaseTemplate update" + e.getMessage()
                    + "");
        } finally {

        }
        return 0;
    }

    protected Cursor select(String sql) {
        try {
            if (sSQLiteDatabase != null) {
                return sSQLiteDatabase.rawQuery(sql, null);
            } else {
                e("select(String sql)", "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "select", "CommonDataBaseTemplate select" + e.getMessage()
                    + "");
        }
        return null;
    }

    protected Cursor select(String sql, String[] selectionArgs) {
        try {
            if (sSQLiteDatabase != null) {
                return sSQLiteDatabase.rawQuery(sql, selectionArgs);
            } else {
                e("select(String sql, String[] selectionArgs)",
                        "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "select", "CommonDataBaseTemplate select" + e.getMessage()
                    + "");
        }
        return null;
    }

    protected void execSQL(String sql) {
        try {
            if (sSQLiteDatabase != null) {
                sSQLiteDatabase.execSQL(sql);
            } else {
                e("execSQL", "sSQLiteDatabase=null");
            }

        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "select",
                    "CommonDataBaseTemplate execSQL" + e.getMessage() + "");
        }
    }
}

package cn.transpad.transpadui.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import cn.transpad.transpadui.util.L;


public class FoneDatabase implements IFoneDatabase {
	private static final String TAG = "FoneDatabase";

	private static FoneOpenHelper sFoneOpenHelper;

	private static final String DATABASE_NAME = "transpadui.db";
	public static byte[] _writeLock = new byte[0];

	private static SQLiteDatabase mSQLiteDatabase = null;
	private static final FoneDatabase mFoneDatabase = new FoneDatabase();

	private FoneDatabase() {

	}

	public static synchronized void init(Context context) {

		if (sFoneOpenHelper == null) {
			sFoneOpenHelper = new FoneOpenHelper(context, DATABASE_NAME, null,
					StorageConfig.DATABASE_CURRENT_VERSION);
		}

	}

	public static FoneDatabase getInstance() {
		return mFoneDatabase;
	}

	/**
	 * get SQLiteDatabase instance
	 * 
	 * @return SQLiteDatabase
	 */
	public SQLiteDatabase openDatabase(boolean isIgnoreLocked) {
		if (mSQLiteDatabase == null) {
			mSQLiteDatabase = sFoneOpenHelper.getWritableDatabase();

		}

		if (mSQLiteDatabase != null) {

			while (mSQLiteDatabase.isDbLockedByCurrentThread()
					|| mSQLiteDatabase.isDbLockedByOtherThreads()) {
				// db is locked, keep looping
				if (isIgnoreLocked) {
					L.w(TAG,
							"openDatabase",
							"mSQLiteDatabase locked isIgnoreLocked="
									+ isIgnoreLocked
									+ " currentThread="
									+ mSQLiteDatabase
									.isDbLockedByCurrentThread()
									+ " otherThreads="
									+ mSQLiteDatabase
									.isDbLockedByOtherThreads());
					break;
				} else {
					L.w(TAG,
							"openDatabase",
							"mSQLiteDatabase locked isIgnoreLocked="
									+ isIgnoreLocked
									+ " currentThread="
									+ mSQLiteDatabase
									.isDbLockedByCurrentThread()
									+ " otherThreads="
									+ mSQLiteDatabase
									.isDbLockedByOtherThreads());
				}
				StorageModule.getInstance().writeStackTrace();
			}
			// L.v(TAG, "openDatabase", "mSQLiteDatabase unlocked.");
		} else {
			L.v(TAG, "openDatabase", "mSQLiteDatabase=null");
		}

		return mSQLiteDatabase;
	}

	/**
	 * 关闭数据库
	 */
	public synchronized void close() {

		// if (mSQLiteDatabase != null) {
		//
		// if (mOpenCounter.decrementAndGet() == 0) {
		// // Closing database
		// mSQLiteDatabase.close();
		//
		// }
		// }
	}

	static class FoneOpenHelper extends SQLiteOpenHelper {

		public FoneOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			L.i(TAG, "onCreate", "start");

			// 创建数据库
			StorageConfig.createDatabase(db);
			ContentValues contentValues = new ContentValues();
			contentValues.put(LAUNCHER_PAGE_NAME, "app");
			db.insert(TB_LAUNCHER_PAGE, null,contentValues);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			L.i(TAG, "onUpgrade", "oldVersion=" + oldVersion + " newVersion="
					+ newVersion);
			if (newVersion > oldVersion) { // 旧版本升到新版本

				// 选用合适的数据库升级方案,进行升级
				StorageConfig.upgradeDatabase(db, oldVersion);

			}
		}
	}
}

package cn.transpad.transpadui.storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import cn.transpad.transpadui.util.L;


/**
 * 
 * 用户表适配器
 * 
 * @author wangyang
 * @since 2014年4月29日
 */
final class UserDataBaseAdapter implements IFoneDatabase {
	private static final String TAG = UserDataBaseAdapter.class.getSimpleName();
	private static final UserDataBaseAdapter mInstance = new UserDataBaseAdapter();
	private OperateDataBaseTemplate mGeneralDataBaseTemplate = OperateDataBaseTemplate
			.getInstance();

	private UserDataBaseAdapter() {

	}

	static UserDataBaseAdapter getInstance() {
		return mInstance;
	}

	/**
	 * 
	 * 添加密码
	 * 
	 * @param password
	 *            密码
	 * @return int 操作结果 <br>
	 *         1 成功 <br>
	 *         -1 插入异常 <br>
	 */
	int addPassword(String password) {
		synchronized (TAG) {

			String sql = "insert into " + TB_USER + " (" + USER_PASSWORD
					+ ") values(?);";

			try {
				mGeneralDataBaseTemplate.beginTransaction();

				SQLiteStatement statement = mGeneralDataBaseTemplate
						.getSQLiteStatement(sql);
				statement.bindString(0, password);
				mGeneralDataBaseTemplate.insert(statement);
				mGeneralDataBaseTemplate.setTransactionSuccessful();

			} catch (Exception e) {
				e.printStackTrace();
				L.v(TAG, e.getMessage());
				return -1;
			} finally {
				mGeneralDataBaseTemplate.endTransaction();
				mGeneralDataBaseTemplate.close();
			}
			return 1;
		}
	}

	/**
	 * 
	 * 判断密码是否存在
	 * 
	 * @return boolean 操作结果<br>
	 *         true 存在<br>
	 *         false 不存在
	 */
	public boolean isExistPassword() {
		String sql = "select *  from " + TB_USER + " ;";
		Cursor cursor = mGeneralDataBaseTemplate.select(sql);
		if (cursor != null) {
			int num = cursor.getCount();
			cursor.close();
			return num > 1 ? true : false;
		}
		return false;
	}

	/**
	 * 
	 * 获取密码
	 * 
	 * @return String 操作结果<br>
	 *         null 或 "" 密码不存在<br>
	 */
	public String getUserPassword() {

		// 查询密码
		String sql = "select " + USER_PASSWORD + " from " + TB_USER + ";";
		Cursor cursor = mGeneralDataBaseTemplate.select(sql);

		if (cursor == null) {
			return null;
		}
		String password = "";
		while (cursor.moveToFirst()) {
			password = cursor.getString(cursor.getColumnIndex(USER_PASSWORD));
		}

		cursor.close();
		mGeneralDataBaseTemplate.close();
		return password;
	}

	/**
	 * 
	 * 更新密码
	 * 
	 * @param password
	 *            密码
	 * @return int 插入结果 <br>
	 *         1 成功 <br>
	 *         -1 插入异常 <br>
	 *         -2更新异常 <br>
	 */
	public int updatePassword(String password) {
		try {

			// 判断密码是否存在
			if (isExistPassword()) {

				String sql = "update " + TB_USER + " set " + USER_PASSWORD
						+ "=" + password + ";";
				mGeneralDataBaseTemplate.update(sql);

			} else {

				return addPassword(password);

			}

		} catch (Exception e) {
			return -2;
		}
		return 1;
	}
}

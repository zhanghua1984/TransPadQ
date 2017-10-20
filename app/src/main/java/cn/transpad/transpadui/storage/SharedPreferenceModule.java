package cn.transpad.transpadui.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;

import cn.transpad.transpadui.util.L;


/**
 * 
 * 偏好设置模块.<br>
 * 提供偏好设置管理操作.
 * 
 * @author wangyang
 * @since 2014年4月29日
 */

public class SharedPreferenceModule {
	public static final String FONE_PLAYER_SHARE_PREFERENCE = "fone_player_share_preference";
	private static SharedPreferences mSharedPreferences;
	private static SharedPreferences mDefaultSharedPreferences;
	private static final SharedPreferenceModule sSharedPreferenceModule = new SharedPreferenceModule();
	private static final String TAG = SharedPreferenceModule.class
			.getSimpleName();

	/**
	 * 获取{@link SharedPreferenceModule
	 * SharedPreferenceModule}对象<br>
	 * 单例方法<br>
	 * 同步
	 * 
	 * @return {@link SharedPreferenceModule
	 *         SharedPreferenceModule}对象
	 */
	public static SharedPreferenceModule getInstance() {
		return sSharedPreferenceModule;
	}

	/**
	 * 初始化{@link SharedPreferenceModule
	 * SharedPreferenceModule}
	 * 
	 * @param context
	 *            当前上下文
	 */
	@SuppressLint({ "InlinedApi", "WorldWriteableFiles", "WorldReadableFiles" })
	public static void init(Context context) {

		if (context == null) {
			throw new NullPointerException("Context is null");
		}

		if (mSharedPreferences != null) {
			throw new RuntimeException("SharedPreferences already inited");
		}

		if (VERSION.SDK_INT < 11) {
			mSharedPreferences = context.getSharedPreferences(
					FONE_PLAYER_SHARE_PREFERENCE, Context.MODE_WORLD_WRITEABLE
							| Context.MODE_WORLD_READABLE);
		} else {
			mSharedPreferences = context.getSharedPreferences(
					FONE_PLAYER_SHARE_PREFERENCE, Context.MODE_MULTI_PROCESS
							| Context.MODE_WORLD_WRITEABLE
							| Context.MODE_WORLD_READABLE);
		}

		// 旧版本,用于兼容旧数据
		mDefaultSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

	}

	private SharedPreferenceModule() {

	}

	/**
	 * 
	 * 获取默认SharedPreferences字符串
	 * 
	 * @return String
	 */
	public String getDefaultString(String key) {
		String value = "";
		if (mDefaultSharedPreferences != null) {
			value = mDefaultSharedPreferences.getString(key, "");
		}
		return value;
	}

	/**
	 * 
	 * 设置默认SharedPreferences字符串
	 * 
	 * @return String
	 */
	public void setDefaultString(String key, String value) {
		if (mDefaultSharedPreferences != null) {
			SharedPreferences.Editor editor = mDefaultSharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}

	/**
	 * 根据键获取布尔值.如果键不存在,返回false<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与布尔值对应的键
	 * @return 布尔值
	 */
	public boolean getBoolean(String key) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getBoolean(key, false);
	}

	/**
	 * 根据键获取布尔值.如果键不存在,返回defaultValue<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与布尔值对应的键
	 * @param defaultValue
	 *            默认值
	 * @return 布尔值
	 */
	public boolean getBoolean(String key, boolean defaultValue) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getBoolean(key, defaultValue);
	}

	/**
	 * 根据键存储布尔值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与布尔值对应的键
	 * @param value
	 *            存储的布尔值
	 */
	public void setBoolean(String key, boolean value) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 根据键获取整型值.如果键不存在,返回0<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与整型值对应的键
	 * @return 整形值
	 */
	public int getInt(String key) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getInt(key, 0);
	}

	/**
	 * 根据键获取整型值.如果键不存在,指定的值<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与整型值对应的键
	 * @return 整形值
	 */
	public int getInt(String key, int defaultValue) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getInt(key, defaultValue);
	}

	/**
	 * 根据键存储整型值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与整型值对应的键
	 * @param value
	 *            存储的整型值
	 */
	public void setInt(String key, int value) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("Context is null");
		}
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * 根据键获取长整型值.如果键不存在,返回0<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与长整型值对应的键
	 * @return 长整型值
	 */
	public long getLong(String key) {

		if (mSharedPreferences == null) {
			L.e(TAG, "getLong", "mSharedPreferences=null");
		}

		return mSharedPreferences.getLong(key, 0);
	}

	/**
	 * 根据键获取长整型值.如果键不存在,返回defaultValue<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与长整型值对应的键
	 * @param defaultValue
	 *            如果不存在该key,则返回defaultValue
	 * @return 长整型值
	 */
	public long getLong(String key, long defaultValue) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getLong(key, defaultValue);
	}

	/**
	 * 根据键存储长整型值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与长整型值对应的键
	 * @param value
	 *            存储的长整型值
	 */
	public void setLong(String key, long value) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("Context is null");
		}

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	/**
	 * 根据键获取字符串值.如果键不存在,返回空字符<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与字符串值对应的键
	 * @return 字符串值
	 */
	public String getString(String key) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getString(key, "");
	}

	/**
	 * 根据键获取字符串值.如果键不存在,返回指定的字符<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与字符串值对应的键
	 * @return 字符串值
	 */
	public String getString(String key, String defaultValue) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getString(key, defaultValue);
	}

	/**
	 * 根据键存储字符串值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与字符串值对应的键
	 * @param value
	 *            存储的字符串值
	 */
	@SuppressLint("NewApi")
	public void setString(String key, String value) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("Context is null");
		}

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putString(key, value);
		
		if (VERSION.SDK_INT < 9) {

			editor.commit();

		} else {

			editor.apply();

		}
	}

	/**
	 * 根据键删除字符串值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与字符串值对应的键
	 */
	@SuppressLint("NewApi")
	public void removeString(String key) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("Context is null");
		}

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.remove(key);

		if (VERSION.SDK_INT < 9) {

			editor.commit();

		} else {

			editor.apply();

		}

	}

	/**
	 * 根据键获取单精度浮点值.如果键不存在,返回空字符<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与单精度浮点值对应的键
	 * @return 单精度浮点值
	 */
	public float getFloat(String key) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("SharedPreferences is null");
		}

		return mSharedPreferences.getFloat(key, 0.0f);
	}

	/**
	 * 根据键存储单精度浮点值.<br>
	 * 同步方法
	 * 
	 * @param key
	 *            与单精度浮点值对应的键
	 * @param value
	 *            存储的单精度浮点值
	 */
	public void setFloat(String key, float value) {

		if (mSharedPreferences == null) {
			throw new NullPointerException("Context is null");
		}

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

}

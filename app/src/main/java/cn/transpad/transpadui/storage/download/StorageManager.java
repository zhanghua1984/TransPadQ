package cn.transpad.transpadui.storage.download;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

public class StorageManager {

	private static final String TAG = "StorageManager";
	private HashMap<String, String> pathMap = new HashMap<String, String>();
	private static final String INTERNAL = "internal";
	private static final String EXTERNAL = "exterNal";

	private static StorageManager mStorageManager;

	private StorageManager() {
		getStoragePath();
	}

	private static Context sContext = null;

	public static void init(Context context) {
		sContext = context;
	}

	public static StorageManager getInstance() {
		if (mStorageManager == null) {
			mStorageManager = new StorageManager();
		}
		return mStorageManager;
	}

	public void getStoragePath() {
		ArrayList<String> arrayPath = StorageUtil.getDriveRootPath1();
		L.v(TAG, "getStoragePath", "getDriveRootPath1 : " + arrayPath);
		if (arrayPath == null || !isHasAllStoragePath(arrayPath)) {
			arrayPath = StorageUtil.getDriveRootPath2();
			L.v(TAG, "getStoragePath", "getDriveRootPath2 : " + arrayPath);
			if (arrayPath == null || !isHasAllStoragePath(arrayPath)) {
				L.v(TAG, "getStoragePath",
						"getDriveRootPath2 not find use system path "
								+ arrayPath);
			}
		}

		saveToPathMap(arrayPath);
	}

	/**
	 * 过滤特殊手机扫到的假的外置sd卡路径
	 * 
	 * @param path
	 * @return
	 */
	public boolean filterStoragePath(String path) {
		boolean res = true;
		if (path.equals("/firmware")) {
			res = false;
		}
		return res;
	}

	/**
	 * 特殊手机：只有外置sd卡，没有手机存储
	 * 
	 * @param phone
	 * @return
	 */
	public boolean isSpecialPhone(String phone) {
		L.v(TAG, "isSpecialPhone", "phone : " + phone);
		String phones[] = sContext.getResources().getStringArray(
				R.array.cache_phone_facturers);
		L.v(TAG, "isSpecialPhone", "phones : " + phones);
		boolean res = false;
		for (int i = 0; i < phones.length; i++) {
			if (phones[i].toLowerCase(Locale.getDefault()).equals(
					phone.toLowerCase(Locale.getDefault()))) {
				res = true;
				break;
			}
		}
		return res;
	}

	public String getInternalSDPath() {
		return pathMap.get(INTERNAL);
	}

	public String getExternalSDPath() {
		return pathMap.get(EXTERNAL);
	}

	public void saveToPathMap(ArrayList<String> arrayPath) {
		L.v(TAG, "saveToPathMap", "arrayPath : " + arrayPath);
		pathMap.clear();
		String sysInternalPath = Environment.getExternalStorageDirectory()
				.getPath();
		L.v(TAG, "saveToPathMap", "sysInternalPath>> : " + sysInternalPath);
		if (arrayPath == null) {
			pathMap.put(INTERNAL, sysInternalPath);
			return;
		}

		if (arrayPath.size() > 2) {
			pathMap.put(INTERNAL, sysInternalPath);
			return;
		}

		// 海信手机特殊处理
		if (android.os.Build.MODEL.equals("HS-U939")) {
			for (int i = 0; i < arrayPath.size(); i++) {
				String path = arrayPath.get(i);
				if (path.equals(sysInternalPath) && new File(path).exists()) {// 外置sd
					L.v(TAG, "saveToPathMap", "EXTERNAL2 : " + path);
					if (Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						pathMap.put(EXTERNAL, path);
					}
				} else {// 内置sd
					if (!path.equals(sysInternalPath)
							&& new File(path).exists()
							&& new File(path).canWrite()
							&& filterStoragePath(path)) {
						L.v(TAG, "saveToPathMap", "INTERNAL2 : " + path);
						pathMap.put(INTERNAL, path);
					}
				}
			}
			return;
		}

		for (int i = 0; i < arrayPath.size(); i++) {
			String path = arrayPath.get(i);
			if (path.equals(sysInternalPath)) { // 内置sd
				L.v(TAG, "saveToPathMap", "path.equals(sysInternalPath)");
				if (isSpecialPhone(android.os.Build.MODEL)) {
					pathMap.put(EXTERNAL, path);
					break;
				}
				if (new File(path).exists()) {
					L.v(TAG, "saveToPathMap", "INTERNAL : " + path);
					pathMap.put(INTERNAL, path);
				}
			} else {// 外置sd
				if (new File(path).exists() && new File(path).canWrite()
						&& filterStoragePath(path)) {
					L.v(TAG, "saveToPathMap", "EXTERNAL : " + path);
					pathMap.put(EXTERNAL, path);
				}
			}
		}

		if (pathMap.size() == 0) {
			pathMap.put(INTERNAL, sysInternalPath);
		}

		if (pathMap.size() == 1 && !pathMap.containsValue(sysInternalPath)) {
			pathMap.put(INTERNAL, sysInternalPath);
		}
	}

	/**
	 * 必须只有两个存储路径，其中必须有一个与系统返回路径一样，认为是真正的存储路径
	 * 
	 * @param arrayPath
	 * @return
	 */
	public boolean isHasAllStoragePath(ArrayList<String> arrayPath) {
		boolean res = false;
		if (arrayPath == null) {
			return res;
		}
		String sysInternalPath = Environment.getExternalStorageDirectory()
				.getPath();
		L.v(TAG, "isHasAllStoragePath", "sysInternalPath : " + sysInternalPath);
		if (arrayPath.size() == 2 && arrayPath.contains(sysInternalPath)) {
			for (int i = 0; i < arrayPath.size(); i++) {
				if (!arrayPath.get(i).equals(sysInternalPath)
						&& !new File(arrayPath.get(i)).exists()) {
					res = false;
					break;
				} else {
					res = true;
				}

			}
		}
		return res;
	}

	public static class StorageUtil {

		public static ArrayList<String> getDriveRootPath2() {
			ArrayList<String> arrayPath = new ArrayList<String>();
			try {
				Runtime runtime = Runtime.getRuntime();
				Process proc = runtime.exec("mount");
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				String line;
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					if (line.contains("secure"))
						continue;
					if (line.contains("asec"))
						continue;

					if (line.contains("fat")) {
						String columns[] = line.split(" ");
						if (columns != null && columns.length > 1) {
							arrayPath.add(columns[1]);
						}
					} else if (line.contains("fuse")) {
						String columns[] = line.split(" ");
						if (columns != null && columns.length > 1) {
							arrayPath.add(columns[1]);
						}
					}
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return arrayPath;
		}

		public static ArrayList<String> getDriveRootPath1() {
			ArrayList<String> rootDrive = null;
			String str1 = "/system/etc/vold.fstab";
			if (new File(str1).exists()) {
				try {
					FileReader fr = new FileReader(str1);
					BufferedReader localBufferedReader = new BufferedReader(fr,
							8192 * 2);
					rootDrive = getDriveRootPath_t(localBufferedReader);
					fr.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					L.v(TAG, "getDriveRootPath_t", e.getMessage());
					rootDrive = null;
				} catch (IOException e) {
					e.printStackTrace();
					L.v(TAG, "getDriveRootPath_t", e.getMessage());
					rootDrive = null;
				}
			}
			return rootDrive;
		}

		public static ArrayList<String> getDriveRootPath_t(BufferedReader br) {
			ArrayList<String> worthyCmd = null;
			ArrayList<String> rootDrive = null;
			worthyCmd = new ArrayList<String>();
			rootDrive = new ArrayList<String>();

			String line;
			try {
				while ((line = br.readLine()) != null) {
					L.v(TAG, "getDriveRootPath_t", line);
					if ((!line.contains("#")) && (line.length() != 0)) {
						if (line.contains("mmc")) {
							worthyCmd.add(line);
						}
					}
				}

				for (int i = 0; i < worthyCmd.size(); i++) {
					// 分隔
					String[] stime = worthyCmd.get(i).split(" ");
					rootDrive.add(stime[2]);
				}
			} catch (IOException e) {
				e.printStackTrace();
				L.v(TAG, "getDriveRootPath_t", e.getMessage());
				rootDrive = null;
			}

			// v(getMediaRootPath(), "rootDrive:"+rootDrive);
			// if (!rootDrive.contains("/mnt/sdcard/")) {
			// rootDrive.add("/mnt/sdcard/");
			// }

			return rootDrive;
		}

	}

}

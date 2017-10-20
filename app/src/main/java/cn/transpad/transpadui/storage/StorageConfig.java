package cn.transpad.transpadui.storage;

import android.database.sqlite.SQLiteDatabase;

import cn.transpad.transpadui.util.L;


/**
 * 存储模块配置文件
 *
 * @author wangyang
 * @since 2014年5月12日
 */
public class StorageConfig implements IFoneDatabase {
    private static final String TAG = StorageConfig.class.getSimpleName();
    /**
     * 数据库升级时,该常量指向最新版本(注:此变量值不能低于已发布数据库版本的值,否则异常)
     */
    public static final int DATABASE_CURRENT_VERSION = StorageConfig.FONE_DATABASE_VERSION_4_1_5;
    /**
     * 数据库版本(2.0)
     */
    public static final int FONE_DATABASE_VERSION_2_0 = 1;
    /**
     * 数据库版本<br>
     * 2.1 2.1.9[act 2.1.6]
     */
    public static final int FONE_DATABASE_VERSION_2_1_TO_2_1_9 = 2;
    /**
     * 数据库版本<br>
     * 3.0<br>
     * 3.0.6[act 3.0.8]<br>
     * 3.1[internal 3.0.7][act 3.1.2]<br>
     * 3.2.0[act 3.1.5]<br>
     * 3.2.5<br>
     */
    public static final int FONE_DATABASE_VERSION_3_0_TO_3_2_5 = 3;
    /**
     * 数据库版本(3.3)
     */
    public static final int FONE_DATABASE_VERSION_3_3 = 4;
    /**
     * 数据库版本<br>
     * 3.3.5<br>
     * 3.3.6
     */
    public static final int FONE_DATABASE_VERSION_3_3_5_TO_3_3_6 = 5;
    /**
     * 数据库版本<br>
     * 3.4<br>
     * 3.5[internal 3.4.3][act 3.4.3]<br>
     */
    public static final int FONE_DATABASE_VERSION_3_4_TO_3_5 = 6;
    /**
     * 数据库版本(4.0)
     */
    public static final int FONE_DATABASE_VERSION_4_0 = 7;
    /**
     * 数据库版本(4.0.5)
     */
    public static final int FONE_DATABASE_VERSION_4_0_5 = 8;
    /**
     * 数据库版本(4.1.0)
     */
    public static final int FONE_DATABASE_VERSION_4_1_0 = 9;

    /**
     * 数据库版本(4.1.5)
     */
    public static final int FONE_DATABASE_VERSION_4_1_5 = 10;

    /**
     * 系统默认用户自拍视频路径
     */
    public static final String SYSTEM_AUTODYNE_VIDEO_PATH = "/dcim";
    /**
     * 文件大小过滤条件(1M以下的视频忽略)
     */
    public static final long VIDEO_FILE_SIZE_FILTER_CONDITION = 1024 * 1024;
    /**
     * 文件大小过滤条件(800k以下的ogg文件忽略)
     */
    public static final long AUDIO_FILE_SIZE_FILTER_CONDITION = 800 * 1024;
    /**
     * 图片缓存log开关
     */
    public static final boolean IMAGE_DOWNLOAD_MODULE_LOG = false;
    /**
     * 离线缓存下载管理log开关
     */
    public static final boolean CACHE_DOWNLOAD_MANAGER_LOG = true;
    /**
     * 离线缓存模块log开关
     */
    public static final boolean CACHE_MODULE_LOG = true;
    /**
     * 离线缓存C层数据处理模块log开关
     */
    public static final boolean CACHE_MESSAGE_HANDLER_LOG = true;

    /**
     * 匹配文件
     *
     * @param extension 后缀名
     * @return boolean 是否匹配<br>
     * true 匹配<br>
     * false 不匹配
     */
    public static boolean matchVideoFile(String extension) {
        if (extension == null)
            return false;

        int len = extension.length(); // byHance, to speed up
        switch (len) {
            case 2:
                if (extension.equals("rm") || extension.equals("ts")
                        || extension.equals("tp"))
                    return true;

                return false;
            case 3:
                if (extension.equals("3gp") || extension.equals("mp4")
                        || extension.equals("flv") || extension.equals("avi")
                        || extension.equals("wmv") || extension.equals("mov")
                        || extension.equals("mkv") || extension.equals("vob")
                        || extension.equals("mpg") || extension.equals("f4v")
                        || extension.equals("3g2") || extension.equals("3gp")
                        || extension.equals("amv") || extension.equals("asf")
                        || extension.equals("tta") || extension.equals("g3p"))
                    return true;

                return false;
            case 4:
                if (extension.equals("rmvb") || extension.equals("3gpp")
                        || extension.equals("m2ts") || extension.equals("mpeg"))
                    return true;

                return false;
            case 5:
                if (extension.equals("3gpp2"))
                    return true;

                return false;
            default:
                return false;
        } // switch len
    }

    /**
     * 配置数据库升级方案
     *
     * @param sqliteDatabase 数据库操作对象
     * @return void
     */
    public static void upgradeDatabase(SQLiteDatabase sqliteDatabase,
                                       int oldVersion) {
        switch (oldVersion) {
            case StorageConfig.FONE_DATABASE_VERSION_2_0: // 2.0
                L.i(TAG, "onUpgrade", "2.0 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_2_1_TO_2_1_9: // 2.1-2.1.9
                L.i(TAG, "onUpgrade", "2.1-2.1.9 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_3_0_TO_3_2_5: // 3.0-3.2.5
                L.i(TAG, "onUpgrade", "3.0-3.2.5 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_3_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase3_X_To_4_1_0(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_3_3: // 3.3
                L.i(TAG, "onUpgrade", "3.3 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_3_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase3_X_To_4_1_0(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_3_3_5_TO_3_3_6: // 3.3.5-3.3.6
                L.i(TAG, "onUpgrade", "3.3.5-3.3.6 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_3_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase3_X_To_4_1_0(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_3_4_TO_3_5: // 3.4-3.5
                L.i(TAG, "onUpgrade", "3.4-3.5 oldVersion=" + oldVersion);
                try {
                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_3_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase3_X_To_4_1_0(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "3.4-3.5 error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_4_0: // 4.0
                L.i(TAG, "onUpgrade", "4.0 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_4_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase4_0_X_TO_4_1_5(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_4_0_5: // 4.0.5
                L.i(TAG, "onUpgrade", "4.0.5 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_4_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase4_0_X_TO_4_1_5(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_4_1_0: // 4.1.0
                L.i(TAG, "onUpgrade", "4.1.0 oldVersion=" + oldVersion);
                try {

                    // 添加数据库
                    createDatabase(sqliteDatabase);

                    // 兼容旧数据
//				Upgrade_4_X_DataBaseAdapter.getInstance()
//						.upgradeDatabase4_0_X_TO_4_1_5(sqliteDatabase);

                } catch (Throwable e) {
                    L.e(TAG, "onUpgrade", "error msg : " + e.getMessage());
                }
                break;
            case StorageConfig.FONE_DATABASE_VERSION_4_1_5: // 4.1.5
                L.i(TAG, "onUpgrade", "4.1.5 oldVersion=" + oldVersion);
                // 后续数据库升级,再此编写代码
                break;
            default:
                L.i(TAG, "onUpgrade", "default oldVersion=" + oldVersion);
                break;

        }
    }

    /**
     * 创建数据库
     *
     * @param sqliteDatabase 数据库
     * @return void
     */
    public static void createDatabase(SQLiteDatabase sqliteDatabase) {
        sqliteDatabase.execSQL(CREATE_USER_TABLE);
        L.v(TAG, "createDatabase", CREATE_USER_TABLE);
        sqliteDatabase.execSQL(CREATE_DIRECTORY_TREE_TABLE);
        L.v(TAG, "createDatabase", CREATE_DIRECTORY_TREE_TABLE);
        sqliteDatabase.execSQL(CREATE_OFFLINE_CACHE_FILE_TABLE);
        L.v(TAG, "createDatabase", CREATE_OFFLINE_CACHE_FILE_TABLE);
        sqliteDatabase.execSQL(CREATE_PLAY_RECORD_TABLE);
        L.v(TAG, "createDatabase", CREATE_PLAY_RECORD_TABLE);
//		sqliteDatabase.execSQL(CREATE_NOTIFICATION_TABLE);
//		L.v(TAG, "createDatabase", CREATE_NOTIFICATION_TABLE);
//		sqliteDatabase.execSQL(CREATE_CUSTOM_CHANNEL_TABLE);
//		L.v(TAG, "createDatabase", CREATE_CUSTOM_CHANNEL_TABLE);
//		sqliteDatabase.execSQL(CREATE_FILE_DOWNLOAD_TABLE);
//		L.v(TAG, "createDatabase", CREATE_FILE_DOWNLOAD_TABLE);
        sqliteDatabase.execSQL(CREATE_LAUNCHER_PAGE_TABLE);
        L.v(TAG, "createDatabase", CREATE_LAUNCHER_PAGE_TABLE);
        sqliteDatabase.execSQL(CREATE_LAUNCHER_APP_TABLE);
        L.v(TAG, "createDatabase", CREATE_LAUNCHER_APP_TABLE);
    }
}

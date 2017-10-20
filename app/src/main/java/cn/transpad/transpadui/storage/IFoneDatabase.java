package cn.transpad.transpadui.storage;

/**
 * 数据库配置信息<br>
 * 数据库的详细设计请参照《缓存和存储模块数据库设计》文档
 *
 * @author wangyang
 * @since 2014年4月29日
 */
public interface IFoneDatabase {
    /**
     * 用户表
     */
    final static String TB_USER = "tb_fone_user";
    // 用户ID
    final static String USER_ID = "user_id";
    // 用户密码
    final static String USER_PASSWORD = "user_password";
    /**
     * 创建用户表
     */
    final String CREATE_USER_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ").append(TB_USER).append(" (")
            .append(USER_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(USER_PASSWORD + " TEXT").append(");").toString();


    /**
     * 文件目录树表
     */
    final static String TB_DIRECTORY_TREE = "tb_directory_tree";
    // id
    static final String DIRECTORY_ID = "directory_id";
    // 目录名称,不带后缀
    static final String DIRECTORY_NAME = "directory_name";
    // 目录父文件夹名称
    static final String DIRECTORY_PARENT_NAME = "directory_parent_name";
    // 目录路径
    static final String DIRECTORY_PATH = "directory_path";
    // 目录原始路径(用于加密文件恢复时使用)
    static final String DIRECTORY_ORIGINAL_PATH = "directory_original_path";
    // 目录作者
    static final String DIRECTORY_AUTHOR = "directory_author";
    // 目录更改时间
    static final String DIRECTORY_DATE_MODIFIED = "directory_date_modified";
    // 目录时长
    static final String DIRECTORY_DURATION = "directory_duration";
    // 目录类型(1是文件夹;2是文件)
    static final String DIRECTORY_TYPE = "directory_type";
    // 文件类型(1是视频;2是音频;3是图片)
    static final String FILE_TYPE = "file_type";
    // 文件大小
    static final String FILE_SIZE = "file_size";
    // 文件是否显示(0未显示,1显示)
    static final String FILE_IS_VISIBLE = "file_is_visible";

    /**
     * 创建文件目录树表
     */
    final String CREATE_DIRECTORY_TREE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(TB_DIRECTORY_TREE)
            .append(" (")
            .append(DIRECTORY_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(DIRECTORY_NAME + " TEXT,")
            .append(DIRECTORY_PARENT_NAME + " TEXT,")
            .append(DIRECTORY_PATH + " TEXT UNIQUE,")//唯一
            .append(DIRECTORY_ORIGINAL_PATH + " TEXT,")
            .append(DIRECTORY_AUTHOR + " TEXT,")
            .append(DIRECTORY_DATE_MODIFIED + " LONG,")
            .append(DIRECTORY_DURATION + " LONG,")
            .append(DIRECTORY_TYPE + " INT,")
            .append(FILE_TYPE + " INT,")
            .append(FILE_SIZE + " LONG,")
            .append(FILE_IS_VISIBLE + " INT").append(");").toString();

    /**
     * 离线缓存文件表(TABLE_DOWNLOAD_TASK; TABLE_DOWNLOAD_TASKED)
     */
    final static String TB_OFFLINE_CACHE_FILE = "tb_offline_cache_file";
    final static String TB_OFFLINE_CACHE_FILE_BACK_UP = "tb_offline_cache_file_back_up";
    // ID
    static final String OFFLINE_CACHE_FILE_ID = "id";
    //文件名称
    static final String OFFLINE_CACHE_FILE_NAME = "offline_cache_file_name";
    // 文件图片
    static final String OFFLINE_CACHE_FILE_IMAGE_URL = "offline_cache_file_image_url";
    // 文件版本号
    static final String OFFLINE_CACHE_FILE_VERSION_CODE = "offline_cache_file_version_code";
    // 文件包名
    static final String OFFLINE_CACHE_FILE_PACKAGE_NAME = "offline_cache_file_package_name";
    // 文件搜索关键字
    static final String OFFLINE_CACHE_FILE_KEYWORD = "offline_cache_file_keyword";
    // 文件已经缓存的大小
    static final String OFFLINE_CACHE_FILE_ALREADY_SIZE = "offline_cache_file_already_size";
    // 文件总大小
    static final String OFFLINE_CACHE_FILE_TOTAL_SIZE = "offline_cache_file_total_size";
    // 文件缓存状态(0等待,1下载中,2和3暂停,4完成,5出错,6未缓存)
    static final String OFFLINE_CACHE_FILE_DOWNLOAD_STATE = "offline_cache_file_download_state";
    // 文件下载类型(1离线缓存,2图片,3apk,4边播边下)
    static final String OFFLINE_CACHE_FILE_DOWNLOAD_TYPE = "offline_cache_file_download_type";
    // 缓存本地路径
    static final String OFFLINE_CACHE_FILE_STORAGE_PATH = "offline_cache_file_storage";
    // 文件下载地址
    static final String OFFLINE_CACHE_FILE_DETAIL_URL = "offline_cache_file_detail_url";
    // 文件创建时间
    static final String OFFLINE_CACHE_FILE_CREATE_TIME = "offline_cache_file_create_time";
    // 文件错误码
    static final String OFFLINE_CACHE_FILE_ERROR_CODE = "offline_cache_file_error_code";
    // 文件下载完成是否安装
    static final String OFFLINE_CACHE_FILE_IS_INSTALL = "offline_cache_file_is_install";

    /**
     * 创建离线缓存文件表
     */
    final String CREATE_OFFLINE_CACHE_FILE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(TB_OFFLINE_CACHE_FILE)
            .append(" (")
            .append(OFFLINE_CACHE_FILE_ID + " LONG UNIQUE,")
            .append(OFFLINE_CACHE_FILE_NAME + " TEXT,")
            .append(OFFLINE_CACHE_FILE_IMAGE_URL + " TEXT,")
            .append(OFFLINE_CACHE_FILE_VERSION_CODE + " INT,")
            .append(OFFLINE_CACHE_FILE_PACKAGE_NAME + " TEXT,")
            .append(OFFLINE_CACHE_FILE_KEYWORD + " TEXT,")
            .append(OFFLINE_CACHE_FILE_ALREADY_SIZE + " LONG,")
            .append(OFFLINE_CACHE_FILE_TOTAL_SIZE + " LONG,")
            .append(OFFLINE_CACHE_FILE_DOWNLOAD_STATE + " INT,")
            .append(OFFLINE_CACHE_FILE_DOWNLOAD_TYPE + " INT,")
            .append(OFFLINE_CACHE_FILE_STORAGE_PATH + " TEXT,")
            .append(OFFLINE_CACHE_FILE_DETAIL_URL + " TEXT,")
            .append(OFFLINE_CACHE_FILE_ERROR_CODE + " INT,")
            .append(OFFLINE_CACHE_FILE_IS_INSTALL + " INT,")
            .append(OFFLINE_CACHE_FILE_CREATE_TIME + " LONG").append(");")
            .toString();
    /**
     * 播放记录表
     */
    final static String TB_PLAY_RECORD = "tb_play_record";
    // id
    static final String PLAY_RECORD_ID = "play_record_id";
    // 播放记录类型(1是视频类型;2是音频类型)
    static final String PLAY_RECORD_TYPE = "play_record_type";
    // 节目播放地址
    static final String PLAY_RECORD_PLAY_URL = "play_record_play_url";
    // 节目已经播放的位置
    static final String PLAY_RECORD_ALREADY_PLAY_TIME = "play_record_already_play_time";
    // 节目时长
    static final String PLAY_RECORD_TOTAL_TIME = "play_record_total_time";
    // 播放记录创建的时间
    static final String PLAY_RECORD_CTEATE_TIME = "play_record_create_time";
    // 播放记录视频CID
    static final String PLAY_RECORD_CID = "play_record_cid";
    /**
     * 创建播放记录表
     */
    final String CREATE_PLAY_RECORD_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ").append(TB_PLAY_RECORD)
            .append(" (").append(PLAY_RECORD_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(DIRECTORY_ID + " INT,")
            .append(PLAY_RECORD_TYPE + " INT,")
            .append(PLAY_RECORD_PLAY_URL + " TEXT,")
            .append(PLAY_RECORD_CID + " LONG,")
            .append(PLAY_RECORD_ALREADY_PLAY_TIME + " LONG,")
            .append(PLAY_RECORD_TOTAL_TIME + " LONG,")
            .append(PLAY_RECORD_CTEATE_TIME + " LONG")
            .append(");")
            .toString();
    /**
     * 桌面页面表
     */
    final static String TB_LAUNCHER_PAGE = "tb_launcher_page";
    // id
    static final String LAUNCHER_PAGE_ID = "launcher_page_id";

    static final String LAUNCHER_PAGE_NAME = "launcher_page_name";
    /**
     * 创建桌面页面表
     */
    final String CREATE_LAUNCHER_PAGE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(TB_LAUNCHER_PAGE)
            .append(" (")
            .append(LAUNCHER_PAGE_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(LAUNCHER_PAGE_NAME + " TEXT")
            .append(");").toString();

    /**
     * 桌面应用表
     */
    final static String TB_LAUNCHER_ITEM = "tb_launcher_item";
    // id
    static final String LAUNCHER_ITEM_ID = "launcher_item_id";
    //名称
    static final String LAUNCHER_ITEM_NAME = "launcher_item_name";
    //包名
    static final String LAUNCHER_ITEM_PACKAGE_NAME = "launcher_item_package_name";
    //activity名字
    static final String LAUNCHER_ITEM_ACTIVITY_NAME = "launcher_item_activity_name";
    // 是否安装
    static final String LAUNCHER_ITEM_IS_INSTALL = "launcher_item_is_install";
    //图片url
    static final String LAUNCHER_ITEM_IMAGE_URL = "launcher_item_image_url";
    //下载url
    static final String LAUNCHER_ITEM_DOWNLOAD_URL = "launcher_item_download_url";
    //位置索引
    static final String LAUNCHER_ITEM_INDEX = "launcher_item_index";

    /**
     * 创建桌面应用表
     */
    final String CREATE_LAUNCHER_APP_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(TB_LAUNCHER_ITEM)
            .append(" (")
            .append(LAUNCHER_ITEM_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(LAUNCHER_PAGE_ID + " INT,")
            .append(LAUNCHER_ITEM_NAME + " TEXT,")
            .append(LAUNCHER_ITEM_PACKAGE_NAME + " TEXT,")
            .append(LAUNCHER_ITEM_ACTIVITY_NAME + " TEXT,")
            .append(LAUNCHER_ITEM_IS_INSTALL + " TEXT,")
            .append(LAUNCHER_ITEM_IMAGE_URL + " TEXT,")
            .append(LAUNCHER_ITEM_DOWNLOAD_URL + " TEXT,")
            .append(LAUNCHER_ITEM_INDEX + " INT")
            .append(");").toString();

    /**
     * 文件下载表
     */
    final static String TB_FILE_DOWNLOAD = "tb_file_download";
    // id(自增长)
    static final String FILE_DOWNLOAD_ID = "id";
    // 文件版本号
    static final String FILE_DOWNLOAD_VERSION_CODE = "file_download_version_code";
    // 文件下载状态
    static final String FILE_DOWNLOAD_STATE_TYPE = "file_download_state_type";
    // 文件下载是否显示正在下载通知
    static final String FILE_DOWNLOAD_IS_RUNNING_NOTIFICATION = "file_download_is_running_notification";
    // 文件下载是是否显示已完成通知
    static final String FILE_DOWNLOAD_IS_FINISH_NOTIFICATION = "file_download_is_finish_notification";
    // 文件下载是否安装
    static final String FILE_DOWNLOAD_IS_INSTALL = "file_download_is_install";
    // 文件下载失败时是否提示消息
    static final String FILE_DOWNLOAD_IS_ERROR_TOAST = "file_download_is_error_toast";
    // 文件下载成功上报
    static final String FILE_DOWNLOAD_REPORT_TYPE = "file_download_report_type";
    // 文件下载类型
    static final String FILE_DOWNLOAD_TYPE = "file_download_type";
    // 文件下载是否限速
    static final String FILE_DOWNLOAD_IS_LIMIT_SPEED = "file_download_is_limit_speed";
    // 文件是否需要升级
    static final String FILE_DOWNLOAD_IS_UPGRADE = "file_download_is_upgrade";
    // 文件是否支持切网
    static final String FILE_DOWNLOAD_IS_SUPPORT_SWITCH_NETWORK = "file_download_is_support_switch_network";
    // 文件已经下载的大小
    static final String FILE_DOWNLOAD_ALREADY_SIZE = "file_download_already_size";
    // 文件总大小
    static final String FILE_DOWNLOAD_TOTAL_SIZE = "file_download_total_size";
    // 文件名称
    static final String FILE_DOWNLOAD_FILE_NAME = "file_download_file_name";
    // 文件版本名称
    static final String FILE_DOWNLOAD_VERSION_NAME = "file_download_version_name";
    // 文件描述信息
    static final String FILE_DOWNLOAD_DESC = "file_download_desc";
    // 文件URL
    static final String FILE_DOWNLOAD_URL = "file_download_url";
    // 文件图片URL
    static final String FILE_DOWNLOAD_IMAGE_URL = "file_download_image_url";
    // 文件存储路径
    static final String FILE_DOWNLOAD_STORAGE_PATH = "file_download_storage_path";

    /**
     * 创建文件下载表
     */
    final String CREATE_FILE_DOWNLOAD_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ").append(TB_FILE_DOWNLOAD)
            .append(" (")
            .append(FILE_DOWNLOAD_ID + " INTEGER PRIMARY KEY autoincrement,")
            .append(FILE_DOWNLOAD_VERSION_CODE + " INT,")
            .append(FILE_DOWNLOAD_STATE_TYPE + " INT,")
            .append(FILE_DOWNLOAD_IS_RUNNING_NOTIFICATION + " INT,")
            .append(FILE_DOWNLOAD_IS_FINISH_NOTIFICATION + " INT,")
            .append(FILE_DOWNLOAD_IS_INSTALL + " INT,")
            .append(FILE_DOWNLOAD_IS_ERROR_TOAST + " INT,")
            .append(FILE_DOWNLOAD_REPORT_TYPE + " INT,")
            .append(FILE_DOWNLOAD_TYPE + " INT,")
            .append(FILE_DOWNLOAD_IS_LIMIT_SPEED + " INT,")
            .append(FILE_DOWNLOAD_IS_UPGRADE + " INT,")
            .append(FILE_DOWNLOAD_IS_SUPPORT_SWITCH_NETWORK + " INT,")
            .append(FILE_DOWNLOAD_ALREADY_SIZE + " LONG,")
            .append(FILE_DOWNLOAD_TOTAL_SIZE + " LONG,")
            .append(FILE_DOWNLOAD_FILE_NAME + " TEXT,")
            .append(FILE_DOWNLOAD_VERSION_NAME + " TEXT,")
            .append(FILE_DOWNLOAD_DESC + " TEXT,")
            .append(FILE_DOWNLOAD_URL + " TEXT,")
            .append(FILE_DOWNLOAD_IMAGE_URL + " TEXT,")
            .append(FILE_DOWNLOAD_STORAGE_PATH + " TEXT").append(");")
            .toString();

}

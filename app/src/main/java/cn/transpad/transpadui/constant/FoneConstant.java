package cn.transpad.transpadui.constant;


import cn.transpad.transpadui.http.LoginRst;

/**
 * 静态常量
 * 
 * @author JasonZue
 * @since 2014-5-14
 */

public class FoneConstant {

	/** Sp中 key */
	public static final String FONE_SP_NAME_SP = "userInfo";// 用户信息SP名称
	public static final String AUTO_DOWNLOAD_FLAG_SP = "checkboxAutoDownload"; // wifi自动缓存开关
	public static final String TMP_LIMIT_CHOOSED_ID_FLAG_SP = "limitChoosedID";// 上限容量勾选选中id
	public static final String TMP_LIMIT_ITEM_LONG_FLAG_SP = "limitItemSize";// 上限容量选中容量key(格式化好的容量值)
	public static final String NOTIFY_SWITCH_FLAG_SP = "notifySwitch";// 通知接收开关
	public static final String NOTIFY_AUTO_OPEN_FLAG_SP = "notifyAutoOpen";// 通知服务自动启动开关
	public static final String LAST_CACHE_COUNT_SP = "lastCacheCount";// 上次获取的缓存数量
	public static final String LAST_COLLECTION_COUNT_SP = "lastCollectionCount";// 上次获取的收藏数量
	public static final String LAST_UPDATA_TIME_SP = "lastUpdateTime";// 上次弹升级时间
	public static final String IS_FIRST_USE_SP = "isFirstUse";// 是否第一次使用
	public static final String COLLETION_TV_UPDATE_COUNT = "collectionUpdateCount";// 追播更新数量
	public static final String UPDATE_VERSION = "updateVersion";// 升级版本 用来处理升级成功上报使用
	/** 选中的上限值(long) */
	public static final String TMP_LIMIT_SIZE = "upperSize";
	/** 选中的上限值(long) */
	public static final String TMP_LIMIT_OLD_SIZE = "upperOldSize";
	/** 广播action **/
//	public static final String BROADCAST_LOGIN_SUCCESS = "com.fone.player.broadcast_login_success";

	/** 清除默认设置标志 **/
	public static boolean isClearDefaultSetting = false;

	/*********************************** 升级 *******************************************/
	/** 升级状态 推荐 */
	public static final String SUGGEST_UPDATE = "0"; // 推荐升级
	/** 升级状态 强制 */
	public static final String FORCE_UPDATE = "1"; // 强制升级
	/** 升级状态 已是最新 */
	public static final String ALREADY_NEWLEST = "2"; // 已是最新版本
	/** 首页 */
	public static final int UPDATE_HOME = 0;
	/** 详情页 */
	public static final int UPDATE_DETAIL = 1;
	/** 全屏 */
	public static final int UPDATE_FULLSCREEN = 2;
	/**
	 * 升级弹框位置 0->首页 UPDATE_HOME 1->详情页 UPDATE_DETAIL 2->全屏播放页 UPDATE_FULLSCREEN
	 * (默认赋值 -->UPDATE_HOME)
	 */
	public static int updatePosition = UPDATE_HOME;
	/** 升级信息实体类 默认为空 */
	public static LoginRst loginRst = null;
	/** 记录是否弹出过升级提示 */
	public static boolean isUpdate = false;
	/** 记录是否登陆 */
	public static boolean isLogin = false;

	/*********************************** 声明 *******************************************/
	public static final String FONE_SP_DECLARATION_SP = "fone_declarations";// 法律声明
																			// 提示语
																			// 分享语
																			// sp

	public static final String SETTING_LEGAL_NOTICE = "setting_legal_notice";// 设置法律申明
	public static final String SEARCH_LEGAL_NOTICE = "search_legal_notice";// 搜索法律申明
	public static final String CACHE_LEGAL_NOTICE = "cache_legal_notice";// 缓存法律申明
	public static final String DETAIL_LEGAL_NOTICE = "detail_legal_notice";// 详情法律申明
	public static final String HOT_LEGAL_NOTICE = "hot_legal_notice";// 热点接口法律申明
	public static final String EPISODE_LEGAL_NOTICE = "episode_legal_notice";// 剧集法律申明
	public static final String PROGRAM_LEGAL_NOTICE = "program_legal_notice";// 栏目法律申明
	public static final String LIVE_LEGAL_NOTICE = "live_legal_notice";// 直播法律申明
	public static final String SHARE_WORD = "share_word";// 分享语
	public static final String REGISTER_LEGAL_NOTICE = "register_legal_notice";// 注册条款及隐私声明

}

package com.examw.netschool.app;
/**
 * APP 常量。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public final class Constant {
	/**
	 * 终端类型(2-苹果,3-安卓,4-其他)。
	 */
	public static final int TERMINAL = 3;
	
//	/**
//	 * 用户ID/用户姓名。
//	 */
//	public static final String CONST_USERID = "user_id", CONST_USERNAME = "user_name";
	/**
	 * 班级ID/班级名称。
	 */
	public static final String CONST_CLASS_ID = "class_id", CONST_CLASS_NAME = "class_name";
	/**
	 * 课程资源ID/课程资源名称。
	 */
	public static final String CONST_LESSON_ID = "lesson_id", CONST_LESSON_NAME = "lesson_name";
	/**
	 * 播放记录ID。
	 */
	public static final String CONST_LESSON_RECORD_ID = "lesson_record_id";
	
	/**
	 *  Handler消息Message的What类型为文本消息。
	 */
	public static final int HANLDER_WHAT_MSG = 0;
	/**
	 * Handler消息Message的What类型为更新进度。
	 */
	public static final int HANLDER_WHAT_PROGRESS = 1;
	/**
	 * Handler消息Message的What类型为更新状态。
	 */
	public static final int HANLDER_WHAT_STATE = 2;
	
	
	
	
	
	/**
	 * 导航文件配置。
	 */
	public static final String PREFERENCES_CONFIG_GUIDEFILE = "guidefile";
	/**
	 * 导航文件配置-是否是第一次。
	 */
	public static final String PREFERENCES_CONFIG_GUIDEFILE_ISFIRST = "isfirst_";
	
	/**
	 * 用户密码存储配置。
	 */
	public static final String PREFERENCES_CONFIG_USERPWD = "userpwd";
	/**
	 * 用户密码存储配置-用户ID。
	 */
	public static final String PREFERENCES_CONFIG_USERPWD_USERID = "id_";
	/**
	 * 用户密码存储配置-用户机构。
	 */
	public static final String PREFERENCES_CONFIG_USERPWD_AGENCYID = "agency_";
	/**
	 * 共享用户名。
	 */
	public static final String PREFERENCES_CONFIG_SHARE_USER = "share_username";
	
//	/**
//	 * 当前用户。
//	 */
//	public static final String PREFERENCES_CONFIG_CURRENT_USER = "current_user";
//	/**
//	 * 当前用户-用户ID。
//	 */
//	public static final String PREFERENCES_CONFIG_CURRENT_USER_ID = "current_user_id";
//	/**
//	 * 当前用户-用户姓名。
//	 */
//	public static final String PREFERENCES_CONFIG_CURRENT_USER_NAME = "current_user_name";
}
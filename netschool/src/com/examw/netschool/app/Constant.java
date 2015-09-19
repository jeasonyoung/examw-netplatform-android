package com.examw.netschool.app;
/**
 * APP 常量。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public final class Constant {
	public static final String CONST_USERID = "user_id", CONST_USERNAME = "user_name";
	public static final String CONST_CLASS_ID = "class_id", CONST_CLASS_NAME = "class_name";
	public static final String CONST_LESSON_ID = "lesson_id", CONST_LESSON_NAME = "lesson_name";
	public static final String CONST_LESSON_RECORD_ID = "lesson_record_id";
	
	public static final int PAGE_SIZE = 15;
	
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
	 * HOST API ROOT URL。
	 */
	public static final String DOMAIN_URL = "http://demo.examw.com/examw-netplatform";
	//"http://www.youeclass.com/";
	/**
	 * API 身份验证用户名。
	 */
	public static final String DOMAIN_Username = "hk@test";
	/**
	 * API 身份验证用户密码。
	 */
	public static final String DOMAIN_Password = "test";
	/**
	 * 所属培训机构ID。
	 */
	public static final String DOMAIN_AGENCY_ID = "9bc380e2-4fc7-11e5-bfe6-000d609a8169";
	
	
	/**
	 * 导航文件配置。
	 */
	public static final String PREFERENCES_CONFIG_GUIDEFILE = "guidefile";
	public static final String PREFERENCES_CONFIG_GUIDEFILE_ISFIRST = "isfirst";
	
	/**
	 * 用户密码存储配置。
	 */
	public static final String PREFERENCES_CONFIG_USERPWD = "userpwd";
	public static final String PREFERENCES_CONFIG_USERPWD_USERID = "id_";
	
	/**
	 * 共享用户名。
	 */
	public static final String PREFERENCES_CONFIG_SHARE_USER = "share_username";
	
	/**
	 * 当前用户
	 */
	public static final String PREFERENCES_CONFIG_CURRENT_USER = "current_user";
	public static final String PREFERENCES_CONFIG_CURRENT_USER_ID = "current_user_id";
	public static final String PREFERENCES_CONFIG_CURRENT_USER_NAME = "current_user_name";

	
	
//	public static final String MEDIA_DOMAIN_URL = "v.dalischool.com";
//	public static final String NGINX_URL = "http://v.dalischool.com:8091/";
}
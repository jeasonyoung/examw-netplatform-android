package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 登录反馈结果。
 * 
 * @author jeasonyoung
 * @since 2015年11月11日
 */
public class LoginResult implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 所属机构ID。
	 */
	public String agency_id;
	/**
	 * 随机用户ID。
	 */
	public String rand_user_id;
	/**
	 * 真实姓名。
	 */
	public String real_name;
}
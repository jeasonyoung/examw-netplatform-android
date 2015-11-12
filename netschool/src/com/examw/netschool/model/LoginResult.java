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
	private String agencyId,randUserId,realName;
	/**
	 * 获取agencyId
	 * @return agencyId
	 */
	public String getAgencyId() {
		return agencyId;
	}
	/**
	 * 设置 agencyId
	 * @param agencyId 
	 *	  agencyId
	 */
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	/**
	 * 获取randUserId
	 * @return randUserId
	 */
	public String getRandUserId() {
		return randUserId;
	}
	/**
	 * 设置 randUserId
	 * @param randUserId 
	 *	  randUserId
	 */
	public void setRandUserId(String randUserId) {
		this.randUserId = randUserId;
	}
	/**
	 * 获取realName
	 * @return realName
	 */
	public String getRealName() {
		return realName;
	}
	/**
	 * 设置 realName
	 * @param realName 
	 *	  realName
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	
	
//	/**
//	 * 所属机构ID。
//	 */
//	public String agency_id;
//	/**
//	 * 随机用户ID。
//	 */
//	public String rand_user_id;
//	/**
//	 * 真实姓名。
//	 */
//	public String real_name;
}
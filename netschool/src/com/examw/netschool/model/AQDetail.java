package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 答疑主题明细。
 * 
 * @author jeasonyoung
 * @since 2015年9月20日
 */
public class AQDetail implements Serializable{
	private static final long serialVersionUID = 1L;
	private String id,content,userId,userName,createTime;	
	/**
	 * 获取明细ID。
	 * @return 明细ID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置明细ID。
	 * @param id 
	 *	  明细ID。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取明细内容。
	 * @return 明细内容。
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置明细内容。
	 * @param content 
	 *	  明细内容。
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 获取所属用户ID。
	 * @return 所属用户ID。
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * 设置所属用户ID。
	 * @param userId 
	 *	  所属用户ID。
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取所属用户名称。
	 * @return 所属用户名称。
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置所属用户名称。
	 * @param userName 
	 *	  所属用户名称。
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取时间。
	 * @return 时间。
	 */
	public String getCreateTime() {
		return createTime;
	}
	/**
	 * 设置时间。
	 * @param createTime 
	 *	  时间。
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
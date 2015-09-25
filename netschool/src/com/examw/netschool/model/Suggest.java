package com.examw.netschool.model;

import java.io.Serializable;
/**
 * 学员建议。
 * 
 * @author jeasonyoung
 * @since 2015年9月20日
 */
public class Suggest implements Serializable{
	private static final long serialVersionUID = 1L;
	private String agencyId,studentId,content;
	/**
	 * 获取所属机构ID。
	 * @return 所属机构ID。
	 */
	public String getAgencyId() {
		return agencyId;
	}
	/**
	 * 设置所属机构ID。
	 * @param agencyId 
	 *	  所属机构ID。
	 */
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	/**
	 * 获取所属学员ID。
	 * @return 所属学员ID。
	 */
	public String getStudentId() {
		return studentId;
	}
	/**
	 * 设置所属学员ID
	 * @param studentId 
	 *	  所属学员ID
	 */
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	/**
	 * 获取建议内容。
	 * @return 建议内容。
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置建议内容。
	 * @param content 
	 *	  建议内容。
	 */
	public void setContent(String content) {
		this.content = content;
	}
}
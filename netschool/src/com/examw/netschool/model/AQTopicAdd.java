package com.examw.netschool.model;
/**
 * 答疑主题新增数据模型。
 * 
 * @author jeasonyoung
 * @since 2015年9月25日
 */
public class AQTopicAdd extends AQTopic {
	private static final long serialVersionUID = 1L;
	private String agencyId,studentId;
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
	 * 设置所属学员ID。
	 * @param studentId 
	 *	  所属学员ID。
	 */
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
}
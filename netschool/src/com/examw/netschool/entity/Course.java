package com.examw.netschool.entity;

import com.examw.netschool.util.Constant;
import com.examw.netschool.util.StringUtils;
/**
 * 课程数据模型。
 * @author jeasonyoung
 *
 */
public class Course {
	private long id,fileSize,finishSize;
	private String courseId,courseName,classId,courseType,courseMode,courseGroup,filePath,fileUrl,userName;
	private int state;
	/**
	 * 获取课程标示(_id)。
	 * @return 课程标示。
	 */
	public long getId() {
		return id;
	}
	/**
	 * 设置课程标示(_id)。
	 * @param id
	 * 课程标示。
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 *  获取课程ID(courseid)。
	 * @return 课程ID。
	 */
	public String getCourseId() {
		return courseId;
	}
	/**
	 * 设置课程ID(courseid)。
	 * @param courseId
	 * 课程ID。
	 */
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	/**
	 * 获取课程名称(coursename)。
	 * @return 课程名称。
	 */
	public String getCourseName() {
		return courseName;
	}
	/**
	 * 设置课程名称(coursename)。
	 * @param courseName
	 * 课程名称。
	 */
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	/**
	 * 获取班级ID(classid)。
	 * @return 班级ID。
	 */
	public String getClassId() {
		return classId;
	}
	/**
	 * 设置班级ID(classid)。
	 * @param classid
	 *  班级ID。
	 */
	public void setClassId(String classId) {
		this.classId = classId;
	}
	/**
	 * 获取课程类型(coursetype)。
	 * @return 课程类型。
	 */
	public String getCourseType() {
		return courseType;
	}
	/**
	 * 设置课程类型(coursetype)。
	 * @param courseType
	 * 课程类型。
	 */
	public void setCourseType(String courseType) {
		this.courseType = courseType;
	}
	/**
	 * 获取课程模式(coursemode)。
	 * @return 课程模式。
	 */
	public String getCourseMode() {
		return courseMode;
	}
	/**
	 * 设置课程模式(coursemode)。
	 * @param courseMode
	 * 课程模式。
	 */
	public void setCourseMode(String courseMode) {
		this.courseMode = courseMode;
	}
	/**
	 * 获取课程组(coursegroup)。
	 * @return 课程组。
	 */
	public String getCourseGroup() {
		return courseGroup;
	}
	/**
	 * 设置课程组(coursegroup)。
	 * @param courseGroup
	 * 课程组。
	 */
	public void setCourseGroup(String courseGroup) {
		this.courseGroup = courseGroup;
	}
	/**
	 * 获取视频文件大小(filesize)。
	 * @return 视频文件大小。
	 */
	public long getFileSize() {
		return fileSize;
	}
	/**
	 * 设置视频文件大小(filesize)。
	 * @param filesize
	 * 视频文件大小。
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * 获取下载大小(finishsize)。
	 * @return 下载大小。
	 */
	public long getFinishSize() {
		return finishSize;
	}
	/**
	 * 设置下载大小(finishsize)。
	 * @param finishsize
	 * 下载大小。
	 */
	public void setFinishSize(long finishSize) {
		this.finishSize = finishSize;
	}
	/**
	 * 获取下载路径(filepath)。
	 * @return 下载路径。
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * 设置下载路径(filepath)。
	 * @param filePath
	 * 下载路径。
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * 获取课程URL(fileurl)。
	 * @return 课程URL。
	 */
	public String getFileUrl() {
		if(!StringUtils.isEmpty(fileUrl))
		{
			if(fileUrl.indexOf(Constant.MEDIA_DOMAIN_URL)==-1)
			{
				fileUrl = Constant.NGINX_URL +fileUrl;
			}
		}
		return fileUrl;
	}
	/**
	 * 设置课程URL(fileurl)。
	 * @param fileUrl
	 * 课程URL。
	 */
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	/**
	 * 获取课程状态(state)。
	 * @return 课程状态。
	 */
	public int getState() {
		return state;
	}
	/**
	 * 设置课程状态(state)。
	 * @param state
	 * 课程状态
	 */
	public void setState(int state) {
		this.state = state;
	}
	/**
	 * 获取所属用户(username)。
	 * @return 所属用户。
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置所属用户(username)。
	 * @param username
	 * 所属用户。
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
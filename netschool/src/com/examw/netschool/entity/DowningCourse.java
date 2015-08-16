package com.examw.netschool.entity;

import com.examw.netschool.util.StringUtils;

/**
 * 正在下载的课程模型。
 * @author jeasonyoung
 *
 */
public class DowningCourse {
	private String courseName,filePath,fileUrl,userName;
	private long fileSize,finishSize;
	private int state;
	/**
	 * 初始状态。
	 */
	public static final int STATE_INIT = -1;
	/**
	 * 连接失败状态。
	 */
	public static final int STATE_NETFAIL = -2;
	/**
	 * 暂停状态。
	 */
	public static final int STATE_PAUSE = 0; 
	/**
	 * 下载状态。
	 */
	public static final int STATE_DOWNING = 1;
	/**
	 * 下载完成状态。
	 */
	public static final int STATE_FINISH = 2;
	/**
	 *  下载取消状态。
	 */
	public static final int STATE_CANCEL = 3;
	/**
	 * 等待状态。
	 */
	public static final int STATE_WAITTING = 4;
	/**
	 * 获取课程名称。
	 * @return 课程名称。
	 */
	public String getCourseName() {
		return courseName;
	}
	/**
	 * 设置课程名称。
	 * @param courseName
	 * 课程名称。
	 */
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	/**
	 * 获取视频存储路径。
	 * @return 视频存储路径。
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * 设置视频存储路径。
	 * @param filePath
	 * 视频存储路径。
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * 获取视频大小。
	 * @return 视频大小。
	 */
	public long getFileSize() {
		return fileSize;
	}
	/**
	 * 设置视频大小。
	 * @param fileSize
	 * 视频大小。
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * 获取视频下载大小。
	 * @return 视频下载大小。
	 */
	public long getFinishSize() {
		return finishSize;
	}
	/**
	 * 设置视频下载大小。
	 * @param finishSize
	 * 视频下载大小。
	 */
	public void setFinishSize(long finishSize) {
		this.finishSize = finishSize;
	}
	/**
	 * 获取视频URL。
	 * @return 视频URL。
	 */
	public String getFileUrl() {
		return fileUrl;
	}
	/**
	 * 设置视频URL。
	 * @param fileUrl
	 * 视频URL。
	 */
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	/**
	 * 获取所属用户。
	 * @return 所属用户。
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置所属用户。
	 * @param userName
	 * 所属用户。
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取课程状态。
	 * @return 课程状态。
	 */
	public int getState() {
		return state;
	}
	/**
	 * 设置课程状态。
	 * @param status
	 * 课程状态。
	 */
	public void setState(int state) {
		this.state = state;
	}
	/*
	 * 重载。
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31  + (this.fileUrl == null ? 0 : this.fileUrl.hashCode());
	}
	/*
	 * 重载对象比较
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null) return false;
		if(o instanceof DowningCourse){
			DowningCourse data = (DowningCourse)o;
			if(!StringUtils.isEmpty(this.fileUrl) && !StringUtils.isEmpty(data.getFileUrl())){
				return this.fileUrl.equalsIgnoreCase(data.getFileUrl());
			}
		}
		return true;
	}
}
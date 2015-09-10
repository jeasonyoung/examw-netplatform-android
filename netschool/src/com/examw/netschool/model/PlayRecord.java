package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 播放记录。
 * 
 * @author jeasonyoung
 * @since 2015年9月7日
 */
public class PlayRecord implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id, lessonId,lessonName, createTime;
	private Integer playTime;
	/**
	 * 获取播放记录ID。
	 * @return 播放记录ID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置播放记录ID。
	 * @param id 
	 *	  播放记录ID。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取所属课程资源ID。
	 * @return 所属课程资源ID。
	 */
	public String getLessonId() {
		return lessonId;
	}
	/**
	 * 设置所属课程资源ID。
	 * @param lessonId 
	 *	  所属课程资源ID。
	 */
	public void setLessonId(String lessonId) {
		this.lessonId = lessonId;
	}
	/**
	 * 获取所属课程资源名称。
	 * @return 所属课程资源名称。
	 */
	public String getLessonName() {
		return lessonName;
	}
	/**
	 * 设置所属课程资源名称。
	 * @param lessonName 
	 *	  所属课程资源名称。
	 */
	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}
	/**
	 * 获取播放时间。
	 * @return 播放时间。
	 */
	public Integer getPlayTime() {
		return playTime;
	}
	/**
	 * 设置播放时间。
	 * @param playTime 
	 *	  播放时间。
	 */
	public void setPlayTime(Integer playTime) {
		this.playTime = playTime;
	}
	/**
	 * 获取创建时间。
	 * @return 创建时间。
	 */
	public String getCreateTime() {
		return createTime;
	}
	/**
	 * 设置创建时间。
	 * @param createTime 
	 *	  创建时间。
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
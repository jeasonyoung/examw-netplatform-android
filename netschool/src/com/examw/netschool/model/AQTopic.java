package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 答疑主题。
 * 
 * @author jeasonyoung
 * @since 2015年9月20日
 */
public class AQTopic implements Serializable {
	private static final long serialVersionUID = 1L;
	//private String id,title,content,lesson_id,lesson_name,last_time;
	
	/**
	 * 主题ID。
	 */
	public String id;
	/**
	 * 主题标题。
	 */
	public String title;
	/**
	 * 主题内容
	 */
	public String content;
	/**
	 * 所属课程资源ID。
	 */
	public String lesson_id;
	/**
	 * 所属课程资源名称。
	 */
	public String lesson_name;
	/**
	 * 更新时间。
	 */
	public String last_time;
//	
//	
//	/**
//	 * 获取主题ID。
//	 * @return 主题ID。
//	 */
//	public String getId() {
//		return id;
//	}
//	/**
//	 * 设置主题ID。
//	 * @param id 
//	 *	  主题ID。
//	 */
//	public void setId(String id) {
//		this.id = id;
//	}
//	/**
//	 * 获取所属课程资源ID。
//	 * @return 所属课程资源ID。
//	 */
//	public String getLessonId() {
//		return lessonId;
//	}
//	/**
//	 * 设置所属课程资源ID。
//	 * @param lessonId 
//	 *	  所属课程资源ID。
//	 */
//	public void setLessonId(String lessonId) {
//		this.lessonId = lessonId;
//	}
//	/**
//	 * 获取所属课程资源名称。
//	 * @return 课程资源名称。
//	 */
//	public String getLessonName() {
//		return lessonName;
//	}
//	/**
//	 * 设置课程资源名称。
//	 * @param lessonName 
//	 *	  课程资源名称。
//	 */
//	public void setLessonName(String lessonName) {
//		this.lessonName = lessonName;
//	}
//	/**
//	 * 获取标题。
//	 * @return 标题。
//	 */
//	public String getTitle() {
//		return title;
//	}
//	/**
//	 * 设置标题。
//	 * @param title 
//	 *	  标题。
//	 */
//	public void setTitle(String title) {
//		this.title = title;
//	}
//	/**
//	 * 获取内容。
//	 * @return 内容。
//	 */
//	public String getContent() {
//		return content;
//	}
//	/**
//	 * 设置内容。
//	 * @param content 
//	 *	  内容。
//	 */
//	public void setContent(String content) {
//		this.content = content;
//	}
//	/**
//	 * 获取时间。
//	 * @return lastTime
//	 */
//	public String getLastTime() {
//		return lastTime;
//	}
//	/**
//	 * 设置时间。
//	 * @param lastTime 
//	 *	  时间。
//	 */
//	public void setLastTime(String lastTime) {
//		this.lastTime = lastTime;
//	}
}
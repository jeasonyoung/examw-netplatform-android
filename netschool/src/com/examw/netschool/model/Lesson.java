package com.examw.netschool.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
/**
 * 课程资源数据。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class Lesson implements Serializable,Comparable<Lesson> {
	private static final long serialVersionUID = 1L;
	private String id,name,videoUrl,highVideoUrl,superVideoUrl;
	private Integer time,orderNo;
	/**
	 * 获取课程资源ID。
	 * @return 课程资源ID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置课程资源ID。
	 * @param id 
	 *	  课程资源ID。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取课程资源名称。
	 * @return 课程资源名称。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置课程资源名称。
	 * @param name 
	 *	  课程资源名称。
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取标清视频URL。
	 * @return 标清视频URL。
	 */
	public String getVideoUrl() {
		return videoUrl;
	}
	/**
	 * 设置标清视频URL。
	 * @param videoUrl 
	 *	  标清视频URL。
	 */
	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	/**
	 * 获取高清视频URL。
	 * @return 高清视频URL。
	 */
	public String getHighVideoUrl() {
		return highVideoUrl;
	}
	/**
	 * 设置高清视频URL。
	 * @param highVideoUrl 
	 *	  高清视频URL。
	 */
	public void setHighVideoUrl(String highVideoUrl) {
		this.highVideoUrl = highVideoUrl;
	}
	/**
	 * 获取超清视频URL。
	 * @return 超清视频URL。
	 */
	public String getSuperVideoUrl() {
		return superVideoUrl;
	}
	/**
	 * 设置超清视频URL。
	 * @param superVideoUrl 
	 *	  超清视频URL。
	 */
	public void setSuperVideoUrl(String superVideoUrl) {
		this.superVideoUrl = superVideoUrl;
	}
	/**
	 * 获取优先视频URL。
	 * @return 优先视频URL。
	 */
	public String getPriorityUrl(){
		//标清视频URL
		if(StringUtils.isNotBlank(this.videoUrl)) return this.videoUrl;
		//高清视频URL
		if(StringUtils.isNotBlank(this.highVideoUrl)) return this.highVideoUrl;
		//超清视频URL
		if(StringUtils.isNotBlank(this.superVideoUrl)) return this.superVideoUrl;
		//没有视频
		return null;
	}
	/**
	 * 获取视频时长。
	 * @return 视频时长。
	 */
	public Integer getTime() {
		return time;
	}
	/**
	 * 设置视频时长。
	 * @param time 
	 *	  视频时长。
	 */
	public void setTime(Integer time) {
		this.time = time;
	}
	/**
	 * 获取排序号。
	 * @return 排序号。
	 */
	public Integer getOrderNo() {
		return orderNo;
	}
	/**
	 * 设置排序号。
	 * @param orderNo 
	 *	  排序号。
	 */
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
	/*
	 * 排序比较。
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Lesson o) {
		return this.orderNo - o.orderNo;
	}
}
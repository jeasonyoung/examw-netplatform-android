package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 主界面项目数据模型。
 * 
 * @author jeasonyoung
 * @since 2015年9月26日
 */
public class MainItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private int iconResId,textResId;
	private Class<?> activityClass;
	/**
	 * 构造函数。
	 * @param textResId
	 * 文本资源ID。
	 * @param iconResId
	 * 图标资源ID。
	 * @param activity
	 * Activity
	 */
	public MainItem(int textResId, int iconResId, Class<?> activity){
		
		this.setTextResId(textResId);
		this.setIconResId(iconResId);
		this.setActivityClass(activity);
	}
	/**
	 * 获取图标资源ID。
	 * @return 图标资源ID。
	 */
	public int getIconResId() {
		return iconResId;
	}
	/**
	 * 设置图标资源ID。
	 * @param iconResId 
	 *	  图标资源ID。
	 */
	public void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}
	/**
	 * 获取文本资源ID。
	 * @return 文本资源ID。
	 */
	public int getTextResId() {
		return textResId;
	}
	/**
	 * 设置文本资源ID。
	 * @param textResId 
	 *	  文本资源ID。
	 */
	public void setTextResId(int textResId) {
		this.textResId = textResId;
	}
	/**
	 * 获取activityClass
	 * @return activityClass
	 */
	public Class<?> getActivityClass() {
		return activityClass;
	}
	/**
	 * 设置 activityClass
	 * @param activityClass 
	 *	  activityClass
	 */
	public void setActivityClass(Class<?> activityClass) {
		this.activityClass = activityClass;
	}
}
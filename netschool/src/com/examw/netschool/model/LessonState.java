package com.examw.netschool.model;
/**
 * 课程资源状态。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public enum LessonState {
	/**
	 * 未下载。
	 */
	None(0, "未下载"),
	/**
	 * 下载中。
	 */
	Downloading(1, "下载中"),
	/**
	 * 已下载。
	 */
	Downloaded(2, "已下载");
	
	private int value;
	private String name;
	/**
	 * 构造函数。
	 * @param value
	 * @param name
	 */
	private LessonState(int value, String name){
		this.value = value;
		this.name = name;
	}
	/**
	 * 获取枚举值。
	 * @return 枚举值。
	 */
	public int getValue() {
		return value;
	}
	/**
	 * 获取枚举名称。
	 * @return 枚举名称。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static LessonState parse(Integer value){
		if(value != null){
			for(LessonState state : LessonState.values()){
				if(state.getValue() == value) return state;
			}
		}
		return None;
	}
}
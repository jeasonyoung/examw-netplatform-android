package com.examw.netschool.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
/**
 * 我的课程数据模型。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class PackageClass implements Serializable,Comparable<PackageClass> {
	private static final long serialVersionUID = 1L;
	
	public static final String TYPE_PACKAGE = "package", TYPE_CLASS = "class";
	
	/**
	 * 父ID。
	 */
	public String pid;
	/**
	 * 当前ID。
	 */
	public String id;
	/**
	 * 名称。
	 */
	public String name;
	/**
	 * 类型(class:班级,package:套餐)。
	 */
	public String type;
	/**
	 * 排序号。
	 */
	public Integer order_no;
	
	/**
	 * 是否为班级。
	 * @return
	 */
	public boolean IsClass(){
		return StringUtils.isNotBlank(this.type) && StringUtils.equalsIgnoreCase(this.type, TYPE_CLASS);
	}
	
//	private String pid,id,name,type;
//	private Integer orderNo;
//	/**
//	 * 获取上级课程ID。
//	 * @return 上级课程ID。
//	 */
//	public String getPid() {
//		return pid;
//	}
//	/**
//	 * 设置上级课程ID。
//	 * @param pid 
//	 *	  上级课程ID。
//	 */
//	public void setPid(String pid) {
//		this.pid = pid;
//	}
//	/**
//	 * 获取课程ID。
//	 * @return 课程ID。
//	 */
//	public String getId() {
//		return id;
//	}
//	/**
//	 * 设置课程ID。
//	 * @param id 
//	 *	  课程ID。
//	 */
//	public void setId(String id) {
//		this.id = id;
//	}
//	/**
//	 * 获取课程名称。
//	 * @return 课程名称。
//	 */
//	public String getName() {
//		return name;
//	}
//	/**
//	 * 设置课程名称。
//	 * @param name 
//	 *	  课程名称。
//	 */
//	public void setName(String name) {
//		this.name = name;
//	}
//	/**
//	 * 获取课程类型。
//	 * @return 课程类型。
//	 */
//	public String getType() {
//		return type;
//	}
//	/**
//	 * 设置课程类型。
//	 * @param type 
//	 *	  课程类型。
//	 */
//	public void setType(String type) {
//		this.type = type;
//	}
//	/**
//	 * 获取排序号。
//	 * @return 排序号。
//	 */
//	public Integer getOrderNo() {
//		return orderNo;
//	}
//	/**
//	 * 设置排序号。
//	 * @param orderNo 
//	 *	  排序号。
//	 */
//	public void setOrderNo(Integer orderNo) {
//		this.orderNo = orderNo;
//	}
	/*
	 * 排序比较。
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PackageClass o) {
		return this.order_no - o.order_no;
	}
}
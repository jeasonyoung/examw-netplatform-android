package com.examw.netschool.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * 套餐/班级。
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class PackageClass implements Serializable {
	private static final long serialVersionUID = 1L;
	private String pid, id,name,type;
	private Integer orderNo;
	/**
	 * 获取上级ID。
	 * @return 上级ID。
	 */
	public String getPid() {
		return pid;
	}
	/**
	 * 设置上级ID。
	 * @param pid 
	 *	  上级ID。
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}
	/**
	 * 获取套餐班级ID。
	 * @return 套餐班级ID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置套餐班级ID。
	 * @param id 
	 *	  套餐班级ID。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取套餐班级名称。
	 * @return 套餐班级名称。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置套餐班级名称。
	 * @param name 
	 *	  套餐班级名称。
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取套餐班级类型。
	 * @return 套餐班级类型。
	 */
	public String getType() {
		return type;
	}
	/**
	 * 是否为班级。
	 * @return
	 */
	public boolean IsClass(){
		return StringUtils.isNotBlank(this.type) && StringUtils.equalsIgnoreCase(this.type, "class");
	}
	/**
	 * 设置套餐班级类型。
	 * @param type 
	 *	  套餐班级类型。
	 */
	public void setType(String type) {
		this.type = type;
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
}
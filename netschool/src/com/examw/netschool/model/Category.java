package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 考试类别。
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class Category implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id,name,abbr;
	private Integer code;
	/**
	 * 获取考试类别ID。
	 * @return 考试类别ID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * 设置考试类别ID。
	 * @param id 
	 *	  考试类别ID。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取考试类别名称。
	 * @return 考试类别名称。
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置考试类别名称。
	 * @param name 
	 *	  考试类别名称。
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取考试类别简称。
	 * @return 考试类别简称。
	 */
	public String getAbbr() {
		return abbr;
	}
	/**
	 * 设置考试类别简称。
	 * @param abbr 
	 *	  考试类别简称。
	 */
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	/**
	 * 获取考试代码。
	 * @return 考试代码。
	 */
	public Integer getCode() {
		return code;
	}
	/**
	 * 设置考试代码。
	 * @param code 
	 *	  考试代码。
	 */
	public void setCode(Integer code) {
		this.code = code;
	}
}
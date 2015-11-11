package com.examw.netschool.model;

import java.io.Serializable;
/**
 * 考试数据。
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class Exam implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 考试ID。
	 */
	public String id;
	/**
	 * 考试名称。
	 */
	public String name;
	/**
	 * 考试简称。
	 */
	public String abbr;
	/**
	 * 排序号。
	 */
	public Integer order_no;
	
//	private String id,name,abbr;
//	private Integer code;
//	/**
//	 * 获取考试ID。
//	 * @return 考试ID。
//	 */
//	public String getId() {
//		return id;
//	}
//	/**
//	 * 设置考试ID。
//	 * @param id 
//	 *	  考试ID。
//	 */
//	public void setId(String id) {
//		this.id = id;
//	}
//	/**
//	 * 获取考试名称。
//	 * @return 考试名称。
//	 */
//	public String getName() {
//		return name;
//	}
//	/**
//	 * 设置考试名称。
//	 * @param name 
//	 *	  考试名称。
//	 */
//	public void setName(String name) {
//		this.name = name;
//	}
//	/**
//	 * 获取考试简称。
//	 * @return 考试简称。
//	 */
//	public String getAbbr() {
//		return abbr;
//	}
//	/**
//	 * 设置考试简称。
//	 * @param abbr 
//	 *	  考试简称。
//	 */
//	public void setAbbr(String abbr) {
//		this.abbr = abbr;
//	}
//	/**
//	 * 获取考试代码。
//	 * @return 考试代码。
//	 */
//	public Integer getCode() {
//		return code;
//	}
//	/**
//	 * 设置考试代码。
//	 * @param code 
//	 *	  考试代码。
//	 */
//	public void setCode(Integer code) {
//		this.code = code;
//	}
}
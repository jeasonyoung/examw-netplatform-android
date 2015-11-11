package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 反馈结果数据。
 * @param <T>
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class JSONCallback<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Boolean success;
	private T data;
	private String msg;
	
	/**
	 * 构造函数。
	 */
	public JSONCallback(){
		
	}
	
	/**
	 * 构造函数。
	 * @param success
	 * 是否成功。
	 * @param msg
	 * 反馈消息。
	 */
	public JSONCallback(boolean success, String msg){
		this.setSuccess(success);
		this.setMsg(msg);
	}
	
	/**
	 * 获取是否成功。
	 * @return 是否成功。
	 */
	public Boolean getSuccess() {
		return success;
	}
	/**
	 * 设置是否成功。
	 * @param success 
	 *	  是否成功。
	 */
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	/**
	 * 获取反馈数据。
	 * @return 反馈数据。
	 */
	public T getData() {
		return data;
	}
	/**
	 * 设置反馈数据。
	 * @param data 
	 *	  反馈数据。
	 */
	public void setData(T data) {
		this.data = data;
	}
	/**
	 * 获取反馈消息。
	 * @return 反馈消息。
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * 设置反馈消息。
	 * @param msg 
	 *	  反馈消息。
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
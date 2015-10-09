package com.examw.netschool.dao;

import com.examw.netschool.app.AppContext;

import android.content.Context;
import android.util.Log;

/**
 * 数据操作基类。
 * 
 * @author jeasonyoung
 * @since 2015年9月9日
 */
public abstract class BaseDao {
	private static final String TAG = "BaseDao";
	protected final MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 */
	public BaseDao(){
		Log.d(TAG, "构造函数...");
		final Context context = AppContext.getContext();
		if(context == null){
			Log.e(TAG, "初始化数据操作基础类上下文不存在!");
			throw new RuntimeException("上下文不存在!");
		}
		this.dbHelper = new MyDBHelper(context, AppContext.getCurrentUserId());
	}
}
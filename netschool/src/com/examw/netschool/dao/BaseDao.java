package com.examw.netschool.dao;

import android.content.Context;
import android.util.Log;

/**
 * 数据操作基类。
 * 
 * @author jeasonyoung
 * @since 2015年9月9日
 */
public class BaseDao {
	private static final String TAG = "BaseDao";
	protected final MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 */
	public BaseDao(Context context, String userId){
		Log.d(TAG, "构造函数...");
		this.dbHelper = new MyDBHelper(context, userId);
	}
	/**
	 * 构造函数。
	 * @param dao
	 */
	public BaseDao(BaseDao dao){
		Log.d(TAG, "构造函数...");
		this.dbHelper = (dao == null) ?  null : dao.dbHelper;
	}
}
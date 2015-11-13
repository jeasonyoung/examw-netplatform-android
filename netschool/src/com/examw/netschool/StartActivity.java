package com.examw.netschool;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * APP 启动Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class StartActivity extends Activity{
	private static final String TAG = "StartActivity";
	private static final int THREAD_WAIT = 800;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "初始化...");
		//设置布局文件
		this.setContentView(R.layout.activity_start);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "重载启动...");
		//异步加载。
		new AsyncTask<Void, Void, Boolean>() {
			/*
			 * 重载后台线程加载数据。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected Boolean doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载数据...");
					//线程等待
					Thread.sleep(THREAD_WAIT);
					//初始化
					final AppContext appContext = (AppContext)getApplicationContext();
					//获取引导定义
					final SharedPreferences guidefile = getSharedPreferences(Constant.PREFERENCES_CONFIG_GUIDEFILE, Context.MODE_PRIVATE);
					if(appContext != null && guidefile != null){
						//获取版本代码
						final int versionCode = appContext.getVersionCode();
						//是否为第一次。
						return guidefile.getBoolean(Constant.PREFERENCES_CONFIG_GUIDEFILE_ISFIRST + versionCode, false);
					}
				}catch(Exception e){
					Log.d(TAG, "线程后台处理异常:" + e.getMessage());
				}
				return false;
			}
			/*
			 * 前台主线程更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Boolean result) {
				Log.d(TAG, "主线程处理...");
//				if(result){
					Log.d(TAG, "登录...");
					gotoLogin();
//				}else{
//					Log.d(TAG, "引导...");
//					gotoGuide();
//				}
			}
		}.execute((Void)null);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "重启...");
		//跳转到登录
		this.gotoLogin();
	}
	//登录
	private void gotoLogin() {
		Log.d(TAG, "调转到登录...");
		//启动登录
		this.startActivity(new Intent(this, LoginActivity.class));
		//关闭当前
		this.finish();
	}
//	//引导页
//	private void gotoGuide() {
//		Log.d(TAG, "调整引导页...");
//		//启动引导
//		this.startActivity(new Intent(this, GuideActivity.class));
//		//关闭当前
//		this.finish();
//	}
}
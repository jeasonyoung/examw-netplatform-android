package com.examw.netschool;

import java.lang.ref.WeakReference;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * APP 启动Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class StartActivity extends Activity{
	private static final String TAG = "StartActivity";
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "初始化...");
		//设置布局文件
		this.setContentView(R.layout.activity_start);
		//获取引导定义
		final SharedPreferences guidefile = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_GUIDEFILE, Context.MODE_PRIVATE);
		//初始化
		final AppContext appContext = (AppContext)this.getApplicationContext();
		if(appContext != null && guidefile != null){
			//获取版本代码
			final int versionCode = appContext.getVersionCode();
			//是否为第一次。
			final boolean isfirst = guidefile.getBoolean(Constant.PREFERENCES_CONFIG_GUIDEFILE_ISFIRST + versionCode, false);
			//初始化消息处理
			final Handler handler = new MyHandler(this);
			if(isfirst){//第一次
				handler.sendEmptyMessageDelayed(1, 2000);
			}else {
				handler.sendEmptyMessageDelayed(2, 2000);
			}
		}
		//
		super.onCreate(savedInstanceState);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		Log.d(TAG, "重启...");
		//跳转到登录
		this.gotoLogin();
		//
		super.onRestart();
	}
	//登录
	private void gotoLogin() {
		Log.d(TAG, "调转到登录...");
		//启动登录
		this.startActivity(new Intent(this, LoginActivity.class));
		//关闭当前
		this.finish();
	}
	//引导页
	private void gotoGuide() {
		Log.d(TAG, "调整引导页...");
		//启动引导
		this.startActivity(new Intent(this, GuideActivity.class));
		//关闭当前
		this.finish();
	}
	//消息处理。
	private static class MyHandler extends Handler {
		private static final String TAG = "MyHandler";
		private WeakReference<StartActivity> mActivity;
		/**
		 * 构造函数。
		 * @param activity
		 */
		public MyHandler(StartActivity activity) {
			Log.d(TAG, "初始化...");
			this.mActivity = new WeakReference<StartActivity>(activity);
		}
		/*
		 * 消息处理。
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			final StartActivity login = mActivity.get();
			if(login == null) return;
			Log.d(TAG, "消息处理...");
			switch(msg.what)
			{
				case -1:
				case 1:{
					Log.d(TAG, "登录...");
					login.gotoLogin();
					break;
				}
				case 2:{
					Log.d(TAG, "引导...");
					login.gotoGuide();
					break;
				}
			}
		}
	}
}
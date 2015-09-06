package com.examw.netschool;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;

/**
 * APP 启动Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class StartActivity extends Activity{
	private static final String TAG = "StartActivity";
	
	private AlertDialog alertDialog;
	private ProgressDialog progressDialog;
	private AppContext appContext;
	
	/*
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "初始化...");
		
		this.setContentView(R.layout.activity_start);
		
		this.appContext = (AppContext)this.getApplication();
		final int versionCode = appContext.getVersionCode();
		
		final SharedPreferences guidefile = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_GUIDEFILE, Context.MODE_PRIVATE);
		final boolean isfirst = guidefile.getBoolean(Constant.PREFERENCES_CONFIG_GUIDEFILE_ISFIRST + versionCode, false);
		final Handler handler = new MyHandler(this);
		if(isfirst){
			handler.sendEmptyMessageDelayed(1, 2000);
		}else {
			handler.sendEmptyMessageDelayed(2, 2000);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "重启...");
		this.gotoLogin();
	}
	
	//登录
	private void gotoLogin() {
		Log.d(TAG, "调转到登录...");
		this.startActivity(new Intent(this, LoginActivity.class));
		this.finish();
	}
	
	//引导页
	private void gotoGuide() {
		Log.d(TAG, "调整引导页...");
		this.startActivity(new Intent(this, GuideActivity.class));
		this.finish();
	}
	/*
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "销毁...");
		if(this.alertDialog != null) this. alertDialog.dismiss(); 
		if(this.progressDialog != null) this.progressDialog.dismiss();
	}
	/**
	 * 
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月2日
	 */
	private static class MyHandler extends Handler {
		private WeakReference<StartActivity> mActivity;
		
		public MyHandler(StartActivity activity) {
			this.mActivity = new WeakReference<StartActivity>(activity);
		 }
		
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
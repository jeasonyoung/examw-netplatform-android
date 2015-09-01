package com.examw.netschool;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.examw.netschool.app.AppContext;

public class StartActivity extends Activity{
	private AlertDialog alertDialog = null;
	private ProgressDialog progressDialog = null;
	private SharedPreferences settingfile;
	private Handler handler;
	private AppContext appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//com.umeng.common.Log.LOG = true;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_start);
		
		final SharedPreferences guidefile = getSharedPreferences("guidefile", 0);
		this.settingfile = this.getSharedPreferences("settingfile", 0);
		
		appContext = (AppContext) getApplication();
		final int versionCode = appContext.getVersionCode();
		
		this.initSetting();
		
		final int isfirst = guidefile.getInt("isfirst"+versionCode, 0);
		handler = new MyHandler(this);
		if(isfirst == 1){
			handler.sendEmptyMessageDelayed(1, 2000);
		}else {
			handler.sendEmptyMessageDelayed(2, 2000);
		}
	}
	
	private void initSetting() {
		if(this.settingfile.contains("IsFirst")) return;
		SharedPreferences.Editor editor = this.settingfile.edit();
		editor.putString("IsFirst", "No")
				 .putBoolean("setDownIsUse3G", true)
				 .putString("setDownfilepath", getString(R.string.Downfilepath))
				 .putBoolean("setDownfiletype", true)
				 .putBoolean("setPlayIsUse3G", true)
				 .putBoolean("setPlayfiletype", true)
				 .putInt("setCheckUpdateMode",0)
				 .putLong("lastCheckUpdateTime", 0)
				 .commit();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		gotoLogin();
	}
	
	private void gotoLogin()
	{
		Intent intent = new Intent();
		intent.setClass(StartActivity.this, LoginActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}
	
	private void gotoGuide()
	{
		Intent intent = new Intent();
		intent.setClass(this, GuideActivity.class);
		this.startActivity(intent);
		this.finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(alertDialog!=null) alertDialog.dismiss(); 
		if(progressDialog!=null) progressDialog.dismiss();
	}
	
	static class MyHandler extends Handler
	{
		WeakReference<StartActivity> mActivity;
		public MyHandler(StartActivity activity) {
			mActivity = new WeakReference<StartActivity>(activity);
		 }
		@Override
		public void handleMessage(Message msg) {
			StartActivity login = mActivity.get();
			switch(msg.what)
			{
				case 1:
					login.gotoLogin();
					break;
				case -1:
					login.gotoLogin();
					break;
				case 2:
					login.gotoGuide();
					break;
			}
		}
	}
	
}
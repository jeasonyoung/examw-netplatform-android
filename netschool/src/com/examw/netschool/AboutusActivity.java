package com.examw.netschool;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutusActivity extends Activity{
	private ImageButton returnBtn;
	private TextView version;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_about);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		this.version = (TextView) this.findViewById(R.id.versionstext);
		PackageInfo localPackageInfo;
		try {
			localPackageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
			this.version.setText("版本号：V" + localPackageInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		//MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//MobclickAgent.onResume(this);
		
	}
}

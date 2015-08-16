package com.examw.netschool;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
/**
 * 下载类。
 * @author jeasonyoung
 *
 */
@SuppressWarnings("deprecation")
public class DownloadActivity extends ActivityGroup implements OnClickListener {
	private ImageButton returnBtn;
	private RadioButton downingBtn,finishBtn;
	private LinearLayout container;
	private int flag;
	private String username;
	private Button delBtn;
	/*
	 * 重载创建。
	 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_down);
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		String url = intent.getStringExtra("url");
		String action = intent.getStringExtra("actionName");
		this.username = intent.getStringExtra("username");
		initLayout();
		container.removeAllViews();
		if("outline".equals(action))
		{
			finishBtn.setChecked(true);
			turnToDownFinishActivity();
			flag=1;
		}else
		{
		Intent mIntent = new Intent(this, DowningActivity.class);
		mIntent.putExtra("name", name);
		mIntent.putExtra("url", url);
		mIntent.putExtra("username", username);
        container.addView(getLocalActivityManager().startActivity(
                "DowningActivity",mIntent               
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());
		}
	}
	private void initLayout()
	{
		this.container= (LinearLayout) this.findViewById(R.id.containerBody);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.delBtn = (Button) this.findViewById(R.id.delbtn);
		this.downingBtn = (RadioButton) this.findViewById(R.id.downingBtn);
		this.finishBtn =(RadioButton) this.findViewById(R.id.downfinishBtn);
		this.delBtn.setOnClickListener(this);
		this.returnBtn.setOnClickListener(this);
		this.downingBtn.setOnClickListener(this);
		this.finishBtn.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.returnbtn:
			this.finish();
			break;
		case R.id.delbtn:
			Toast.makeText(this, "长按列表项以删除", Toast.LENGTH_SHORT).show();
			break;
		case R.id.downingBtn:
			if(flag==1)
			{
				turnToDowningActivity();
				flag=0;
			}
			break;
		case R.id.downfinishBtn:
			if(flag==0)
			{
				turnToDownFinishActivity();
				flag=1;
			}
			break;
		}
	}
	private void turnToDowningActivity() {
		container.removeAllViews();
		Intent mIntent = new Intent(DownloadActivity.this, DowningActivity.class);
		mIntent.putExtra("username", username);
        container.addView(getLocalActivityManager().startActivity(
                "DowningActivity",mIntent               
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());
	}
	private void turnToDownFinishActivity() {
		container.removeAllViews();
		Intent mIntent = new Intent(DownloadActivity.this, DownFinishActivity.class);
		mIntent.putExtra("username", username);
        container.addView(getLocalActivityManager().startActivity(
                "DownFinishActivity",mIntent               
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());
	}
}
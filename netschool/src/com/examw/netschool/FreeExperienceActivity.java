package com.examw.netschool;

import com.examw.netschool.app.Constant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * 免费体验Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月17日
 */
public class FreeExperienceActivity extends FragmentActivity implements OnClickListener{
	private static final String TAG = "FreeExperienceActivity";
	private String userId,userName;
	private Search search;
	/*
	 * 重载创建。
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "初始化...");
		super.onCreate(savedInstanceState);
		//加载布局XML
		this.setContentView(R.layout.activity_free_experience);
		//
		final Intent intent = this.getIntent();
		if(intent != null){
			//用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//用户名称
			this.userName = intent.getStringExtra(Constant.CONST_USERNAME);
		}
		//返回按钮
		final View btnReturn = this.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(this);
		//搜索
		this.search = new Search(this);
		//课程中心
		final View btnCenter = this.findViewById(R.id.btn_lesson_center);
		btnCenter.setOnClickListener(this);
		//我的课程
		final View btnMy = this.findViewById(R.id.btn_lesson_my);
		btnMy.setOnClickListener(this);
		//播放记录
		final View btnRecord = this.findViewById(R.id.btn_lesson_record);
		btnRecord.setOnClickListener(this);
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//添加Fragment
		this.getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.fragment_container, new FreeExperienceFragmentByCategory(this.userId, this.search))
			.commit();
		super.onStart();
	}
	/*
	 * 点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "点击事件处理..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回
				this.finish();
				break;
			}
			case R.id.btn_lesson_center:{//课程中心
				this.getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, new FreeExperienceFragmentByCategory(this.userId, this.search))
				.commit();
				break;
			}
			case R.id.btn_lesson_my:{//我的课程
				 final Intent intent = new Intent(this, MyCourseActivity.class);
				 intent.putExtra(Constant.CONST_USERID, userId);
				 intent.putExtra(Constant.CONST_USERNAME, userName);
				 this.startActivity(intent);
				break;
			}
			case R.id.btn_lesson_record:{//播放记录
				 final Intent intent = new Intent(this, PlayRecordActivity.class);
				 intent.putExtra(Constant.CONST_USERID, userId);
				 intent.putExtra(Constant.CONST_USERNAME, userName);
				 this.startActivity(intent);
				break;
			}
		}
	}
	//搜索数据
	public class Search{
		private EditText etSearch;
		private ImageButton btnSearch;
		/**
		 * 构造函数。
		 * @param view
		 */
		public Search(Activity view){
			//搜索文本框
			this.etSearch = (EditText)view.findViewById(R.id.searchKey);
			//搜素按钮
			this.btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
		}
		/**
		 * 获取搜索关键字。
		 * @return
		 */
		public String getSearchKey(){
			return this.etSearch.getText().toString();
		}
		/**
		 * 设置查询事件响应。
		 * @param onClickListener
		 */
		public void setOnClickListener(OnClickListener onClickListener){
			this.btnSearch.setOnClickListener(onClickListener);
		}
	}
}
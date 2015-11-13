package com.examw.netschool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 免费体验Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月17日
 */
public class FreeExperienceActivity extends FragmentActivity implements OnClickListener{
	private static final String TAG = "FreeExperienceActivity";
	private Search search;
	/*
	 * 重载创建。
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "初始化...");
		//加载布局XML
		this.setContentView(R.layout.activity_free_experience);
		//返回按钮
		final View btnReturn = this.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(this);
		//标题
		final TextView tvTopTitle = (TextView)this.findViewById(R.id.top_title);
		tvTopTitle.setText(R.string.free_experience_title);
		
		//搜索
		this.search = new Search(this);
		//课程中心
		final View btnCenter = this.findViewById(R.id.btn_free_experience);
		btnCenter.setOnClickListener(this);
		//我的课程
		final View btnMy = this.findViewById(R.id.btn_my_course);
		btnMy.setOnClickListener(this);
		//播放记录
		final View btnRecord = this.findViewById(R.id.btn_play_record);
		btnRecord.setOnClickListener(this);
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "重载启动...");
		//
		this.loadFirstFragment();
	}
	//加载第一个Fragment
	private void loadFirstFragment(){
		this.getSupportFragmentManager().beginTransaction()
		.addToBackStack(null)
		.add(R.id.fragment_container, new FreeExperienceFragmentByExams(this.search))
		.commit();
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
				if(this.getSupportFragmentManager().getBackStackEntryCount() > 1){
					this.getSupportFragmentManager().popBackStack();
				}else{
					this.finish();
				}
				break;
			}
			case R.id.btn_free_experience:{//课程中心
				//加载Frist
				this.loadFirstFragment();
				break;
			}
			case R.id.btn_my_course:{//我的课程
				 this.startActivity(new Intent(this, MyCourseActivity.class));
				 //关闭当前
				 this.finish();
				break;
			}
			case R.id.btn_play_record:{//播放记录
				 this.startActivity(new Intent(this, PlayRecordActivity.class));
				 //关闭当前
				 this.finish();
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
			this.etSearch = (EditText)view.findViewById(R.id.search_key);
			//搜素按钮
			this.btnSearch = (ImageButton)view.findViewById(R.id.btn_search);
		}
		/**
		 * 获取搜索关键字。
		 * @return
		 */
		public String getSearchKey(){
			return this.etSearch.getText().toString();
		}
		/**
		 * 清空搜索关键字
		 */
		public void clean(){
			this.etSearch.setText(null);
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
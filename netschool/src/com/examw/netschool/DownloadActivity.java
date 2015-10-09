package com.examw.netschool;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Lesson;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
/**
 * 下载Activity。
 * @author jeasonyoung
 *
 */
public class DownloadActivity extends FragmentActivity implements OnCheckedChangeListener {
	private static final String TAG = "DownloadActivity";
	private String userId,lessonId;
	
	private int index = 0;
	private ViewPager viewPager;
	private RadioGroup radioGroup;
	/**
	 * 索引参数键。
	 */
	public static final String CONST_FRAGMENT_INDEX = "page_index";
	/**
	 * 下载课程UI。
	 */
	public static final int CONST_FRAGMENT_DOWNING = 0;
	/**
	 * 离线课程UI。
	 */
	public static final int CONST_FRAGMENT_FINISH = 1;
	/*
	 * 重载创建
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建....");
		//设置布局文件 
		this.setContentView(R.layout.activity_download);
		
		//加载传递数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//当前用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//课程资源ID
			this.lessonId = intent.getStringExtra(Constant.CONST_LESSON_ID);
			//页面索引
			index = intent.getIntExtra(CONST_FRAGMENT_INDEX, CONST_FRAGMENT_DOWNING);
		}
		
		//返回按钮
		final View btnBack = this.findViewById(R.id.btn_return);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "返回事件处理...");
				finish();
			}
		});
		//顶部标题
		final TextView tvTitle = (TextView)this.findViewById(R.id.top_title);
		if(tvTitle != null){
			tvTitle.setText(R.string.download_top_title);
		}
		
		//分组选项
		this.radioGroup = (RadioGroup)this.findViewById(R.id.down_radio_group);
		radioGroup.setOnCheckedChangeListener(this);
		//ViewPager
		this.viewPager = (ViewPager)this.findViewById(R.id.download_pagers);
		//设置数据适配器
		this.viewPager.setAdapter(this.mAdapter);
		//
		super.onCreate(savedInstanceState);
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//异步加载数据
		new AsyncTask<Void, Void, Void>() {
			/*
			 * 后台线程处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected Void doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载下载数据...");
					//检查课程资源ID
					if(StringUtils.isBlank(lessonId)) return null;
					//初始化
					final DownloadDao downloadDao = new DownloadDao();
					//检查是否存在
					if(!downloadDao.hasDownload(lessonId)){
						//初始化
						final LessonDao lessonDao = new LessonDao();
						//查询课程信息
						final Lesson lesson = lessonDao.getLesson(lessonId);
						if(lesson != null){
							//添加到下载
							downloadDao.add(new Download(lesson)); 
						}
					}
				}catch(Exception e){
					Log.e(TAG, "后台线程加载下载数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前端主线程更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Void result) {
				 Log.d(TAG, "前端主线程更新数据...");
				 //通知数据适配器更新数据
				 mAdapter.notifyDataSetChanged();
				 //
				 final int pageIndex = Math.min(index, mAdapter.getCount() - 1);
				 radioGroup.check(pageIndex == CONST_FRAGMENT_DOWNING ?  R.id.btn_downing : R.id.btn_finish);
			}
		}.execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 选项卡事件处理。
	 * @see android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(android.widget.RadioGroup, int)
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		Log.d(TAG, "选项卡事件处理..." + checkedId);
		switch(checkedId){
			case R.id.btn_downing:{//下载中
				Log.d(TAG, "下载中...");
				this.viewPager.setCurrentItem(0);
				break;
			}
			case R.id.btn_finish:{//下载完成
				Log.d(TAG, "下载完成...");
				this.viewPager.setCurrentItem(1);
				break;
			}
		}
	}
	//数据适配器。
	private FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
		/*
		 * 获取数据总数。
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			Log.d(TAG, "加载适配器数据总数...");
			return 2;
		}
		/*
		 * 获取Fragment.
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int pos) {
			Log.d(TAG, "加载Fragment...." + pos);
			Fragment result = null;
			switch(pos){
				 case 0:{//下载中
					 result = new DownloadByFragmentDowning(userId);
					 break;
				 }
				 case 1:{//下载完成
					 result = new DownloadByFragmentFinish(userId);
					 break;
				 }
			}
			return result;
		}
	};
}
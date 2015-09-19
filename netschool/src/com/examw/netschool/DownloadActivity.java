package com.examw.netschool;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.service.DownloadService;
import com.examw.netschool.service.IDownloadService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
/**
 * 下载Activity。
 * @author jeasonyoung
 *
 */
public class DownloadActivity extends FragmentActivity implements OnCheckedChangeListener, ServiceConnection{
	private static final String TAG = "DownloadActivity";
	private String userId,lessonId;
	private ViewPager viewPager;
	private IDownloadService downloadService;
	
	private DownloadDao dao;
	/*
	 * 重载创建
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建....");
		super.onCreate(savedInstanceState);
		
		//绑定文件下载服务
		this.getApplicationContext().bindService(new Intent(this, DownloadService.class), this, Context.BIND_AUTO_CREATE);
		
		//设置内容XML 
		this.setContentView(R.layout.activity_download);
		
		//加载传递数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//当前用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//课程资源ID
			this.lessonId = intent.getStringExtra(Constant.CONST_LESSON_ID);
		}
		//返回按钮
		final View btnBack = this.findViewById(R.id.btnReturn);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "返回事件处理...");
				finish();
			}
		});
		//分组选项
		final RadioGroup radioGroup = (RadioGroup)this.findViewById(R.id.downRadioGroup);
		radioGroup.setOnCheckedChangeListener(this);
		//ViewPager
		this.viewPager = (ViewPager)this.findViewById(R.id.downloadPagers);
		//设置数据适配器
		this.viewPager.setAdapter(this.mAdapter);
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//异步加载数据
		new AsyncLoadData().execute((Void)null);
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
			case R.id.btnDowning:{//下载中
				Log.d(TAG, "下载中...");
				this.viewPager.setCurrentItem(0);
				break;
			}
			case R.id.btnDownFinish:{//下载完成
				Log.d(TAG, "下载完成...");
				this.viewPager.setCurrentItem(1);
				break;
			}
		}
	}
	/*
	 * 连接下载服务。
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "连接下载服务["+name+"]..." + service);
		this.downloadService = (IDownloadService)service;
	}
	/*
	 * 断开下载服务。
	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "断开下载服务...");
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
					 result = new DownloadByFragmentDowning(userId, downloadService);
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
	//异步加载数据。
	private class AsyncLoadData extends AsyncTask<Void, Void, Void>{
		/*
		 * 后台线程处理数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程处理数据...");
				if(StringUtils.isBlank(lessonId)) return null;
				//惰性加载数据操作
				if(dao == null){
					Log.d(TAG, "惰性加载数据操作");
					dao = new DownloadDao(getApplicationContext(), userId);
				}
				//检查是否存在
				if(dao.hasDownload(lessonId)){
					Log.d(TAG, "课程资源["+lessonId+"]已在下载中...");
					return null;
				}
				//查询课程信息
				final LessonDao lessonDao = new LessonDao(dao);
				final Lesson lesson = lessonDao.getLesson(lessonId);
				if(lesson == null){
					Log.d(TAG, "课程["+lessonId+"]不存在!");
					return null;
				}
				//添加到下载
				final Download download = new Download(lesson);
				dao.add(download); 
				//启动下载
				if(downloadService != null){
					Log.d(TAG, "启动下载后台服务...");
					downloadService.addDownload(download);
				}
			}catch(Exception e){
				Log.e(TAG, "异步线程异常:" + e.getMessage(), e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}
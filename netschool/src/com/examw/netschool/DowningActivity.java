package com.examw.netschool;

import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.examw.netschool.adapter.DowningListAdapter;
import com.examw.netschool.dao.CourseDao;
import com.examw.netschool.entity.DowningCourse;
import com.examw.netschool.service.DownloadService;
import com.examw.netschool.util.StringUtils;
import com.umeng.analytics.MobclickAgent;

/**
 *  视频下载UI。
 * @author jeasonyoung
 *
 */
public class DowningActivity extends BaseActivity{
	private static final String TAG = "DowningActivity";
	private ListView listView;
	private LinearLayout nodata;
	private DowningListAdapter mAdapter;
	private DownloadServiceConnection serviceConnection = new DownloadServiceConnection();
	private DownloadService.IFileDownloadService fileDownloadService;
	private CourseDao courseDao = new CourseDao(this);
	private List<DowningCourse> dataSource;
	private String username;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//绑定文件下载服务
		this.getApplicationContext().bindService(new Intent(this, DownloadService.class), this.serviceConnection, BIND_AUTO_CREATE);
		//加载布局
		this.setContentView(R.layout.activity_downing);
		this.listView = (ListView) this.findViewById(R.id.videoListView);
		//长按弹出取消下载的PopupWindow
		this.listView.setOnItemLongClickListener(new OnItemLongClickListener());
		this.nodata = (LinearLayout) this.findViewById(R.id.down_nodataLayout);
		
		Intent intent = this.getIntent();
		this.username = intent.getStringExtra("username");
		String name =  intent.getStringExtra("name");
		String url = intent.getStringExtra("url");
		//加载数据
		this.dataSource = this.courseDao.findAllDowning(this.username);
		//初始化从课程列表中点击的要下载项
		if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(url))
		{
			DowningCourse downing = new DowningCourse();
			downing.setCourseName(name);
			downing.setFileUrl(url);
			downing.setState(DowningCourse.STATE_INIT);
			downing.setUserName(this.username);
			if(!this.dataSource.contains(downing)){
				Log.d(TAG, "添加须下载课程["+name+"=>"+url+"]到集合...");
				this.dataSource.add(downing);
				//添加到数据库
				this.courseDao.updateState(this.username, downing.getFileUrl(), DowningCourse.STATE_DOWNING);
			}
		}
		this.mAdapter = new DowningListAdapter(getApplicationContext(), this.dataSource);
		listView.setAdapter(mAdapter);
		if(this.dataSource.size() == 0){
			nodata.setVisibility(View.VISIBLE);
		}
	}
	/*
	 * 重载恢复。
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	/*
	 * 重载暂停。
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	
	@Override
	protected void onDestroy() {
		//取消绑定服务
		this.getApplicationContext().unbindService(this.serviceConnection);
		super.onDestroy();
	}
	/**
	 * 下载服务连接器。
	 * @author jeasonyoung
	 *
	 */
	private final class DownloadServiceConnection implements ServiceConnection{
		/*
		 * 连接服务。
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "连接下载服务...");
			fileDownloadService = (DownloadService.IFileDownloadService)service;
		}
		/*
		 * 断开服务。
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "断开下载服务...");
			 fileDownloadService = null;
		}
	}
	/**
	 * 长按取消下载事件处理。
	 * @author jeasonyoung
	 *
	 */
	private class OnItemLongClickListener implements AdapterView.OnItemLongClickListener, View.OnClickListener,DialogInterface.OnClickListener{
		private QuickActionPopupWindow actionbar;
		private ActionItem actionDelete;
		private AlertDialog dialog;
		private DowningCourse course;
		 /*
		  * 重载长按处理。
		  * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
		  */
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if(position > -1 && position < mAdapter.getCount()){
				this.course = (DowningCourse)mAdapter.getItem(position);
				this.showPopupWindow(view);
			}
			return false;
		}
		//显示弹出框
		private void showPopupWindow(View v){
			this.actionbar = new QuickActionPopupWindow(DowningActivity.this);
			this.actionDelete = new ActionItem();
			this.actionDelete.setIcon(getResources().getDrawable(R.drawable.action_delete));
			this.actionDelete.setTitle("取消");
			this.actionDelete.setClickListener(this);
			this.actionbar.addActionItem(this.actionDelete);
			//设置动画风格
			this.actionbar.setAnimStyle(QuickActionPopupWindow.ANIM_AUTO);
			//显示
			this.actionbar.show(v);
		}
		/*
		 * 取消事件处理。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			 //弹框是否确认删除
			this.dialog = new AlertDialog.Builder(DowningActivity.this)
			.setTitle("删除文件")
			.setMessage("是否确认取消下载并删除文件?")
			.setPositiveButton("确定",this)
			.setNegativeButton("取消", this).create();
			//显示
			this.dialog.show();
		}
		/*
		 * 弹框按钮事件处理
		 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
		 */
		@Override
		public void onClick(DialogInterface dialog, int which) {
			 switch(which){
				 case DialogInterface.BUTTON_POSITIVE:{//确认
					 dialog.cancel();
					 this.actionbar.dismiss();
					 //发送广播意图：停止下载,并删除文件
					 if(fileDownloadService != null){
						 Log.d(TAG, "课程["+this.course.getCourseName()+"]取消下载...");
						 fileDownloadService.cancelDownload(this.course);
					 }
					 //删除数据库记录
					 courseDao.deleteDowing(username, this.course.getFileUrl());
					 //从数据集合中移除
					 dataSource.remove(this.course);
					 //更新UI
					 mAdapter.notifyDataSetChanged();
					 break;
				 }
				 case DialogInterface.BUTTON_NEGATIVE:{//取消
					 dialog.cancel();
					 this.actionbar.dismiss();
					 break;
				 }
			 }
		}
	}
}
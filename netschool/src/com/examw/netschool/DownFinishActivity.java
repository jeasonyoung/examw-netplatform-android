package com.examw.netschool;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.examw.netschool.adapter.DownedListAdapter;
import com.examw.netschool.entity.Course;
/**
 * 下载完成。
 * @author jeasonyoung
 *
 */
public class DownFinishActivity extends BaseActivity {
	private ListView listview;
	//private CourseDao dao;
	private List<Course> list;
	private LinearLayout nodata;
	private BaseAdapter mAdapter;
	private QuickActionPopupWindow actionbar;
	private ActionItem action_delete;
	private ActionButtonClickListener listener;
	private String username;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_downfinish);
		this.listview = (ListView) this.findViewById(R.id.videoListView);
		this.nodata = (LinearLayout) this.findViewById(R.id.down_nodataLayout);
		this.username = getIntent().getStringExtra("username");
		
//		if (this.dao == null)this.dao = new CourseDao(this);
//		this.list = this.dao.findAllDowned(username);
		if (this.list.size() == 0) {
			this.nodata.setVisibility(View.VISIBLE);
		}
		this.mAdapter = new DownedListAdapter(this, this.list);
		
		this.listview.setAdapter(this.mAdapter);
		this.listview.setOnItemClickListener(new ItemClickListener());
		this.listview.setOnItemLongClickListener(new ItemLongClickListener());
	}
	/**
	 * 选项点击事件监听器。
	 * @author jeasonyoung
	 *
	 */
	private class ItemClickListener implements OnItemClickListener {
		/*
		 * 重载点击事件。
		 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			System.out.print("ItemClickListener 播放");
			//
			//MobclickAgent.onEvent(DownFinishActivity.this,"download_listen");
			//
			Course c = list.get(arg2);
			Intent intent = new Intent(DownFinishActivity.this, VideoPlayActivity.class);
			intent.putExtra("name", c.getCourseName());
			intent.putExtra("url", c.getFilePath());
			intent.putExtra("courseid", c.getCourseId());
			intent.putExtra("username", username);
			DownFinishActivity.this.startActivity(intent);
		}
	}
	/**
	 * 选项事件监听。
	 * @author jeasonyoung
	 *
	 */
	private class ItemLongClickListener implements OnItemLongClickListener {
		/*
		 * 重载点击事件。
		 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			this.showWindow(arg1,arg2);
			return false;
		}
		//
		private void showWindow(View v, int location) {
			if(actionbar == null)
			{
				actionbar = new QuickActionPopupWindow(DownFinishActivity.this);
				action_delete = new ActionItem();
				action_delete.setTitle("删除");
				action_delete.setIcon(getResources().getDrawable(R.drawable.action_delete));
				
				actionbar.addActionItem(action_delete);
				// 设置动画风格
				actionbar.setAnimStyle(QuickActionPopupWindow.ANIM_AUTO);
			}
			if(listener == null){
				listener = new ActionButtonClickListener();
			}
			listener.setIndex(location);
			action_delete.setClickListener(listener); 
			// 显示
			actionbar.show(v);
		}
	}
	/**
	 * 按钮点击事件。
	 * @author jeasonyoung
	 *
	 */
	private class ActionButtonClickListener implements OnClickListener
	{
		private int index;
		/**
		 * 设置索引。
		 * @param index
		 * 索引值。
		 */
		public void setIndex(int index) {
			this.index = index;
		}
		/*
		 * 重载点击事件。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			//弹框是否确认删除
			AlertDialog dialog = new AlertDialog.Builder(DownFinishActivity.this)
			.setTitle("删除文件")
			.setMessage("是否确认删除该视频文件")
			.setPositiveButton("确定", new  DialogInterface.OnClickListener() {
				/*
				 * 重载点击事件。
				 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					actionbar.dismiss();
					
					Course c = list.get(index);
					new File(c.getFilePath()).delete();
					//Log.i("DownFinish","删除了文件");
					//dao.updateState(c.getUserName(), c.getFileUrl(), 0);
					list.remove(index);
					mAdapter.notifyDataSetChanged();
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create();
			dialog.show();
		}
	}
}
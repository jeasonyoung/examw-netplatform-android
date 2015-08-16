package com.examw.netschool;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.dao.PlayrecordDao;
import com.examw.netschool.entity.Playrecord;
import com.umeng.analytics.MobclickAgent;
/**
 * 播放记录类
 * @author jeasonyoung
 *
 */
public class PlayrecordActivity extends ListActivity{
	private ImageButton returnBtn;
	private ArrayList<Playrecord> recordList;
	private String username;
	private String loginType;
	private PlayrecordDao dao = new PlayrecordDao(this);
	private RecordListAdapter mAdapter;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_playrecord);
		Intent intent = this.getIntent();
		this.username = intent.getStringExtra("username");
		this.loginType = intent.getStringExtra("loginType");
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
	}
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		if(recordList==null){
			this.recordList = (ArrayList<Playrecord>) this.dao.getRecordList(username);
		}else{
			this.recordList.clear();
			this.recordList.addAll(this.dao.getRecordList(username));
		}
		if(this.mAdapter==null){
			this.mAdapter = new RecordListAdapter();
			this.setListAdapter(mAdapter);
		}else{
			this.mAdapter.notifyDataSetChanged();
		}
		super.onStart();
	}
	/*
	 * 重载点击事件。
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Playrecord r = this.recordList.get(position);
		if("local".equals(loginType))
		{
			if(r.getCourseFilePath()==null)
			{
				Toast.makeText(this, "您没有下载该视频", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		//
		MobclickAgent.onEvent(this,"record_listen");
		//
		Intent intent = new Intent(this,VideoPlayActivity.class);
		intent.putExtra("username", username);
		intent.putExtra("name", r.getCourseName());
		intent.putExtra("url", r.getCourseFilePath()==null?r.getCourseUrl():r.getCourseFilePath());
		intent.putExtra("httpUrl", r.getCourseUrl());
		intent.putExtra("loginType", loginType);
		intent.putExtra("courseid", r.getCourseId());
		this.startActivity(intent);	
	}
	/**
	 * 
	 * @author jeasonyoung
	 *
	 */
	private class RecordListAdapter extends BaseAdapter
	{
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			try{
				return recordList.size();
			}catch(Exception e) {
				return 0;
			}
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			try{
				return recordList.get(position);
			}catch(Exception e){
				return null;
			}
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@SuppressLint("ViewHolder") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater mInflater = LayoutInflater.from(PlayrecordActivity.this);
			Playrecord  r = recordList.get(position);
			convertView = mInflater.inflate(R.layout.list_playrecord, null);
			TextView coursename = (TextView) convertView.findViewById(R.id.coursenamelab);
			TextView currentTime = (TextView) convertView.findViewById(R.id.currentTimeLab);
			TextView playTime = (TextView) convertView.findViewById(R.id.palytimelab);
			coursename.setText(r.getCourseName());
			currentTime.setText("已学习到:"+r.getFormatCurrentTime());
			playTime.setText("学习时间:"+r.getPlayTime());
			return convertView;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
}
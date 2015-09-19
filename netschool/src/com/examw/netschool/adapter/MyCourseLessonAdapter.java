package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.examw.netschool.DownloadActivity;
import com.examw.netschool.R;
import com.examw.netschool.VideoPlayActivity;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.model.Lesson;

/**
 * 我的课程资源数据适配器。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class MyCourseLessonAdapter extends BaseAdapter{
	private static final String TAG = "MyCourseLessonAdapter";
	private final Context context;
	private final LayoutInflater inflater;
	private final List<Lesson> list;
	private final String userId;
	private DownloadDao downloadDao;
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 * @param lessons
	 */
	public MyCourseLessonAdapter(Context context,String userId,  List<Lesson> lessons){
		Log.d(TAG, "初始化...");
		this.inflater = LayoutInflater.from(this.context = context);
		this.userId = userId;
		this.list = lessons;
	}
	/*
	 * 获取数据总数。
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		Log.d(TAG, "获取数据总数...");
		return this.list == null ? 0 : this.list.size();
	}
	/*
	 * 获取数据。
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		Log.d(TAG, "获取["+position+"]数据...");
		return (this.list == null || this.list.size() < position) ? null : this.list.get(position);
	}
	/*
	 * 获取数据ID。
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	/*
	 * 获取数据行视图。
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "获取数据行["+position+"]视图...");
		ViewHolder viewHolder = null;
		if(convertView == null){
			Log.d(TAG, "新建数据行:" + position);
			//加载
			convertView = this.inflater.inflate(R.layout.courselist_layout, parent, false);
			//初始化
			viewHolder = new ViewHolder(convertView);
			//缓存
			convertView.setTag(viewHolder);
		}else {
			Log.d(TAG, "重用数据行:" + position);
			viewHolder = (ViewHolder)convertView.getTag();
		}
		//加载数据
		viewHolder.loadData(position);
		//返回
		return convertView;
	}
	//
	private class ViewHolder implements OnClickListener{
		private TextView nameView,downView;
		private ImageButton btnPlay;
		private Lesson lesson;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			if(convertView == null) return;
			//名称
			this.nameView = (TextView)convertView.findViewById(R.id.text4);
			//
			this.downView = (TextView)convertView.findViewById(R.id.Downprogresstext);
			//
			this.btnPlay = (ImageButton) convertView.findViewById(R.id.playerBtn);
			this.btnPlay.setOnClickListener(this);
		}
		/**
		 * 加载数据。
		 * @param pos
		 */
		public void loadData(int pos){
			if(list == null || list.size() < pos) return;
			//
			this.lesson = list.get(pos);
			if(lesson == null) return;
			//
			this.nameView.setText(lesson.getName());
			this.nameView.setOnClickListener(this);
			
			//惰性加载数据操作
			if(downloadDao == null){
				Log.d(TAG, "惰性加载数据...");
				downloadDao = new DownloadDao(context, userId);
			}
			//是否存在
			if(downloadDao.hasDownload(lesson.getId())){//已下载
				//加载下载数据
				final Download download = downloadDao.getDownload(lesson.getId());
				final DownloadState state = DownloadState.parse(download.getState());
				if(state== DownloadState.FINISH){//下载完成
					this.downView.setText(state.getName());
					this.downView.setTextColor(context.getResources().getColor(R.color.green));
				}else{//下载中
					this.downView.setText(state.getName());
					this.downView.setTextColor(context.getResources().getColor(R.color.red));
				}
			}else{//未下载
				this.downView.setText("未下载");
				this.downView.setTextColor(context.getResources().getColor(R.color.grey));
			}
		}
		/*
		 * 点击事件处理。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			Log.d(TAG, "点击事件处理..." + v);
			if(this.lesson == null) return;
			//初始化
			Intent intent = null;
			switch(v.getId()){
				case R.id.text4:{//播放
					intent = new Intent(context, VideoPlayActivity.class);
					Log.d(TAG, "播放处理...");
					break;
				}
				case R.id.playerBtn:{//下载
					intent = new Intent(context, DownloadActivity.class);
					Log.d(TAG, "下载处理...");
					break;
				}
			}
			//
			if(intent != null){
				intent.putExtra(Constant.CONST_USERID, userId);
				intent.putExtra(Constant.CONST_LESSON_ID, this.lesson.getId());
				intent.putExtra(Constant.CONST_LESSON_NAME, this.lesson.getName());
				context.startActivity(intent);
			}
		}
	}
}
package com.examw.netschool.adapter;

import java.util.List;

import com.examw.netschool.R;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.model.Lesson;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 课程资源下来数据适配器。
 * 
 * @author jeasonyoung
 * @since 2015年9月24日
 */
public class SpinnerLessonAdapter extends BaseAdapter {
	private static final String TAG = "SpinnerLessonAdapter";
	private final List<Lesson> list;
	/**
	 * 构造函数。
	 * @param lessons
	 */
	public SpinnerLessonAdapter(List<Lesson> lessons){
		Log.d(TAG, "初始化...");
		this.list = lessons;
	}
	/*
	 * 获取数据总数。
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return (this.list == null) ? 0 : this.list.size();
	}
	/*
	 * 获取数据对象。
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
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
	 * 获取数据项View。
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "获取数据项View..." + position);
		ViewHolder viewHolder = null;
		if(convertView == null){
			Log.d(TAG, "新建数据项..." + position);
			//加载数据项布局
			convertView = LayoutInflater.from(AppContext.getContext()).inflate(R.layout.activity_answer_submit_lesson_item, parent, false);
			//初始化
			viewHolder = new ViewHolder(convertView);
			//缓存
			convertView.setTag(viewHolder);
		}else{
			Log.d(TAG, "重用数据项..." + position);
			viewHolder = (ViewHolder)convertView.getTag();
		}
		//加载数据
		viewHolder.loadData((Lesson)this.getItem(position));
		//返回
		return convertView;
	}
	//
	private class ViewHolder{
		private TextView tvTitle;
		//
		public ViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.title);
		}
		//
		public void loadData(Lesson lesson){
			if(lesson != null){
				this.tvTitle.setText(lesson.getName());
			}
		}
	}
}
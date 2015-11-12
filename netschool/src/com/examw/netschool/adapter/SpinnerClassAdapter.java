package com.examw.netschool.adapter;

import java.util.List;

import com.examw.netschool.R;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.model.PackageClass;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 班级下载数据适配器。
 * 
 * @author jeasonyoung
 * @since 2015年9月24日
 */
public class SpinnerClassAdapter extends BaseAdapter {
	private static final String TAG = "SpinnerClassAdapter";
	private final List<PackageClass> list;
	/**
	 * 构造函数。
	 * @param courses
	 */
	public SpinnerClassAdapter(List<PackageClass> courses){
		Log.d(TAG, "初始化...");
		this.list = courses;
	}
	/*
	 * 获取数据总量。
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return (this.list == null) ? 0 : this.list.size();
	}
	/*
	 * 获取数据项。
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return (this.list == null || this.list.size() < position) ? null : this.list.get(position);
	}
	/*
	 * 获取数据项ID。
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
			Log.d(TAG, "新建数据行..." + position);
			//加载行布局
			convertView = LayoutInflater.from(AppContext.getContext()).inflate(R.layout.activity_answer_submit_class_item, parent, false);
			//初始化
			viewHolder = new ViewHolder(convertView);
			//缓存
			convertView.setTag(viewHolder);
		}else{
			Log.d(TAG, "重用数据行..." + position);
			viewHolder = (ViewHolder)convertView.getTag();
		}
		//加载数据
		viewHolder.loadData((PackageClass)this.getItem(position));
		//返回
		return convertView;
	}
	//
	private class ViewHolder{
		private TextView tvTitle;
		//
		public ViewHolder(View convertView){
			//加载
			this.tvTitle = (TextView)convertView;
		}
		//
		public void loadData(PackageClass course){
			if(course != null){
				this.tvTitle.setText(course.getName());
			}
		}
	}
}
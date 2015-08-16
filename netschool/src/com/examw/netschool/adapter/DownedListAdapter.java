package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.entity.Course;

public class DownedListAdapter extends BaseAdapter
{
	private LayoutInflater inflater;
	private List<Course> list;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.list == null)
		      return 0;
		return list.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (this.list == null)
		      return null;
		return list.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.list_downfinish_layout, null);
			holder = new ViewHolder();
			holder.view = (TextView) convertView.findViewById(R.id.filenameLab);
			convertView.setTag(holder);
		}else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.view.setText(list.get(position).getCourseName());
		return convertView;
	}
	public DownedListAdapter(Context context,List<Course> list) {
		// TODO Auto-generated constructor stub
		inflater = LayoutInflater.from(context);
		this.list = list;
	}
	static class ViewHolder
	{
		TextView view;
	}
}
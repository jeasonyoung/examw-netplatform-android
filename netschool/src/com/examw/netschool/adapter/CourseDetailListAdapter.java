package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.examw.netschool.R;

public class CourseDetailListAdapter extends BaseAdapter{
	private Context context;
	private List<String> courses;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return courses.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return courses.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public android.view.View getView(int position, android.view.View v, android.view.ViewGroup parent) {
		ViewHolder holder = null;
		if(v == null)
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(R.layout.courselist_layout, null);
			holder = new ViewHolder();
			holder.name = (TextView) v.findViewById(R.id.text4);
			holder.isDown = (TextView) v.findViewById(R.id.Downprogresstext);
			holder.btn = (ImageButton) v.findViewById(R.id.playerBtn);
			holder.btn.setImageResource(R.drawable.palyico2);
			holder.btn.setFocusable(false);
			holder.isDown.setText("");
			v.setTag(holder);
		}else
		{
			holder = (ViewHolder) v.getTag();
		}
		holder.name.setText(courses.get(position));
		return v;
		
	};
	public CourseDetailListAdapter(Context context,List<String> course) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.courses = course;
	}
	static class ViewHolder
	{
		TextView name;
		TextView isDown;
		ImageButton btn;
	}
}

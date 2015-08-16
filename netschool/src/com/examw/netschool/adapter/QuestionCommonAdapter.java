package com.examw.netschool.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.entity.QuestionAdapterData;

public class QuestionCommonAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private ArrayList<QuestionAdapterData> dataList;
	public QuestionCommonAdapter(Context context,ArrayList<QuestionAdapterData> datalist) {
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.dataList = datalist;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dataList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return dataList.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(v == null)
		{
			v = mInflater.inflate(R.layout.list_question_tier, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.classNameText);
			holder.count = (TextView) v.findViewById(R.id.recordText);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		holder.title.setText(dataList.get(position).getTitle());
		holder.count.setText(String.valueOf(dataList.get(position).getCount()));
		return v;
	}
	static class ViewHolder
	{
		TextView title;
		TextView count;
	}
}

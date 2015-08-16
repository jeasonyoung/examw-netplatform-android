package com.examw.netschool.adapter;

import java.util.LinkedList;

import org.json.JSONArray;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.entity.Problem;

public class ProblemListAdapter extends BaseAdapter {
	private Context context;
	private LinkedList<Problem> list;
	public ProblemListAdapter(Context context,LinkedList<Problem> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list==null)
			return 0;
		return list.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(list == null)
			return null;
		return list.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return  position;
	}
	public android.view.View getView(int position, android.view.View v, android.view.ViewGroup parent) {
		ViewHolder holder  =null;
		if(v == null)
		{
			v = LayoutInflater.from(context).inflate(R.layout.list_tquestion, null);
			holder = new ViewHolder();
			holder.title = (TextView) v.findViewById(R.id.TAnswerTitleText);
			holder.answerNum = (TextView) v.findViewById(R.id.TAnswerNumText);
			holder.path = (TextView) v.findViewById(R.id.TAnswerClassText);
			holder.addTime = (TextView) v.findViewById(R.id.TAnswerTimeText);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		Problem p = list.get(position);
		holder.title.setText(p.getTitle());
		int num = getAnswerNum(p);
		holder.answerNum.setText("已回答("+num+")");
		holder.path.setText(p.getPath());
		holder.addTime.setText(p.getAddTime());
		return v;
	};
	private int getAnswerNum(Problem p)
	{
		try{
			JSONArray json = new JSONArray(p.getAnswersJson());
			return json.length();
		}catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	static class ViewHolder
	{
		TextView title;
		TextView answerNum;
		TextView path;
		TextView addTime;
	}
}

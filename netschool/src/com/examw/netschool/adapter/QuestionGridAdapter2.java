package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.entity.ExamQuestion;

public class QuestionGridAdapter2 extends BaseAdapter{
	private Context context;
	private List<ExamQuestion> questionList;
	private String[] data;
	public QuestionGridAdapter2(Context context,List<ExamQuestion> questionList,String[] data) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.questionList = questionList;
		this.data = data;;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(questionList==null)
		{
			if(data==null)
			{
				return 0;
			}
			return data.length;
		}
		return questionList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if(v == null)
		{
			v = LayoutInflater.from(context).inflate(R.layout.list_question_option, null);
			holder = new ViewHolder();
			holder.textView = (TextView) v.findViewById(R.id.optionTextView);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		if(questionList!=null)
		{
			holder.textView.setText(position+1+"");
			ExamQuestion q = questionList.get(position);
			if(q.getUserAnswer()!=null)
			{
				if(q.getAnswer().equals(q.getUserAnswer()))
				{
				//答对了
					v.setBackgroundColor(context.getResources().getColor(R.color.green));
				}else
				{
					v.setBackgroundColor(context.getResources().getColor(R.color.red));
				}
			}
			return v;
		}
		if(data!=null)
		{
			holder.textView.setText(data[position]);
			if(position==2)
			{
				holder.textView.setTextColor(context.getResources().getColor(R.color.red));
			}
			return v;
		}
		return v;
	}
	static class ViewHolder
	{
		TextView textView;
	}
}


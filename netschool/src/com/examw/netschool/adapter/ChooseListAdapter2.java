package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.QuestionChooseActivity;
import com.examw.netschool.R;
import com.examw.netschool.customview.MyGridView;
import com.examw.netschool.entity.ExamQuestion;

public class ChooseListAdapter2 extends BaseAdapter{
	private Context context;
	private QuestionChooseActivity activity;
	private List<ExamQuestion> questionList;
	private String action;
	public ChooseListAdapter2(Context context,QuestionChooseActivity activity,List<ExamQuestion> questionList,String action) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.activity = activity;
		this.action = action;
		this.questionList = questionList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
			return 1;
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
		v = LayoutInflater.from(context).inflate(R.layout.list_question_directory, null);
		MyGridView gridView = (MyGridView) v.findViewById(R.id.directory_exam_grid);
		gridView.setAdapter(new QuestionGridAdapter2(context,questionList,null));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if("submitPaper".equals(action))
				{
					Intent data=new Intent();  
		         	data.putExtra("action", "showQuestionWithAnswer");  
		         	data.putExtra("cursor", Integer.parseInt(((TextView)arg1.findViewById(R.id.optionTextView)).getText().toString())-1);  
		         	//请求代码可以自己设置，这里设置成20  
		         	activity.setResult(20, data);  
		         	//关闭掉这个Activity  
		         	activity.finish();
				}else
				{
					activity.showAnswer(Integer.parseInt(((TextView)arg1.findViewById(R.id.optionTextView)).getText().toString())-1);
					activity.finish();
				}
			}
		});
		return v;
	}
}
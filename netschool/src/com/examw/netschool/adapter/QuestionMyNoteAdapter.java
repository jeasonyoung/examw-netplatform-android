package com.examw.netschool.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.QuestionDoExamActivity;
import com.examw.netschool.R;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamNote;
import com.google.gson.Gson;

public class QuestionMyNoteAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ExamNote> data;
	private PaperDao dao;
	private Gson gson = new Gson();
	public QuestionMyNoteAdapter(Context context,PaperDao dao,ArrayList<ExamNote> data) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.dao = dao;
		this.data = data;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
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
			v = LayoutInflater.from(context).inflate(R.layout.list_question_notebook, null);
			holder = new ViewHolder();
			holder.content = (TextView) v.findViewById(R.id.notebookContent);
			holder.addTime = (TextView) v.findViewById(R.id.noteTime);
			holder.showQuestion = (TextView) v.findViewById(R.id.lookSource);
			v.setTag(holder);
		}
		holder = (ViewHolder) v.getTag();
		final ExamNote n = data.get(position);
		holder.content.setText(n.getContent());
		holder.addTime.setText(n.getAddTime());
		holder.showQuestion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context,QuestionDoExamActivity.class);
				intent.putExtra("action", "showNoteSource");
				intent.putExtra("username",n.getUsername());
				intent.putExtra("paperId", n.getPaperId());
				intent.putExtra("questionListJson", gson.toJson(dao.findQuestionById(n.getQid())));
				context.startActivity(intent);
			}
		});
		return v;
	};
	static class ViewHolder 
	{
		TextView content;
		TextView addTime;
		TextView showQuestion;
	}
	
}

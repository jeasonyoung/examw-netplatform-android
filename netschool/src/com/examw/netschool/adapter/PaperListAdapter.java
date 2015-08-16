package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.entity.Paper;

public class PaperListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Paper> papers;
	public PaperListAdapter(Context context,List<Paper> papers) {
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.papers = papers;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(papers!=null)
			return papers.size();
		return 0;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(papers!=null)
			return papers.get(position);
		return null;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView==null)
		{
			convertView = mInflater.inflate(R.layout.list_question_exam, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.paper_name_TextView);
			holder.info = (TextView) convertView.findViewById(R.id.paper_info_TextView);
			//holder.doExam = (Button) convertView.findViewById(R.id.paper_action_btn);
			convertView.setTag(holder);
		}else
		{
			holder = (ViewHolder) convertView.getTag(); 
		}
		final Paper p = papers.get(position);
		holder.title.setText(p.getPaperName());
		holder.info.setText("考试时间:"+p.getPaperTime()+"分钟,"+"总分:"+p.getPaperSorce()+"分");
		//holder.doExam.setText("开始考试");
//		holder.doExam.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Context context = mInflater.getContext();
//				Intent intent = new Intent(context,QuestionDoExamActivity.class);
//				intent.putExtra("paperName", p.getPaperName());
//				intent.putExtra("paperId", p.getPaperId());
//				context.startActivity(intent);
//			}
//		});
		return convertView;
	}
	static class ViewHolder
	{
		TextView title,info;
		//Button doExam;
	}
}

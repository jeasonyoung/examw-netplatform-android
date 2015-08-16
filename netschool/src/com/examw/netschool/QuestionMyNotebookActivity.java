package com.examw.netschool;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.examw.netschool.adapter.QuestionMyNoteAdapter;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamNote;
import com.umeng.analytics.MobclickAgent;

public class QuestionMyNotebookActivity extends Activity{
	private ImageButton returnbtn;
	private TextView title,paperTitle;
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private ListView notebookListView;
	private LinearLayout nodataLayout;
	private ArrayList<ExamNote> data;
	private PaperDao dao;
	private QuestionMyNoteAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doproblemrecord_tier2);
		initView();
		initData();
		this.loadingLayout.setVisibility(View.GONE);
		if(data.size()==0)
		{
			this.contentLayout.setVisibility(View.GONE);
			this.nodataLayout.setVisibility(View.VISIBLE);
		}else
		{
			mAdapter = new QuestionMyNoteAdapter(this,dao, data);
			this.notebookListView.setAdapter(mAdapter);
		}
		this.notebookListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				dao.deleteNote(data.get(arg2));
				data.remove(arg2);
				mAdapter.notifyDataSetChanged();
				if(data.size()==0)
				{
					nodataLayout.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
	}
	private void initData()
	{
		dao = new PaperDao(this);
		Intent intent = this.getIntent();
		this.paperTitle.setText(intent.getStringExtra("title"));
		data = (ArrayList<ExamNote>) dao.findNotes(intent.getStringExtra("paperId"),intent.getStringExtra("username")); 
	}
	private void initView()
	{
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.title = (TextView) this.findViewById(R.id.TopTitle1);
		this.paperTitle = (TextView) this.findViewById(R.id.paperTitle);
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.contentLayout = (LinearLayout) this.findViewById(R.id.questionContentLayout);
		this.notebookListView = (ListView) this.findViewById(R.id.question_record_ListView);
		this.returnbtn.setOnClickListener(new ReturnBtnClickListener(this));
		this.title.setText(R.string.my_notebookStr);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
	}
}
package com.examw.netschool;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.examw.netschool.adapter.PaperListAdapter2;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRecord;
import com.examw.netschool.entity.ExamRule;
import com.google.gson.Gson;

public class QuestionRecordActivity extends Activity{
	private ImageButton returnbtn;
	private TextView topTitle;
	private LinearLayout contentLayout,nodataLayout,loadingLayout;
	private ListView paperListView;
	private String username;
	private List<ExamRecord> recordList;
	private PaperDao dao;
	private PaperListAdapter2 mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_questionblank_examtier2);
		findViews();
		dao = new PaperDao(this);
		Intent intent = this.getIntent();
		username = intent.getStringExtra("username");
		initData();
		this.paperListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
				ExamRecord r = recordList.get(arg2);
				loadingLayout.setVisibility(View.VISIBLE);
				itemClickMethod(r);
			}
		});
		this.paperListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showDeleteWindow(arg2);
				return true;
			}
		});
	}
	private void findViews()
	{
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.noneDataLayout);
		this.topTitle = (TextView) this.findViewById(R.id.TopTitle1);
		this.contentLayout = (LinearLayout) this.findViewById(R.id.questionContentLayout);
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.paperListView = (ListView) this.findViewById(R.id.contentListView);
		this.topTitle.setText("做题记录");
		this.returnbtn.setOnClickListener(new ReturnBtnClickListener(this));
	}
	@Override
	protected void onStart() {
		this.loadingLayout.setVisibility(View.GONE);
		super.onStart();
	}
	private void initData()
	{
		recordList = dao.findRecordsByUsername(username);
		if(recordList==null||recordList.size()==0)
		{
			this.contentLayout.setVisibility(View.GONE);
			this.nodataLayout.setVisibility(View.VISIBLE);
		}else
		{
			mAdapter = new PaperListAdapter2(this,recordList);
			this.paperListView.setAdapter(mAdapter);
		}
	}
	private void showDeleteWindow(final int index)
	{
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setTitle("删除").setMessage("是否删除此记录").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// TODO Auto-generated method stub
					//停止下载服务
					dao.deleteRecord(recordList.get(index));
					recordList.remove(index);
					if(recordList.size()==0)
					{
						contentLayout.setVisibility(View.GONE);
						nodataLayout.setVisibility(View.VISIBLE);
					}else
					mAdapter.notifyDataSetChanged();
				}                      
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}                      
			});
		 localBuilder.create().show();
	}
	private void itemClickMethod(ExamRecord r)
	{
		//没有交卷的
		Gson gson = new Gson();
		Intent mIntent = null;
		List<ExamQuestion> questionList = dao.findQuestionByPaperId(r.getPaperId());
		List<ExamRule> ruleList = dao.findRules(r.getPaperId());
		String ruleListJson = gson.toJson(ruleList);
		if(r.getAnswers()==null)
		{
			mIntent = new Intent(this,QuestionDoExamActivity.class);
			mIntent.putExtra("action", "DoExam");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("paperName", r.getPapername());
			mIntent.putExtra("paperId", r.getPaperId());
			mIntent.putExtra("username", username);
			mIntent.putExtra("tempTime", r.getPapertime()*60);
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("paperScore", r.getPaperscore());
			mIntent.putExtra("questionListJson", gson.toJson(questionList));
			this.startActivity(mIntent);
		}else
		{
			SparseBooleanArray isDone = new SparseBooleanArray();
			addAnswer(isDone,questionList,r.getAnswers());
			mIntent = new Intent(this,QuestionChooseActivity.class);
			mIntent.putExtra("action", "showResult");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("questionList", gson.toJson(questionList));
			mIntent.putExtra("paperScore", r.getPaperscore());
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("username", username);
			mIntent.putExtra("paperid", r.getPaperId());
			mIntent.putExtra("useTime", r.getUseTime());
			mIntent.putExtra("record", gson.toJson(r));
			mIntent.putExtra("isDone", gson.toJson(isDone));
			mIntent.putExtra("userScore", r.getScore()); // 本次得分
			this.startActivity(mIntent);	//仍然是要启动这个Activity不带结果返回
		}
	}
	private void addAnswer(SparseBooleanArray isDone,List<ExamQuestion> list,String tempAnswer)
	{
		if (tempAnswer == null || "".equals(tempAnswer.trim())) {
			return;
		}
		int listSize = list.size();
		String choiceAnswer = null, textAnswer = null;
		if (tempAnswer.indexOf("   ") != -1) {
			choiceAnswer = tempAnswer.substring(0, tempAnswer.indexOf("   "));
			textAnswer = tempAnswer.substring(tempAnswer.indexOf("   ") + 3);
		} else {
			choiceAnswer = tempAnswer;
		}
		for (int i = 0; i < listSize; i++) {
			ExamQuestion q = list.get(i);
			String str = q.getQid() + "-";
			if ((!"问答题".equals(q.getQType()))
					&& choiceAnswer.indexOf(str) != -1) {
				String temp = choiceAnswer.substring(choiceAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("&")));
				isDone.append(i, true);
			} else if (textAnswer != null && "问答题".equals(q.getQType())
					&& textAnswer.indexOf(str) != -1) {
				String temp = textAnswer.substring(textAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(),
						temp.indexOf("   ")));
				isDone.append(i, true);
			}
		}
	}
}
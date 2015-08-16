package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRecord;
import com.examw.netschool.entity.ExamRule;
import com.examw.netschool.entity.Paper;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

public class QuestionPaperInfoActivity extends Activity implements OnClickListener{
	private LinearLayout ruleInfo;
	private TextView paperTitle,totalNum,ruleSize,paperScore,paperTime;
	private List<ExamRule> ruleList;
	private Button startBtn,restartBtn;
	private ImageButton returnBtn;
	private Paper paper;
	private String gid;
	private String paperid;
	private PaperDao dao;
	private ProgressDialog dialog;
	private Handler handler;
	private String username,loginType,paperJson;
	private ExamRecord record;
	private int tempTime;
	private List<ExamQuestion> questionList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_paperinfo);
		Intent intent = this.getIntent();
		paperJson = intent.getStringExtra("paperJson");
		gid = intent.getStringExtra("gid");
		paperid = intent.getStringExtra("paperid");
		username = intent.getStringExtra("username");
		loginType = intent.getStringExtra("loginType");
		if(dao==null)
			dao = new PaperDao(this);
		record = dao.findRecord(username, paperid);
		dialog = ProgressDialog.show(QuestionPaperInfoActivity.this,null,"加载中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		handler = new MyHandler(this);
		new GetQuestionListThread().start();
	}
	private void initView(String paperJson)
	{
		this.paperTitle = (TextView) this.findViewById(R.id.papertitle);
		this.ruleSize = (TextView) this.findViewById(R.id.rulesize);
		this.totalNum = (TextView) this.findViewById(R.id.questionNumTotal);
		this.paperScore = (TextView) this.findViewById(R.id.paperscore);
		this.paperTime = (TextView) this.findViewById(R.id.papertime);
		this.ruleInfo = (LinearLayout) this.findViewById(R.id.ruleInfoLayout);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.startBtn = (Button) this.findViewById(R.id.btn_pratice);
		this.restartBtn = (Button) this.findViewById(R.id.btn_restart);
		this.returnBtn.setOnClickListener(this);
		this.restartBtn.setOnClickListener(this);
		this.startBtn.setOnClickListener(this);
		if("local".equals(loginType))
		{
			this.paperTitle.setText(paper.getPaperName());
			this.paperScore.setText(paper.getPaperSorce()+"");
			this.paperTime.setText(paper.getPaperTime()+"");
			this.tempTime = this.paper.getPaperTime()*60;
			int length = ruleList.size();
			this.ruleSize.setText(length+"");
			int total_n = 0;
			for(int i=0;i<length;i++)
			{
				ExamRule r = ruleList.get(i);
				total_n += r.getQuestionNum();
				View v = LayoutInflater.from(this).inflate(R.layout.list_ruleinfo, null);
				TextView ruleTitle = (TextView) v.findViewById(R.id.ruleTitle);
				ruleTitle.setText("第"+(i+1)+"大题"+r.getRuleTitle());
				TextView ruleTitleInfo = (TextView) v.findViewById(R.id.ruleTitleInfo);
				ruleTitleInfo.setText("说明:"+r.getFullTitle());
				this.ruleInfo.addView(v,i);
			}
			if(record != null&&record.getTempAnswer()!=null&&!"".equals(record.getTempAnswer()))
			{
				this.startBtn.setText("继续考试");
				this.restartBtn.setVisibility(View.VISIBLE);
				this.tempTime = record.getTempTime();
			}else if(record!=null&&record.getAnswers()!=null)
			{
				this.startBtn.setText("查看成绩");
				this.restartBtn.setVisibility(View.VISIBLE);
			}else
			{
				this.startBtn.setText("开始考试");
				this.restartBtn.setVisibility(View.GONE);
			}
			this.totalNum.setText(total_n+"");
			return;
		}
		ruleList = new ArrayList<ExamRule>();
		try{
			JSONObject obj = new JSONObject(paperJson);
			paper  = new Paper(obj.optString("paperId")+"",obj.optString("paperName"),obj.optInt("paperScore"),obj.optInt("paperTime"),gid+"",null);
			this.paperTitle.setText(obj.optString("paperName"));
			this.paperScore.setText(obj.optInt("paperScore",100)+"");
			this.paperTime.setText(obj.optInt("paperTime")+"");
			this.tempTime = this.paper.getPaperTime()*60;
			JSONArray rules = obj.getJSONArray("examRules");
			int length = rules.length();
			this.ruleSize.setText(length+"");
			int total_n = 0;
			for(int i=0;i<length;i++)
			{
				JSONObject r = rules.getJSONObject(i);
				//String ruleId, String paperId, String ruleTitle,String ruleTitleInfo,
				//String ruleType,String scoreSet, int questionNum, double scoreForEach, int orderInPaper
				ExamRule rule = new ExamRule(r.getInt("ruleId")+"",r.getInt("paperId")+"",r.getString("title"),
						r.getString("fullTitle"),r.getString("type"),r.getString("ruleScoreSet"),r.getInt("ruleQuestionNum"),
						r.getDouble("ruleScoreForEach"),r.getInt("ruleIdInPaper"));
				total_n += r.getInt("ruleQuestionNum");
				View v = LayoutInflater.from(this).inflate(R.layout.list_ruleinfo, null);
				TextView ruleTitle = (TextView) v.findViewById(R.id.ruleTitle);
				ruleTitle.setText("第"+(i+1)+"大题"+r.getString("title"));
				TextView ruleTitleInfo = (TextView) v.findViewById(R.id.ruleTitleInfo);
				ruleTitleInfo.setText("说明:"+r.getString("fullTitle"));
				rule.setFullTitle(r.getString("fullTitle"));
				this.ruleInfo.addView(v,i);
				ruleList.add(rule);
			}
			if(record != null&&record.getTempAnswer()!=null&&!"".equals(record.getTempAnswer()))
			{
				this.startBtn.setText("继续考试");
				this.restartBtn.setVisibility(View.VISIBLE);
				this.tempTime = record.getTempTime();
			}else if(record!=null&&record.getAnswers()!=null)
			{
				this.startBtn.setText("查看成绩");
				this.restartBtn.setVisibility(View.VISIBLE);
			}else
			{
				this.startBtn.setText("开始考试");
				this.restartBtn.setVisibility(View.GONE);
			}
			this.totalNum.setText(total_n+"");
			dao.insertPaper(paper, ruleList);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.returnbtn:
			this.finish();
			return;
		case R.id.btn_pratice:
			gotoDoExamActivity();
			return;
		case R.id.btn_restart:
			restart();
			return;
		}
	}
	private void gotoDoExamActivity()
	{
		if(record!=null&&record.getAnswers()!=null&&!"".equals(record.getAnswers()))
		{
			Gson gson = new Gson();
			SparseBooleanArray isDone = new SparseBooleanArray();
			addAnswer(isDone,questionList,record.getAnswers());
			Intent mIntent = new Intent(this,QuestionChooseActivity.class);
			mIntent.putExtra("action", "showResult");
			mIntent.putExtra("ruleListJson", gson.toJson(ruleList));
			mIntent.putExtra("questionList", gson.toJson(questionList));
			mIntent.putExtra("paperScore", record.getPaperscore());
			mIntent.putExtra("paperTime", record.getPapertime());
			mIntent.putExtra("username", username);
			mIntent.putExtra("paperid", record.getPaperId());
			mIntent.putExtra("useTime", record.getUseTime());
			mIntent.putExtra("record", gson.toJson(record));
			mIntent.putExtra("isDone", gson.toJson(isDone));
			mIntent.putExtra("userScore", record.getScore()); // 本次得分
			this.startActivity(mIntent);	//仍然是要启动这个Activity不带结果返回
		}else {
			if(questionList==null||questionList.size()==0)
			{
				Toast.makeText(this, "没有题目数据暂时不能练习", Toast.LENGTH_SHORT).show();
				return;
			}
			MobclickAgent.onEvent(this,"Do_Exam_Paper");
			Intent intent = new Intent(this,QuestionDoExamActivity2.class);
			intent.putExtra("paperName", paper.getPaperName());
			intent.putExtra("paperId", paper.getPaperId());
			intent.putExtra("paperTime",paper.getPaperTime());
			intent.putExtra("tempTime", tempTime);
			intent.putExtra("paperScore", paper.getPaperSorce());
			intent.putExtra("action", "DoExam");
			Gson gson = new Gson();
			intent.putExtra("ruleListJson", gson.toJson(ruleList));
			intent.putExtra("questionListJson", gson.toJson(questionList));
			intent.putExtra("username", username);
			this.startActivity(intent);
			this.finish();	//结束生命
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
	private void restart()
	{
		record.setTempAnswer(null);
		dao.updateTempAnswerForRecord(record);
		Intent intent = new Intent(this,QuestionDoExamActivity2.class);
		intent.putExtra("paperName", paper.getPaperName());
		intent.putExtra("paperId", paper.getPaperId());
		intent.putExtra("paperTime",paper.getPaperTime());
		intent.putExtra("tempTime", tempTime);
		intent.putExtra("paperScore", paper.getPaperSorce());
		intent.putExtra("action", "DoExam");
		Gson gson = new Gson();
		intent.putExtra("ruleListJson", gson.toJson(ruleList));
		intent.putExtra("questionListJson", gson.toJson(questionList));
		intent.putExtra("username", username);
		this.startActivity(intent);
		this.finish();	//结束生命
	}
	private class GetQuestionListThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if("local".equals(loginType))
			{
				paper = new Gson().fromJson(paperJson, Paper.class);
				ruleList = dao.findRules(paper.getPaperId());
				questionList = dao.findQuestionByPaperId(paper.getPaperId());
				
			}
			try{
				String result = HttpConnectUtil.httpGetRequest(QuestionPaperInfoActivity.this, Constant.DOMAIN_URL+"mobile/questionListofPaper?paperid="+paperid);
				//解析result
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置expandableListView的adapter
            		try
            		{
            			JSONArray json = new JSONArray(result);
            			int length = json.length();
            			if(length>0)
            			{
            				questionList = new ArrayList<ExamQuestion>();
            				for(int i=0;i<length;i++)
            				{
            					JSONObject obj = json.getJSONObject(i);
            					/*
            					 * String qid,String ruleid, String paperId, String content,
										String answer, String analysis, String linkQid,
											int qType, int optionNum, int orderId
            					 */
            					ExamQuestion q = new ExamQuestion(obj.optInt("questId")+"",obj.optInt("questRuleId")+"",obj.optInt("questPaperId")+"",obj.optString("questContent"),
            										obj.optString("questAnswer"),obj.optString("questAnalysis"),obj.optString("questLinkQuestionId"),
            										obj.optString("type"),obj.optInt("questOptionNum"),obj.optInt("questOrderId"));
            					questionList.add(q);
            				}
            			}
            			dao.insertQuestions(questionList);
            			Message msg = handler.obtainMessage();
        				msg.what = 1;
        				handler.sendMessage(msg);
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            			handler.sendEmptyMessage(-2);
            		}
            	}else
            	{
            		Message msg = handler.obtainMessage();
    				msg.what = -2;
    				handler.sendMessage(msg);
            	}
			}catch(Exception e)
			{
				Message msg = handler.obtainMessage();
				msg.what = -1;
				handler.sendMessage(msg);
			}
		}
	}
	static class MyHandler extends Handler {
        WeakReference<QuestionPaperInfoActivity> mActivity;
        MyHandler(QuestionPaperInfoActivity activity) {
                mActivity = new WeakReference<QuestionPaperInfoActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	QuestionPaperInfoActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
                	theActivity.initView(theActivity.paperJson);
                	break;
                case -2:
                	theActivity.dialog.dismiss();
                	Toast.makeText(theActivity, "暂时没有数据", Toast.LENGTH_SHORT).show();
                	break;
                case -1:
                	//连不上,
                	theActivity.dialog.dismiss();
                	Toast.makeText(theActivity, "连不上服务器", Toast.LENGTH_SHORT).show();
                	break;
                }
        }
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null)
		{
			dialog.dismiss();	
		}
		super.onDestroy();
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

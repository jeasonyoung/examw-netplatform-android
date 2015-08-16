package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.adapter.PopRuleListAdapter;
import com.examw.netschool.adapter.QuestionAdapter2;
import com.examw.netschool.adapter.QuestionAdapter2.AnswerViewHolder;
import com.examw.netschool.adapter.QuestionAdapter2.ContentViewHolder;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamErrorQuestion;
import com.examw.netschool.entity.ExamFavor;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRecord;
import com.examw.netschool.entity.ExamRule;
import com.examw.netschool.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 试题界面 有几个操作都共用此界面 doExam, examTitle 显示试卷标题,返回键提示是否退出考试,下面是交卷按钮
 * 进来先加考试记录,选题要看是否有答案,答案的初始化 doErrors,examTitle显示错题集,返回键直接finish,下面是答案按钮
 * doFavors,examTitle显示我的收藏,返回键直接finish,下面是答案按钮
 * doNotes,examTitle显示我的笔记,返回键直接finish,下面是答案按钮,选题没有,大题项没有
 * 
 * @author Administrator
 * 
 */
public class QuestionDoExamActivity2 extends Activity implements OnClickListener {
	// 组件
	private ImageButton exitExamImgBtn, nextBtn, preBtn, removeBtn, answerBtn,favoriteBtn;
	private TextView timeCountDown, examTitle, examTypeTextView;
	private Button chooseQuestionBtn;
	private LinearLayout nodataLayout, loadingLayout, ruleTypeLayout;
	private Handler timeHandler;
	// 数据
	private String papername, username;
	private String paperid;
	private String action;
	private String ruleListJson;
	private StringBuffer favorQids;
	private int paperTime, time, paperScore;
	private ArrayList<ExamRule> ruleList;
	private ArrayList<ExamQuestion> questionList;
	private ExamQuestion currentQuestion;
	private int questionCursor;
	private ExamRule currentRule;
	private StringBuffer answerBuf, txtAnswerBuf;
	private ExamRecord record;
	private SparseBooleanArray isDone;
	private Gson gson;
	private static boolean timerFlag = true;
	private ExamFavor favor;
	// 选择弹出框
	private PopupWindow popupWindow;
	private ListView lv_group;
	private AlertDialog exitDialog;
	// 提示界面
	private PopupWindow tipWindow;
	private Handler mHandler;
	private MyHandler handler;
	private SharedPreferences guidefile;
	// 数据库操作
	private PaperDao dao;

	private ViewFlow viewFlow;
	private QuestionAdapter2 questionAdapter;

	private ProgressDialog proDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_question_doexam2);
		System.out.println("do exam activity 2 执行 onCreate 方法");
		initView();
		initData();
	}

	private ExamRule getRule(ArrayList<ExamRule> ruleList, ExamQuestion q) {
		for (ExamRule r : ruleList) {
			if (r.getRuleId().equals(q.getRuleId())) {
				return r;
			}
		}
		return new ExamRule("0","其他");
	}

	// 取得主界面的组件,只取得不操作
	private void initView() {
		this.exitExamImgBtn = (ImageButton) this.findViewById(R.id.exitExamImgBtn);// 退出考试
		this.preBtn = (ImageButton) this.findViewById(R.id.previousBtn); // 上一题
		this.nextBtn = (ImageButton) this.findViewById(R.id.nextBtn); // 下一题
		this.favoriteBtn = (ImageButton) this.findViewById(R.id.favoriteBtn);
		this.removeBtn = (ImageButton) this.findViewById(R.id.removeBtn);
		this.timeCountDown = (TextView) this.findViewById(R.id.timecount_down_TextView);// 倒计时
		this.examTitle = (TextView) this.findViewById(R.id.examTitle_TextView);// 考试标题
		this.chooseQuestionBtn = (Button) this.findViewById(R.id.selectTopicId_ImgBtn);// 选题
		this.examTypeTextView = (TextView) this.findViewById(R.id.examTypeTextView);// 大题标题
		this.ruleTypeLayout = (LinearLayout) this.findViewById(R.id.ruleTypeLayout);
		this.answerBtn = (ImageButton) this.findViewById(R.id.answerBtn);// 交卷或者查看答案
		this.viewFlow = (ViewFlow) this.findViewById(R.id.viewflow); // 题目加载组件
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);// 没有数据
		this.nodataLayout.setVisibility(View.GONE);
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout); // 正在加载中
		this.loadingLayout.setVisibility(View.VISIBLE);
		// 绑定事件
		this.preBtn.setOnClickListener(this);
		this.nextBtn.setOnClickListener(this);
		this.removeBtn.setOnClickListener(this);
		this.exitExamImgBtn.setOnClickListener(this);
		this.chooseQuestionBtn.setOnClickListener(this);
		this.answerBtn.setOnClickListener(this);
		this.favoriteBtn.setOnClickListener(this);
		viewFlow.setOnViewSwitchListener(new ViewSwitchListener() {
			@Override
			public void onSwitched(View view, int position) {
				System.out.println("question的位置是: = " + position);
				questionCursor = position;
				currentQuestion = questionList.get(position);
				if (ruleList != null && ruleList.size() > 0) {
					ExamRule currentRule = getRule(ruleList, currentQuestion);
					examTypeTextView.setText(currentRule.getRuleTitle());
				}
				if (favorQids != null && favorQids.indexOf(currentQuestion.getQid()) != -1) {
					favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
				} else {
					favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
				}
				System.out.println("view.getTag = " + view.getTag());
				if ("myErrors".equals(action)) {
					// 有答案了,禁止选择,没答案继续选择
					if (currentQuestion.getUserAnswer() != null) {
						((QuestionAdapter2.ContentViewHolder) view.getTag(R.id.tag_first)).examOption.forbidden(false);
						// questionAdapter.setRadioEnable(((QuestionAdapter.ContentViewHolder)view.getTag(R.id.tag_first)).examOption,
						// false);
					} else {
						((QuestionAdapter2.ContentViewHolder) view.getTag(R.id.tag_first)).examOption.forbidden(true);
						// questionAdapter.setRadioEnable(((QuestionAdapter.ContentViewHolder)view.getTag(R.id.tag_first)).examOption,
						// true);
					}
				}
			}
		});
	}

	private void initData() {
		// 数据初始化
		guidefile = this.getSharedPreferences("guidefile", 0);
		action = getIntent().getStringExtra("action");
		timeHandler = new TimerHandler(this);
		mHandler = new Handler();
		handler = new MyHandler(this);
		// 根据action的不同,区分
		if ("DoExam".equals(action)) {
			this.examTitle.setText(this.papername); // 试卷名字
		} else if ("myNoteBook".equals(action)) {
			this.examTitle.setText("我的笔记");
			this.ruleTypeLayout.setVisibility(View.GONE);
			this.chooseQuestionBtn.setVisibility(View.GONE);// 选题
		} else if ("myErrors".equals(action)) {
			this.examTitle.setText("错题集");
			this.ruleTypeLayout.setVisibility(View.GONE);
			this.removeBtn.setVisibility(View.VISIBLE);
		} else if ("myFavors".equals(action)) {
			this.examTitle.setText("我的收藏");
			this.ruleTypeLayout.setVisibility(View.GONE);
		} else if ("showNoteSource".equals(action)) {
			this.examTitle.setText("我的笔记");
			this.ruleTypeLayout.setVisibility(View.GONE);
		}
		new Thread() {
			public void run() {
				Intent intent = getIntent();
				paperid = intent.getStringExtra("paperId");
				papername = intent.getStringExtra("paperName");
				ruleListJson = intent.getStringExtra("ruleListJson");
//				username = ((AppContext) getApplication()).getUsername();
				username = intent.getStringExtra("username");
				paperTime = intent.getIntExtra("tempTime", 0); // 上次使用的时间
				time = intent.getIntExtra("paperTime", 0) * 60; // 秒
				if (paperTime == 0) {
					paperTime = time;
				}
				paperScore = intent.getIntExtra("paperScore", 0);
				action = intent.getStringExtra("action");
				questionCursor = intent.getIntExtra("cursor", 0);
				gson = new Gson();
				Type questionType = new TypeToken<ArrayList<ExamQuestion>>() {
				}.getType();
				Type ruleType = new TypeToken<ArrayList<ExamRule>>() {
				}.getType();
				questionList = gson.fromJson(intent.getStringExtra("questionListJson"), questionType);
				ruleList = gson.fromJson(ruleListJson, ruleType);
				if (dao == null) dao = new PaperDao(QuestionDoExamActivity2.this);
				if (favor == null) favor = new ExamFavor(username, paperid);
				favorQids = dao.findFavorQids(username, paperid);
				if ("DoExam".equals(action)) {
					record = dao.insertRecord(new ExamRecord(paperid, username));
					isDone = StringUtils.isEmpty(record.getIsDone())? new SparseBooleanArray() : gson.fromJson(record.getIsDone(), SparseBooleanArray.class);
					String tempAnswer = record.getTempAnswer();
					if (tempAnswer == null) {
						answerBuf = new StringBuffer();
						txtAnswerBuf = new StringBuffer();
					} else if (tempAnswer.indexOf("   ") == -1) {
						answerBuf = new StringBuffer(tempAnswer);
						txtAnswerBuf = new StringBuffer();
					} else {
						answerBuf = new StringBuffer(tempAnswer.substring(0, tempAnswer.indexOf("   ")));
						txtAnswerBuf = new StringBuffer(tempAnswer.substring(tempAnswer.indexOf("   ") + 3));
					}
					initQuestionAnswer(tempAnswer);
				}
				handler.sendEmptyMessage(1);
			};
		}.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.previousBtn:
				preQuestion();
				break;
			case R.id.nextBtn:
				nextQuestion();
				break;
			case R.id.favoriteBtn:
				favorQuestion();
				break;
			case R.id.exitExamImgBtn:
				if ("DoExam".equals(action)) {
					showDialog();
				} else {
					this.finish();
				}
				break;
			case R.id.exitExamBtn:
				exitExam();
				break;
			case R.id.exitCancelExamBtn:
				this.exitDialog.dismiss();
				break;
			case R.id.exitSubmitExamBtn:
				submitExam();
			case R.id.ruleTypeLayout:
				if (ruleList != null && ruleList.size() > 0) {
					showWindow(v);
				}
				break;
			case R.id.notebook_ImgBtn:
				showNoteBookActivity();
				break;
			case R.id.selectTopicId_ImgBtn:
				gotoChooseActivity();
				break;
			case R.id.answerBtn:
				submitOrSeeAnswer();
				break;
			case R.id.removeBtn:
				removeFromErrors();
				break;
		}
	}

	private void removeFromErrors() {
		currentQuestion = questionList.get(questionCursor);
		dao.deleteError(username, currentQuestion.getQid());
		Toast.makeText(this, "移除成功,下次不再显示", Toast.LENGTH_SHORT).show();
	}

	private void favorQuestion() {
		currentQuestion = questionList.get(questionCursor);
		String qid = currentQuestion.getQid();
		favor.setQid(qid);
		if ("myFavors".equals(action)) {
			// 表示已经收藏了,现在要取消收藏
			if (favorQids.indexOf(qid) == -1) {
				Toast.makeText(this, "已经取消", Toast.LENGTH_SHORT).show();
				return;
			}
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
			dao.deleteFavor(favor);
			favorQids.replace(favorQids.indexOf(qid), favorQids.indexOf(qid) + qid.length() + 1, "");
			Toast.makeText(this, "取消成功,下次不再显示", Toast.LENGTH_SHORT).show();
			return;
		}
		if (favorQids.indexOf(qid) != -1) {
			Toast.makeText(this, "已经收藏", Toast.LENGTH_SHORT).show();
			return;
		} else {
			// 没收藏,要收藏
			this.favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
			dao.insertFavor(favor);
			favorQids.append(qid).append(",");
			Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
		}
	}

	private void submitOrSeeAnswer() {
		if ("DoExam".equals(action)) {
			// showDialog();
			submitExam();
		} else {
			// questionAdapter.showAnswer();
			// questionAdapter.showAnswer(questionList.get(questionCursor));
			// QuestionAdapter.ViewHolder holder = (ViewHolder)
			// viewFlow.getSelectedView().getTag();
			// if(holder.examAnswerLayout.getVisibility()==View.VISIBLE)
			// holder.examAnswerLayout.setVisibility(View.GONE);
			// else
			// holder.examAnswerLayout.setVisibility(View.VISIBLE);
			QuestionAdapter2.AnswerViewHolder holder = (AnswerViewHolder) viewFlow.getSelectedView().getTag(R.id.tag_second);
			if ("myErrors".equals(action))// &&("2".equals(currentQuestion.getQType())||"3".equals(currentQuestion.getQType())))
			{
				QuestionAdapter2.ContentViewHolder contentHolder = (ContentViewHolder) viewFlow.getSelectedView().getTag(R.id.tag_first);
				if (!currentQuestion.getAnswer().equals(currentQuestion.getUserAnswer())) {
					ExamErrorQuestion error = new ExamErrorQuestion(currentQuestion.getQid(), username, paperid);
					dao.insertError(error);
				}
				contentHolder.examOption.forbidden(false);
				contentHolder.examOption.setFontColor(getResources().getColor(R.color.green),currentQuestion.getAnswer());
				questionAdapter.showAnswer(holder, currentQuestion,currentQuestion.getUserAnswer());
			}
			if (holder.examAnswerLayout.getVisibility() == View.VISIBLE)
				holder.examAnswerLayout.setVisibility(View.GONE);
			else
				holder.examAnswerLayout.setVisibility(View.VISIBLE);
		}
	}

	private void gotoChooseActivity() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// 绑数据
		if ("DoExam".equals(action)) {
			mIntent.putExtra("action", "chooseQuestion");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("isDone", gson.toJson(isDone));
		} else {
			mIntent.putExtra("action", "otherChooseQuestion");
			mIntent.putExtra("questionList", gson.toJson(questionList));
		}
		this.startActivityForResult(mIntent, 1);
	}

	private void gotoChooseActivity2() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// 绑数据
		mIntent.putExtra("action", "submitPaper");
		mIntent.putExtra("questionList", gson.toJson(questionList));
		mIntent.putExtra("paperScore", paperScore);
		mIntent.putExtra("paperTime", time / 60);
		mIntent.putExtra("username", username);
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("useTime", record.getUseTime());
		mIntent.putExtra("isDone", gson.toJson(isDone));
		mIntent.putExtra("userScore", record.getScore()); // 本次得分
		mIntent.putExtra("hasDoneNum", isDone.size()); // 做了多少题
		this.startActivityForResult(mIntent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (20 == resultCode) {
			// 更换试题,当前试题
			String ruleTitle = data.getStringExtra("ruleTitle");
			this.examTypeTextView.setText(ruleTitle);
			questionCursor = data.getIntExtra("cursor", 0);
			action = data.getStringExtra("action");
			// 更新
			data.setAction(action);
			System.out.println("data.getAction = " + action);
			questionAdapter.notifyDataSetChanged();
			viewFlow.setSelection(questionCursor);
		} else if (30 == resultCode) {
			action = "DoExam";
			questionCursor = 0;
			record.setTempAnswer("");
			record.setIsDone("");
			answerBuf.delete(0, answerBuf.length());
			txtAnswerBuf.delete(0, txtAnswerBuf.length());
			isDone.clear();
			setNull4UserAnswer();
		} else if (0 == resultCode) {
			this.finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showNoteBookActivity() {
		Intent mIntent = new Intent(this, QuestionWriteNoteActivity.class);
		// 绑数据,当前的试题的id,username
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("qid", questionList.get(questionCursor).getQid());
		mIntent.putExtra("classid", questionList.get(questionCursor).getQid());
		mIntent.putExtra("username", username);
		this.startActivity(mIntent);

	}

	private void preQuestion() {
		if (questionCursor == 0) {
			Toast.makeText(this, "已经是第一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		questionAdapter.clearCheck();
		questionCursor--;
		viewFlow.setSelection(questionCursor);
	}

	private void nextQuestion() {
		if (questionCursor == questionList.size() - 1) {
			Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		questionAdapter.clearCheck();
		questionCursor++;
		viewFlow.setSelection(questionCursor);
	}

	// 保存选择题(单选和多选)答案
	public void saveChoiceAnswer(String abcd) // 1001-A&1002-B&
	{
		if (!"DoExam".equals(action)) {
			currentQuestion.setUserAnswer("".equals(abcd) ? null : abcd); // 保存学员答案
			if ("myErrors".equals(action))// &&("4".equals(currentQuestion.getQType())||"1".equals(currentQuestion.getQType())))
			{
				// 显示答案
				QuestionAdapter2.ContentViewHolder contentHolder = (ContentViewHolder) viewFlow.getSelectedView().getTag(R.id.tag_first);
				QuestionAdapter2.AnswerViewHolder answerHolder = (AnswerViewHolder) viewFlow.getSelectedView().getTag(R.id.tag_second);
				// questionAdapter.setRadioEnable(contentHolder.examOption,
				// false);
				contentHolder.examOption.forbidden(false);
				contentHolder.examOption.setFontColor(QuestionDoExamActivity2.this.getResources().getColor(R.color.green), currentQuestion.getAnswer());
				questionAdapter.showAnswer(answerHolder, currentQuestion, abcd);
				answerHolder.examAnswerLayout.setVisibility(View.VISIBLE);
				if (!currentQuestion.getAnswer().equals(currentQuestion.getUserAnswer())) {
					ExamErrorQuestion error = new ExamErrorQuestion(currentQuestion.getQid(), username, paperid);
					dao.insertError(error);
					// tOrF[questionCursor] = -1;
				} else {
					// tOrF[questionCursor] = 1;
				}
				return;
			} else
				return;
		}
		currentQuestion = questionList.get(questionCursor);
		// 判断题改变答案 A为对的,B为错的
		if ("4".equals(currentQuestion.getQType())) {
			abcd = "A".equals(abcd) ? "T" : "F";
		}
		String str = currentQuestion.getQid() + "-";
		if (answerBuf.indexOf(str) == -1) {
			answerBuf.append(str + abcd).append("&");
			isDone.append(questionCursor, true);
		} else {
			String left = answerBuf.substring(0, answerBuf.indexOf(str));
			String temp = answerBuf.substring(answerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("&") + 1);
			if ("".equals(abcd)) // 多选题,没有选答案
			{
				// 从答案里去除
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(right);
				isDone.delete(questionCursor);
			} else {
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(str).append(abcd).append("&").append(right);
				isDone.append(questionCursor, true);
			}
		}
		System.out.println("answerBuf = " + answerBuf.toString());
		record.setTempAnswer(answerBuf.toString()
				+ (txtAnswerBuf.length() == 0 ? "" : "   "
						+ txtAnswerBuf.toString()));
		// 每做完5道题自动保存答案
		if (answerBuf.toString().split("&").length % 5 == 0) {
			record.setIsDone(gson.toJson(isDone));
			dao.updateTempAnswerForRecord(record);
		}
		currentQuestion.setUserAnswer("".equals(abcd) ? null : abcd); // 保存学员答案
		if ("4".equals(currentQuestion.getQType())||"1".equals(currentQuestion.getQType())) {
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					handler.sendEmptyMessage(33);
				}
			}, 500);
		}
	}

	// 保存问答题答案
	public void saveTextAnswer(String txtAnswer) {
		if (!"DoExam".equals(action)) {
			return; // 非考试不必保存答案
		}
		String str = currentQuestion.getQid() + "-";
		if ("".equals(txtAnswer.trim())) {
			Toast.makeText(this, "请填写答案", Toast.LENGTH_LONG).show();
			return;
		}
		if (txtAnswerBuf.indexOf(str) == -1) {
			txtAnswerBuf.append(str + txtAnswer.replace("\\s", "")).append("   ");
		} else {
			String left = txtAnswerBuf.substring(0, txtAnswerBuf.indexOf(str));
			String temp = txtAnswerBuf.substring(txtAnswerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("   ") + 3);
			txtAnswerBuf.delete(0, txtAnswerBuf.length()).append(left).append(str).append(txtAnswer).append("   ").append(right);
		}
		isDone.append(questionCursor, true);
		currentQuestion.setUserAnswer(txtAnswer);
		record.setTempAnswer(answerBuf.toString() + "   " + txtAnswerBuf.toString());
		record.setIsDone(gson.toJson(isDone));
		dao.updateTempAnswerForRecord(record);
		Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
	}

	// 交卷,评判分
	private void submitPaper() {
		/**
		 * 
		 */
		if (record.getTempAnswer() == null || "".equals(record.getTempAnswer().trim())) {
			Toast.makeText(this, "还没做交毛卷啊", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			double score = 0; // 总分
			double score1 = 0; // 答错扣分的情况
			double score2 = 0; // 计算大题的临时变量
			StringBuffer buf = new StringBuffer();
			StringBuffer scoreBuf = new StringBuffer("eachScore&");
			for (int k = 0; k < ruleList.size(); k++) // 循环大题
			{
				ExamRule r = ruleList.get(k);
				double fen = r.getScoreForEach();// 每题的分数
				String fenRule = r.getScoreSet();// 判分规则 0|N表示每题多少分就是多少分，
													// 1|N,表示答对一个选项得N分，全部答对得该题的满分
													// 2|N,表示打错扣N分,最少得0分
				for (int j = 0; j < questionList.size(); j++) // 循环题目
				{
					ExamQuestion q = questionList.get(j);
					double tempScore = 0;
					if (q.getRuleId().equals(r.getRuleId())) // 属于该大题的题目，按该规则进行判分
					{
						System.out.println(q.getAnswer() + ", userAnswer:" + q.getUserAnswer());
						if (fenRule.startsWith("0|")){ // 答错不扣分，全对才得满分
							if (q.getAnswer().equals(q.getUserAnswer())) {
								score = score + fen; // 得分
								tempScore = fen;
							}
						} else if (fenRule.startsWith("1|")){// 答对一个选项得多少分
							String answer = q.getAnswer();
							String userAnswer = q.getUserAnswer() == null ? "@"
									: q.getUserAnswer();
							if (answer.contains(userAnswer)) { // 包含答案算分
								if (answer.equals(userAnswer)) {
									score = score + fen;
									tempScore = fen;
								} else {
									String[] ua = userAnswer.split("[,]"); // 少选得分，是每个选项的得分还是只要是少选就得多少分
									double fen1 = Double.parseDouble(fenRule.split("[|]")[1]) * ua.length;
									score = score + fen1;
									tempScore = fen1;
								}
							}
						} else if (fenRule.startsWith("2|")){// 答错扣分
							if (q.getAnswer().equals(q.getUserAnswer())){ // 答对
								score1 += Double.parseDouble(fenRule.split("[|]")[1]);
								tempScore = Double.parseDouble(fenRule.split("[|]")[1]);
							} else{ // 答错
								score1 -= Double.parseDouble(fenRule.split("[|]")[1]);
								tempScore = 0 - Double.parseDouble(fenRule.split("[|]")[1]);
							}
						}
						scoreBuf.append(r.getRuleId()).append("-")
								.append(q.getQid()).append("-")
								.append(tempScore).append("&"); // 每道题的得分
					}
				}
				// 每大题得分
				if (fenRule.startsWith("2|")) {
					buf.append(r.getRuleId());
					buf.append("=");
					buf.append(score1 > 0 ? score1 : 0);
					buf.append(";");
				} else {
					buf.append(r.getRuleId());
					buf.append("=");
					score2 = score - score2;
					buf.append(score2);
					buf.append(";");
					score2 = score;
				}
			}
			score = score1 > 0 ? (score + score1) : score;
			// 学员答案存进去
			record.setScore(score);
			System.out.println("scoreBuf = " + scoreBuf.toString());
			// 更新record记录
			// record.setRcdScoreForEachQuestion(scoreBuf.toString());//每题的得分情况
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void showWindow(View parent) {
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.popupwindow_rule_layout, null);
			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// 加载数据
			PopRuleListAdapter groupAdapter = new PopRuleListAdapter(this, ruleList);
			lv_group.setAdapter(groupAdapter);
			// 创建一个PopuWidow对象
			WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

			int width = (int) (wm.getDefaultDisplay().getWidth() / 2.4);
//			int height = (int) (wm.getDefaultDisplay().getHeight() / 3.2);
			popupWindow = new PopupWindow(view, width, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
		int xPos = windowManager.getDefaultDisplay().getWidth() / 2 - popupWindow.getWidth() / 2;

		// Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
		// + windowManager.getDefaultDisplay().getWidth() / 2);
		// //
		// Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() /
		// 2);
		//
		// Log.i("coder", "xPos:" + xPos);

		popupWindow.showAsDropDown(parent, xPos, -5);

		lv_group.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				// 切题,改变大题名称,切到该大题第一题
				// 当前大题
				ExamRule rule = QuestionDoExamActivity2.this.ruleList.get(position);
				System.out.println("postion = "+position+" rule = "+rule.getRuleTitle());
				int questionPosition = 0;
				for (int i = position - 1; i >= 0; i--) {
					questionPosition += QuestionDoExamActivity2.this.ruleList.get(i).getQuestionNum();
				}
				QuestionDoExamActivity2.this.examTypeTextView.setText(rule.getRuleTitle());
				QuestionDoExamActivity2.this.questionAdapter.clearCheck();
				QuestionDoExamActivity2.this.questionCursor = questionPosition; // cursor从0开始
				QuestionDoExamActivity2.this.viewFlow.setSelection(questionCursor);
				if (popupWindow != null) {
					popupWindow.dismiss();
				}
			}
		});
	}

	// 退出考试(不交卷直接退出)
	private void exitExam() {
		// 更新一次record
		timerFlag = false;
		record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date()));
		record.setTempTime(this.paperTime);
		record.setIsDone(gson.toJson(isDone));
		dao.saveOrUpdateRecord(record);
		this.exitDialog.dismiss();
		this.finish();
	}

	// 交卷
	private void submitExam() {
		if (this.exitDialog != null && this.exitDialog.isShowing()) {
			this.exitDialog.dismiss();
		}
		if (record.getTempAnswer() == null || "".equals(record.getTempAnswer().trim())) {
			Toast.makeText(this, "还没做交毛卷啊", Toast.LENGTH_SHORT).show();
			return;
		}
		timerFlag = false;
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "正在交卷...", true, false);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				submitPaper();// 交卷
				// 更新记录,转到 选题界面
				record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date()));
				record.setAnswers(record.getTempAnswer());
				record.setTempAnswer(null);
				record.setIsDone(null);
				record.setTempTime(0);
				record.setUseTime((time - paperTime) < 60 ? 1 : (time - paperTime) / 60);
				dao.saveOrUpdateRecord(record);
				timeHandler.sendEmptyMessage(10);
			};
		}.start();
	}

	// 按返回键,提示
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getRepeatCount() == 0)) {
			if ("DoExam".equals(action)) {
				showDialog();
				return true;
			}
		}
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	private void showDialog() {
		if (exitDialog == null) {
			View v = LayoutInflater.from(this).inflate(R.layout.exit_layout, null);
			Button exitBtn = (Button) v.findViewById(R.id.exitExamBtn);
			Button submitBtn = (Button) v.findViewById(R.id.exitSubmitExamBtn);
			Button cancelBtn = (Button) v.findViewById(R.id.exitCancelExamBtn);
			AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			localBuilder.setTitle("退出").setMessage("是否退出考试").setView(v);
			exitDialog = localBuilder.create();
			exitBtn.setOnClickListener(this);
			submitBtn.setOnClickListener(this);
			cancelBtn.setOnClickListener(this);
		}
		exitDialog.show();
	}

	@Override
	protected void onStart() {
		if ("DoExam".equals(action)) {
			// timerFlag = true;
			// new TimerThread().start();
			this.answerBtn.setImageResource(R.drawable.exam_submit_img);
		} else if ("showQuestionWithAnswer".equals(action)) {
			this.examTitle.setText(this.papername);
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
			// this.answerBtn.setVisibility(View.INVISIBLE); //仍然占据位置
			this.ruleTypeLayout.setOnClickListener(this);
		} else {
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
		}
		super.onStart();
	}
//	private void secondInit()
//	{
//		if ("DoExam".equals(action)) {
//			// timerFlag = true;
//			// new TimerThread().start();
//			this.answerBtn.setImageResource(R.drawable.exam_submit_img);
//			// this.examAnswerLayout.setVisibility(View.GONE);
//			if (ruleList != null && ruleList.size() > 0) {
//				currentRule = ruleList.get(0);
//				this.examTypeTextView.setText(currentRule.getRuleTitle()); // 大题名字
//				this.ruleTypeLayout.setOnClickListener(this);
//				viewFlow.setSelection(questionCursor);
//			} else {
//				this.nodataLayout.setVisibility(0);
//			}
//		} else if ("showQuestionWithAnswer".equals(action)) {
//			this.examTitle.setText(this.papername);
//			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
//			// this.answerBtn.setVisibility(View.INVISIBLE); //仍然占据位置
//			this.ruleTypeLayout.setOnClickListener(this);
//			viewFlow.setSelection(questionCursor);
//		} else {
//			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
//			viewFlow.setSelection(questionCursor);
//		}
//	}
	@Override
	protected void onResume() {
		if ("DoExam".equals(action)) {
			timerFlag = true;
			new TimerThread().start();
		}
		super.onResume();
		// MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		timerFlag = false;
		super.onPause();
		// MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		timerFlag = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (exitDialog != null) {
			exitDialog.dismiss();
		}
		super.onDestroy();
	}

	private static class TimerHandler extends Handler {
		private WeakReference<QuestionDoExamActivity2> weak;

		public TimerHandler(QuestionDoExamActivity2 a) {
			this.weak = new WeakReference<QuestionDoExamActivity2>(a);
		}

		@Override
		public void handleMessage(Message msg) {
			QuestionDoExamActivity2 theActivity = weak.get();
			switch (msg.what) {
				case 1:
					theActivity.paperTime--;
					theActivity.timeCountDown
							.setText(getTimeText(theActivity.paperTime));
					if (theActivity.paperTime == 0) {
						// 交卷
						timerFlag = false;
						Toast.makeText(theActivity, "Time Over", Toast.LENGTH_LONG)
								.show();
						theActivity.submitExam();
					}
					break;
				case 10:
					if (theActivity.proDialog != null) {
						theActivity.proDialog.dismiss();
					}
					theActivity.gotoChooseActivity2();
					break;
			}
		}

		private String getTimeText(int count) {
			int h = count / 60 / 60;
			int m = count / 60 % 60;
			int s = count % 60;
			return (h > 0 ? h : 0) + ":" + (m > 9 ? m : "0" + m) + ":"
					+ (s > 9 ? s : "0" + s);
		}
	}

	private class TimerThread extends Thread {
		@Override
		public void run() {
			while (timerFlag) {
				timeHandler.sendEmptyMessage(1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initQuestionAnswer(String tempAnswer) {
		if (tempAnswer == null || "".equals(tempAnswer.trim())) {
			return;
		}
		int listSize = questionList.size();
		String choiceAnswer = null, textAnswer = null;
		if (tempAnswer.indexOf("   ") != -1) {
			choiceAnswer = tempAnswer.substring(0, tempAnswer.indexOf("   "));
			textAnswer = tempAnswer.substring(tempAnswer.indexOf("   ") + 3);
		} else {
			choiceAnswer = tempAnswer;
		}
		for (int i = 0; i < listSize; i++) {
			ExamQuestion q = questionList.get(i);
			String str = q.getQid() + "-";
			if ((!"问答题".equals(q.getQType())) && choiceAnswer.indexOf(str) != -1) {
				String temp = choiceAnswer.substring(choiceAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("&")));
			} else if (textAnswer != null && "问答题".equals(q.getQType()) && textAnswer.indexOf(str) != -1) {
				String temp = textAnswer.substring(textAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("   ")));
			}
		}
	}

	private void setNull4UserAnswer() {
		for (ExamQuestion q : questionList) {
			q.setUserAnswer(null);
		}
	}

	@SuppressWarnings("deprecation")
	private void openPopupwin() {
		guidefile = this.getSharedPreferences("guidefile", 0);
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.pop_doexam_tips, null, true);
		tipWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
		tipWindow.setFocusable(true);
		tipWindow.setBackgroundDrawable(new BitmapDrawable());
		tipWindow.setAnimationStyle(R.style.AnimationFade);

		/***************** 以下代码用来循环检测activity是否初始化完毕 ***************/
		Runnable showPopWindowRunnable = new Runnable() {
			@Override
			public void run() {
				// 得到activity中的根元素
				View view = findViewById(R.id.parent);
				// 如何根元素的width和height大于0说明activity已经初始化完毕
				if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
					// 显示popwindow
					tipWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
					// 停止检测
					mHandler.removeCallbacks(this);
				} else {
					// 如果activity没有初始化完毕则等待5毫秒再次检测
					mHandler.postDelayed(this, 5);
				}
			}
		};
		// 开始检测
		mHandler.post(showPopWindowRunnable);
		/****************** 以上代码用来循环检测activity是否初始化完毕 *************/
	}

	public void disPopupWin(View v) {
		if (tipWindow != null && tipWindow.isShowing())
			tipWindow.dismiss();
		SharedPreferences.Editor editor = guidefile.edit();
		if (guidefile.contains("isFirstExam")) {
			editor.remove("isFirstExam");
		}
		editor.putInt("isFirstExam", 1);
		editor.commit();
	}

	public String getAction() {
		return action;
	}

	static class MyHandler extends Handler {
		WeakReference<QuestionDoExamActivity2> weak;
		public MyHandler(QuestionDoExamActivity2 context) {
			weak = new WeakReference<QuestionDoExamActivity2>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			QuestionDoExamActivity2 q2 = weak.get();
			switch(msg.what)
			{
				case 1:
					q2.questionAdapter = new QuestionAdapter2(q2, q2, q2.questionList,q2.username, q2.paperid);
					q2.viewFlow.setAdapter(q2.questionAdapter);
					if("DoExam".equals(q2.action))
					{
						q2.examTitle.setText(q2.papername); // 试卷名字
						q2.answerBtn.setImageResource(R.drawable.exam_submit_img);
						// this.examAnswerLayout.setVisibility(View.GONE);
						if (q2.ruleList != null && q2.ruleList.size() > 0) {
							q2.currentRule = q2.ruleList.get(0);
							q2.examTypeTextView.setText(q2.currentRule.getRuleTitle()); // 大题名字
							q2.ruleTypeLayout.setOnClickListener(q2);
							q2.viewFlow.setSelection(q2.questionCursor);
						} else {
							q2.nodataLayout.setVisibility(0);
						}
					}
					q2.loadingLayout.setVisibility(View.GONE);
					int firstExam = q2.guidefile.getInt("isFirstExam", 0);
					if (firstExam == 0) {
						q2.openPopupwin();
					}
					break;
				case 33:
					if(q2.questionCursor < q2.questionList.size()-1)
					{
						q2.viewFlow.setSelection(++q2.questionCursor);
					}
					break;
			}
		}
	}
	public void writeNote()
	{
		showNoteBookActivity();
	}
}
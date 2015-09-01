package com.examw.netschool;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.adapter.PopRuleListAdapter;
import com.examw.netschool.customview.CheckBoxGroup;
import com.examw.netschool.customview.MyCheckBox;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamFavor;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRecord;
import com.examw.netschool.entity.ExamRule;
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
public class QuestionDoExamActivity extends Activity implements OnClickListener, OnGestureListener {
	// 组件
	private ImageButton exitExamImgBtn, notebookImgBtn, nextBtn, preBtn,
			removeBtn, answerBtn, favoriteBtn;
	private TextView timeCountDown, examTitle, examTypeTextView,
			myAnswerTextView, sysAnswerTextView, analysisTextView;
	private ImageView answerResultImg;
	private Button chooseQuestionBtn, submitExamBtn;
	private GestureDetector mGestureDetector;
	private ScrollView scrollView;
	private LinearLayout nodataLayout, loadingLayout, ruleTypeLayout,
			modeLayout1, modeLayout2, modeLayout3, modeLayout4;
	private TextView examContent1, examContent2, examContent3;
	private EditText answerEditText;
	private CheckBoxGroup examOption2;
	private LinearLayout examAnswerLayout, /*examAnswerLayout2,*/ examImages1,
			examImages2, examImages3/*, examAnswerLayout3*/;
	private RadioGroup examOption1;
	private Handler timeHandler;
	// 数据
	private String papername, username;
	private String paperid;
	private String action;
	private String ruleListJson;
	private StringBuffer favorQids;
	private int paperTime, time, paperScore;
	private List<ExamRule> ruleList;
	private List<ExamQuestion> questionList;
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
	private SharedPreferences guidefile;
	// 数据库操作
	private PaperDao dao;
	// 图片保存目录
	private String imageSavePath;
	private ProgressDialog proDialog;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doexam);
		// 数据初始化
		Intent intent = this.getIntent();
		this.paperid = intent.getStringExtra("paperId");
		this.papername = intent.getStringExtra("paperName");
		this.ruleListJson = intent.getStringExtra("ruleListJson");
		this.username = intent.getStringExtra("username");
		this.paperTime = intent.getIntExtra("tempTime", 0);
		this.time = intent.getIntExtra("paperTime", 0) * 60; // 秒
		this.paperScore = intent.getIntExtra("paperScore", 0);
		this.action = intent.getStringExtra("action");
		this.questionCursor = intent.getIntExtra("cursor", 0);
		// /mnt/sdcard/eschool/hahaha/image/1001
		imageSavePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "eschool" + File.separator + username
				+ File.separator + "image" + File.separator + paperid;
		gson = new Gson();
		
		Type questionType = new TypeToken<ArrayList<ExamQuestion>>() {}.getType(),
				 ruleType = new TypeToken<ArrayList<ExamRule>>() {}.getType();
				 
		this.questionList = gson.fromJson(intent.getStringExtra("questionListJson"), questionType);
		this.ruleList = gson.fromJson(ruleListJson, ruleType);
		if (dao == null) dao = new PaperDao(this);
		
		this.initView();
		if (this.favor == null) this.favor = new ExamFavor(username, paperid);
		this.favorQids = dao.findFavorQids(username, paperid);
		// 根据action的不同,区分
		if ("DoExam".equals(action)) {
			this.record = dao.insertRecord(new ExamRecord(paperid, username));
			isDone = this.record.getIsDone() == null ? new SparseBooleanArray() : gson.fromJson(this.record.getIsDone(), SparseBooleanArray.class);
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
			this.examTitle.setText(this.papername); // 试卷名字
		} else if ("myNoteBook".equals(action)) {
			this.examTitle.setText("我的笔记");
			this.examTypeTextView.setVisibility(View.GONE);
			this.chooseQuestionBtn.setVisibility(View.GONE);// 选题
		} else if ("myErrors".equals(action)) {
			this.examTitle.setText("错题集");
			this.examTypeTextView.setVisibility(View.GONE);
			this.removeBtn.setVisibility(View.VISIBLE);
		} else if ("myFavors".equals(action)) {
			this.examTitle.setText("我的收藏");
			this.examTypeTextView.setVisibility(View.GONE);
		} else if ("showNoteSource".equals(action)) {
			this.examTitle.setText("我的笔记");
			this.examTypeTextView.setVisibility(View.GONE);
		}
		mGestureDetector = new GestureDetector(this, this);
		this.preBtn.setOnClickListener(this);
		this.nextBtn.setOnClickListener(this);
		this.removeBtn.setOnClickListener(this);
		this.notebookImgBtn.setOnClickListener(this);
		this.exitExamImgBtn.setOnClickListener(this);
		this.chooseQuestionBtn.setOnClickListener(this);
		this.answerBtn.setOnClickListener(this);
		this.favoriteBtn.setOnClickListener(this);
		// 去了这个反而灵敏了
		// this.scrollView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// 
		// return mGestureDetector.onTouchEvent(event);
		// }
		// });
		this.scrollView.setFocusable(true);
		this.scrollView.setClickable(true);
		this.scrollView.setLongClickable(true);
		mGestureDetector.setIsLongpressEnabled(true);
		timeHandler = new TimerHandler(this);
		guidefile = this.getSharedPreferences("guidefile", 0);
		int firstExam = guidefile.getInt("isFirstExam", 0);
		if (firstExam == 0) {
			mHandler = new Handler();
			openPopupwin();
		}
	}

	// 取得主界面的组件,只取得不操作
	private void initView() {
		this.exitExamImgBtn = (ImageButton) this.findViewById(R.id.exitExamImgBtn);// 退出考试
		this.notebookImgBtn = (ImageButton) this.findViewById(R.id.notebook_ImgBtn);// 笔记按钮
		this.preBtn = (ImageButton) this.findViewById(R.id.previousBtn); // 上一题
		this.nextBtn = (ImageButton) this.findViewById(R.id.nextBtn); // 下一题
		this.favoriteBtn = (ImageButton) this.findViewById(R.id.favoriteBtn);
		this.removeBtn = (ImageButton) this.findViewById(R.id.removeBtn);
		this.timeCountDown = (TextView) this.findViewById(R.id.timecount_down_TextView);// 倒计时
		this.examTitle = (TextView) this.findViewById(R.id.examTitle_TextView);// 考试标题
		this.chooseQuestionBtn = (Button) this.findViewById(R.id.selectTopicId_ImgBtn);// 选题
		this.examTypeTextView = (TextView) this.findViewById(R.id.examTypeTextView);// 大题标题
		this.ruleTypeLayout = (LinearLayout) this.findViewById(R.id.ruleTypeLayout);
		this.scrollView = (ScrollView) this.findViewById(R.id.ContentscrollView);
		this.examAnswerLayout = (LinearLayout) this.findViewById(R.id.exam_answer_layout);
		this.submitExamBtn = (Button) this.findViewById(R.id.submitExamBtn); // 提交答案
		this.answerBtn = (ImageButton) this.findViewById(R.id.answerBtn);// 交卷或者查看答案
		this.analysisTextView = (TextView) this.findViewById(R.id.exam_analysisTextView);
		this.myAnswerTextView = (TextView) this.findViewById(R.id.myAnswerTextView);
		this.sysAnswerTextView = (TextView) this.findViewById(R.id.sysAnswerTextView);
		this.answerResultImg = (ImageView) this.findViewById(R.id.answerResultImg);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.nodataLayout.setVisibility(8);
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.loadingLayout.setVisibility(8);
		// this.contentLayout = (LinearLayout) this
		// .findViewById(R.id.examContentLayout);
		this.modeLayout1 = (LinearLayout) this.findViewById(R.id.doexam_mode1layout);
		initModeLayout1();
		this.modeLayout2 = (LinearLayout) this.findViewById(R.id.doexam_mode2layout);
		initModeLayout2();
		this.modeLayout2.setVisibility(8);
		this.modeLayout3 = (LinearLayout) this.findViewById(R.id.doexam_mode3layout);
		initModeLayout3();
		this.modeLayout3.setVisibility(8);
		this.modeLayout4 = (LinearLayout) this.findViewById(R.id.doexam_mode4layout);
		this.modeLayout4.setVisibility(8);
	}

	// 单选题的布局
	private void initModeLayout1() {
		this.examContent1 = ((TextView) findViewById(R.id.exam_Content)); // exam_Content1
		// /
		this.examImages1 = (LinearLayout) findViewById(R.id.examImages1);

		this.examOption1 = ((RadioGroup) findViewById(R.id.examOption)); // examOption1
		// this.examListView1 = ((ListView) findViewById(R.id.exam_ListView1));
		// // exam_ListView1
	}

	// 多选题
	private void initModeLayout2() {
		this.examContent2 = (TextView) this.findViewById(R.id.exam_Content2);// 题目内容
		this.examOption2 = (CheckBoxGroup) this.findViewById(R.id.examOption2);// checkbox组的容器
//		this.examAnswerLayout2 = (LinearLayout) this.findViewById(R.id.exam_answer_layout2);
		this.examImages2 = (LinearLayout) findViewById(R.id.examImages2);
		// this.examAnswerLayout2.setVisibility(8);
	}

	// 问答题
	private void initModeLayout3() {
		this.examContent3 = (TextView) this.findViewById(R.id.exam_Content3);
		this.answerEditText = (EditText) this.findViewById(R.id.exam_answerEditText);
		//this.examAnswerLayout3 = (LinearLayout) this.findViewById(R.id.exam_answer_layout3);
		this.examImages3 = (LinearLayout) findViewById(R.id.examImages3);
		// this.examAnswerLayout3.setVisibility(8);
	}

	private void showContent() {
		currentQuestion = questionList.get(questionCursor);
		if (ruleList != null && ruleList.size() > 0) {
			currentRule = ruleList.get(ruleList.indexOf(new ExamRule(currentQuestion.getRuleId(), currentQuestion.getPaperId())));
			this.examTypeTextView.setText(currentRule.getRuleTitle());
		}
		this.examImages1.removeAllViews();
		String type = currentQuestion.getQType();
		String answer = currentQuestion.getUserAnswer();
		// String str = currentQuestion.getQid()+"-";
		// String answer = null;
		// String tempAnswer = record.getTempAnswer();
		// if(tempAnswer!=null&&tempAnswer.indexOf(str)!=-1)
		// {
		// String temp = tempAnswer.substring(tempAnswer.indexOf(str));
		// if("问答题".equals(type))
		// {
		// answer = temp.substring(str.length(),temp.indexOf("   "));
		// }else
		// answer= temp.substring(str.length(),temp.indexOf("&"));
		// }
		if ("单选题".equals(type)) {
			this.modeLayout1.setVisibility(0);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("[A-Z][.．、)]", "@@@").split("@@@");
			String title = arr[0];
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			this.showPics(title, imageSavePath, zuheName, examImages1, examContent1);
			// this.examOption1.clearCheck();
			if (this.examOption1.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < this.examOption1.getChildCount(); j++) {
					this.examOption1.removeViewAt(j);
				}
			}
			for (int i = 1; i < arr.length; i++) {
				int viewCount = this.examOption1.getChildCount();
				RadioButton rb;
				if (i > viewCount) {
					rb = new RadioButton(this);
					rb.setId(i);
					rb.setTextColor(getResources().getColor(R.color.black));
					rb.setButtonDrawable(R.drawable.radio_btn_img);
					this.examOption1.addView(rb, i - 1);
				}
				rb = (RadioButton) this.examOption1.getChildAt(i - 1);
				rb.setText((char) (64 + i) + "．" + arr[i]);
				if (answer != null && answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					rb.setChecked(true);
				}
			}
			this.examOption1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					int id = examOption1.getCheckedRadioButtonId();
					if (id == -1) return;
					saveChoiceAnswer(((char) (64 + id)) + "");
				}
			});
		} else if ("多选题".equals(type)) {
			this.modeLayout1.setVisibility(8);
			this.modeLayout2.setVisibility(0);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("[A-Z][.．、)]", "@@@").split("@@@");
			String title = arr[0];
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(title, imageSavePath, zuheName, examImages2, examContent2);
			// 显示选项
			if (this.examOption2.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < this.examOption2.getChildCount(); j++) {
					this.examOption2.removeViewAt(j);
				}
			}
			// this.examOption2.clearCheck();
			for (int i = 1; i < arr.length; i++) {
				int viewCount = this.examOption2.getChildCount();
				MyCheckBox cb;
				if (i > viewCount) {
					cb = new MyCheckBox(this);
					cb.setTextColor(getResources().getColor(R.color.black));
					cb.setButtonDrawable(R.drawable.checkbox_button_img);
					cb.setValue(String.valueOf((char) (64 + i)));
					this.examOption2.addView(cb, i - 1);
				}
				cb = (MyCheckBox) this.examOption2.getChildAt(i - 1);
				cb.setText((char) (64 + i) + "．" + arr[i]);
				if (answer != null && answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					cb.setChecked(true);
				}
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						MyCheckBox mcb = (MyCheckBox) buttonView;
						if (mcb.getFlag() == -1) {
							mcb.setFlag(0);
							return;
						}
						String s = examOption2.getValue();
						saveChoiceAnswer(s);
					}
				});
			}
		} else if ("判断题".equals(type)) {
			this.modeLayout1.setVisibility(0);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			// this.examContent1.setText(questionCursor + 1 + "、"
			// + currentQuestion.getContent());
			// 加载图片
			String title = currentQuestion.getContent();
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(title, imageSavePath, zuheName, examImages1, examContent1);
			//
			RadioButton rb_t, rb_f;
			if (examOption1.getChildCount() == 0) {
				rb_t = new RadioButton(this);
				rb_t.setId(1);
				rb_f = new RadioButton(this);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setTextColor(getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ×");
				rb_f.setTextColor(getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				this.examOption1.addView(rb_t, 0);
				this.examOption1.addView(rb_f, 1);
			}
			// this.examOption1.clearCheck();
			rb_t = (RadioButton) this.examOption1.getChildAt(0);
			rb_f = (RadioButton) this.examOption1.getChildAt(1);
			if (examOption1.getChildCount() > 2) {
				this.examOption1.removeAllViews();
				rb_t.setId(1);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setTextColor(getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ×");
				rb_f.setTextColor(getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				this.examOption1.addView(rb_t, 0);
				this.examOption1.addView(rb_f, 1);
			}
			if (answer != null) {
				if (answer.indexOf("F") != -1) {
					rb_f.setChecked(true);
					rb_t.setChecked(false);
				} else if (answer.indexOf("T") != -1) {
					rb_t.setChecked(true);
					rb_f.setChecked(false);
				} else {
					rb_t.setChecked(false);
					rb_f.setChecked(false);
				}
			}
			this.examOption1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (checkedId == -1) return;
					if (checkedId == 1){
						saveChoiceAnswer("T");
					}else{
						saveChoiceAnswer("F");
					}
				}
			});
		} else if ("问答题".equals(type)) {
			this.modeLayout1.setVisibility(8);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(0);
			this.modeLayout4.setVisibility(8);
			String title = currentQuestion.getContent();
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(title, imageSavePath, zuheName, examImages3, examContent3);
			if (answer != null) {
				this.answerEditText.setText(answer);
			}
			this.submitExamBtn.setVisibility(0);
			this.submitExamBtn.setOnClickListener(this);
		}
		if (!"DoExam".equals(action)) {
			String trueAnswer = currentQuestion.getAnswer();
			this.myAnswerTextView.setText(answer);
			this.sysAnswerTextView.setText(trueAnswer);
			this.analysisTextView.setText(currentQuestion.getAnalysis());
			if ("问答题".equals(type)) {
				this.answerResultImg.setVisibility(View.GONE);
			} else {
				this.answerResultImg.setVisibility(View.VISIBLE);
				if (trueAnswer.equals(answer)) {
					this.answerResultImg
							.setImageResource(R.drawable.correct_answer_pto);
				} else if (answer != null && !"".equals(answer)
						&& isContain(trueAnswer, answer)) {
					this.answerResultImg
							.setImageResource(R.drawable.halfcorrect_pto);
				} else {
					this.answerResultImg
							.setImageResource(R.drawable.wrong_answer_pto);
				}
			}
		}
		if (favorQids != null && favorQids.indexOf(currentQuestion.getQid()) != -1) {
			this.favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
		} else {
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
		}
	}

	private boolean isContain(String trueAnswer, String answer) {
		if (answer.length() == 1) {
			return trueAnswer.contains(answer);
		}
		String[] arr = answer.split(",");
		boolean flag = true;
		for (String s : arr) {
			flag = flag && trueAnswer.contains(s);
		}
		return flag;
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
			case R.id.submitExamBtn:
				saveTextAnswer();
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
			favorQids.replace(favorQids.indexOf(qid), favorQids.indexOf(qid)
					+ qid.length() + 1, "");
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
			if (this.examAnswerLayout.getVisibility() == View.GONE) {
				this.examAnswerLayout.setVisibility(View.VISIBLE);
			} else if (this.examAnswerLayout.getVisibility() == View.VISIBLE) {
				this.examAnswerLayout.setVisibility(View.GONE);
			}
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
			this.scrollView.fullScroll(33);
			showContent();
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
		mIntent.putExtra("username", username);
		this.startActivity(mIntent);
	}

	private void preQuestion() {
		this.scrollView.fullScroll(33);
		if (questionCursor == 0) {
			Toast.makeText(this, "已经是第一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		this.examOption1.clearCheck();
		this.examOption2.clearCheck();
		questionCursor--;
		showContent();
	}

	private void nextQuestion() {
		this.scrollView.fullScroll(33);
		if (questionCursor == questionList.size() - 1) {
			Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		this.examOption1.clearCheck();
		this.examOption2.clearCheck();
		questionCursor++;
		showContent();
	}

	// 保存选择题(单选和多选)答案
	private void saveChoiceAnswer(String abcd) // 1001-A&1002-B&
	{
		if (!"DoExam".equals(action)) {
			return;
		}
		String str = currentQuestion.getQid() + "-";
		if (answerBuf.indexOf(str) == -1) {
			answerBuf.append(str + abcd).append("&");
			isDone.append(questionCursor, true);
		} else {
			String left = answerBuf.substring(0, answerBuf.indexOf(str));
			String temp = answerBuf.substring(answerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("&") + 1);
			if ("".equals(abcd)){ // 多选题,没有选答案
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
		record.setTempAnswer(answerBuf.toString() + (txtAnswerBuf.length() == 0 ? "" : "   " + txtAnswerBuf.toString()));
		// 每做完5道题自动保存答案
		if (answerBuf.toString().split("&").length % 5 == 0) {
			record.setIsDone(gson.toJson(isDone));
			dao.updateTempAnswerForRecord(record);
		}
		currentQuestion.setUserAnswer("".equals(abcd) ? null : abcd); // 保存学员答案
	}

	// 保存问答题答案
	private void saveTextAnswer() {
		if (!"DoExam".equals(action)) {
			return; // 非考试不必保存答案
		}
		String str = currentQuestion.getQid() + "-";
		String txtAnswer = this.answerEditText.getText().toString();
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
							String userAnswer = q.getUserAnswer() == null ? "@" : q.getUserAnswer();
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
							if (q.getAnswer().equals(q.getUserAnswer())) // 答对
							{
								score1 = score1 + Double.parseDouble(fenRule.split("[|]")[1]);
								tempScore = Double.parseDouble(fenRule.split("[|]")[1]);
							} else{ // 答错
								score1 = score1 - Double.parseDouble(fenRule.split("[|]")[1]);
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
			System.out.println(scoreBuf.toString());
			// 更新record记录
			// record.setRcdScoreForEachQuestion(scoreBuf.toString());//每题的得分情况
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// Log.i("MyGesture", "onDown");
		// Toast.makeText(this, "onDown", Toast.LENGTH_SHORT).show();
		return true; // 事件已处理返回true
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			Log.i("MyGesture", "Fling left");
			nextQuestion();
			// Toast.makeText(this, "Fling Left", Toast.LENGTH_SHORT).show();
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling right
			Log.i("MyGesture", "Fling right");
			// Toast.makeText(this, "Fling Right", Toast.LENGTH_SHORT).show();
			preQuestion();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		 
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	 
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		 this.mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.scrollView.onTouchEvent(event);
		return false;
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
			popupWindow = new PopupWindow(view, 200, 250);
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
		Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:" + windowManager.getDefaultDisplay().getWidth() / 2);
		//
		Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() / 2);
		Log.i("coder", "xPos:" + xPos);

		popupWindow.showAsDropDown(parent, xPos, -5);
		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				// 切题,改变大题名称,切到该大题第一题
				// 当前大题
				ExamRule rule = QuestionDoExamActivity.this.ruleList.get(position);
				int questionPosition = 0;
				for (int i = position - 1; i >= 0; i--) {
					questionPosition += QuestionDoExamActivity.this.ruleList.get(i).getQuestionNum();
				}
				QuestionDoExamActivity.this.examTypeTextView.setText(rule.getRuleTitle());
				QuestionDoExamActivity.this.questionCursor = questionPosition; // cursor从0开始
				QuestionDoExamActivity.this.showContent();
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
		record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
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
		//开一个线程进行交卷  2013-09-25修改
		if(proDialog == null)
		{
			proDialog = ProgressDialog.show(this, null, "正在交卷..", true, false);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}else
		{
			proDialog.show();
		}
		new Thread(){
			public void run() {
				submitPaper();// 交卷
				timerFlag = false;
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
		if ((paramKeyEvent.getKeyCode() == 4)
				&& (paramKeyEvent.getRepeatCount() == 0)) {
			if ("DoExam".equals(action)) {
				showDialog();
				return true;
			}
		}
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	private void showDialog() {
		if (exitDialog == null) {
			View v = LayoutInflater.from(this).inflate(R.layout.exit_layout,
					null);
			Button exitBtn = (Button) v.findViewById(R.id.exitExamBtn);
			Button submitBtn = (Button) v.findViewById(R.id.exitSubmitExamBtn);
			Button cancelBtn = (Button) v.findViewById(R.id.exitCancelExamBtn);
			AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			localBuilder.setTitle("注销").setMessage("是否注销用户").setView(v);
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
			this.examAnswerLayout.setVisibility(View.GONE);
			if (ruleList != null && ruleList.size() > 0) {
				currentRule = ruleList.get(0);
				this.examTypeTextView.setText(currentRule.getRuleTitle()); // 大题名字
				this.ruleTypeLayout.setOnClickListener(this);
				showContent();
			} else {
				this.nodataLayout.setVisibility(0);
			}
		} else if ("showQuestionWithAnswer".equals(action)) {
			this.examTitle.setText(this.papername);
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
			this.examAnswerLayout.setVisibility(View.VISIBLE);
			this.ruleTypeLayout.setOnClickListener(this);
			showContent();
		} else {
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
			this.examAnswerLayout.setVisibility(View.VISIBLE);
			showContent();
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		if ("DoExam".equals(action)) {
			timerFlag = true;
			new TimerThread().start();
		}
		super.onResume();
		//MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		timerFlag = false;
		super.onPause();
		//MobclickAgent.onPause(this);
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
		this.examOption1.removeAllViews();
		this.examOption2.removeAllViews();
		super.onDestroy();
	}

	private static class TimerHandler extends Handler {
		private WeakReference<QuestionDoExamActivity> weak;

		public TimerHandler(QuestionDoExamActivity a) {
			this.weak = new WeakReference<QuestionDoExamActivity>(a);
		}

		@Override
		public void handleMessage(Message msg) {
			QuestionDoExamActivity theActivity = weak.get();
			switch (msg.what) {
			case 1:
				theActivity.paperTime--;
				theActivity.timeCountDown.setText(getTimeText(theActivity.paperTime));
				if (theActivity.paperTime == 0) {
					// 交卷
					timerFlag = false;
					Toast.makeText(theActivity, "Time Over", Toast.LENGTH_LONG)
							.show();
					theActivity.submitExam();
				}
				break;
			case 10:
				if(theActivity.proDialog != null)
				{
					theActivity.proDialog.dismiss();
				}
				theActivity.gotoChooseActivity2(); //交卷
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
		if (tempAnswer == null || "".equals(tempAnswer.trim())) return;
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

	// 解析图片下载地址
	private String[] parseAddress(String address) {
		String[] addr = null;
		// if(address.contains("<IMG "))
		// {
		String[] arr = address.split("<IMG ");
		addr = new String[arr.length];
		for (int i = 1; i < arr.length; i++) {
			String s = arr[i];
			if (!"".equals(s)) {
				String right = s.substring(s.indexOf("src=\"") + 5);
				addr[i] = right.substring(0, right.indexOf("\""));
			} else {
				addr[i] = null;
			}
		}
		// }
		return addr;
	}

	// 异步下载图片
	private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
		private String fileName;

		public GetImageTask(String fileName) {
			this.fileName = fileName;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			URL url;
			//byte[] b = null;
			try {
				this.fileName += params[0].substring(params[0].lastIndexOf("."));
				url = new URL(params[0]); // 设置URL
				HttpURLConnection con;
				con = (HttpURLConnection) url.openConnection(); // 打开连接
				con.setRequestMethod("GET"); // 设置请求方法
				// 设置连接超时时间为5s
				con.setConnectTimeout(5000);
				InputStream in = con.getInputStream(); // 取得字节输入流
				int len = 0;
				byte buf[] = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len); // 把数据写入内存
				}
				byte[] data = out.toByteArray();
				out.close(); // 关闭内存输出流
				// 二进制数据生成位图
				Bitmap bit = BitmapFactory.decodeByteArray(data, 0, data.length);
				return bit;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) return; 
			ImageView img = new ImageView(QuestionDoExamActivity.this);
			img.setScaleType(ImageView.ScaleType.FIT_START);
			examImages1.addView(img);
			try {
				img.setImageURI(Uri.parse(saveFile(result, fileName)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}

		public String saveFile(Bitmap bm, String fileName) throws IOException {
			String filePath = imageSavePath + File.separator + fileName;
			File myCaptureFile = new File(filePath);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bm.recycle();
			bos.flush();
			bos.close();
			return filePath;
		}
	}

	@SuppressWarnings("deprecation")
	private void openPopupwin() {
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
		if (tipWindow != null && tipWindow.isShowing()) tipWindow.dismiss();
		SharedPreferences.Editor editor = guidefile.edit();
		if (guidefile.contains("isFirstExam")) {
			editor.remove("isFirstExam");
		}
		editor.putInt("isFirstExam", 1);
		editor.commit();
	}

	// 显示题目中的图片
	private void showPics(String title, String imageSavePath, String zuheName, LinearLayout examImages, TextView examContent) {
		if (title.contains("<IMG ")) // 包含图片
		{
			// String s = currentRule.getRuleId()+"-"+currentQuestion.getQid();
			// 先去sd中找,找到就显示,找不到先下载再显示
			File dir = new File(imageSavePath);
			if (dir.exists()) {
				// dir.mkdirs();
				File[] files = dir.listFiles();
				int count = files.length;
				for (File f : files) {
					if (f.getName().contains(zuheName)) {
						ImageView img = new ImageView(this);
						img.setImageURI(Uri.parse(f.getPath()));
						examImages.addView(img);
						count--;
					}
				}
				if (count == files.length) // 没有图片,没考虑多图片没有加载完全的情况
				{
					String[] imageUrls = parseAddress(title);
					for (int i = 0; i < imageUrls.length; i++) {
						String url = imageUrls[i];
						if ("".equals(url) || url == null) {
							continue;
						}
						new GetImageTask(zuheName + "-" + i).execute(url);
					}
				}
			} else {
				dir.mkdirs();
				String[] imageUrls = parseAddress(title);
				for (int i = 0; i < imageUrls.length; i++) {
					String url = imageUrls[i];
					if ("".equals(url)) {
						continue;
					}
					new GetImageTask(zuheName + "-" + i).execute(url);
				}
			}
			examContent.setText(questionCursor + 1 + "、"
					+ title.replaceAll("<IMG[\\S\\s]+>", ""));
		} else
			examContent.setText(questionCursor + 1 + "、" + title);
	}
}
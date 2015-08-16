package com.examw.netschool.adapter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.QuestionDoExamActivity2;
import com.examw.netschool.R;
import com.examw.netschool.customview.CheckBoxGroup2;
import com.examw.netschool.customview.OptionLayout;
import com.examw.netschool.entity.ExamQuestion;

public class QuestionAdapter2 extends BaseAdapter {
	private Context context;
	private QuestionDoExamActivity2 activity2;
//	private QuestionDoExamActivity1 activity1;
//	private BaseActivity activity;
	private ArrayList<ExamQuestion> questionList;
	private ContentViewHolder contentHolder;
	// 图片保存目录
	private String imageSavePath;

	private static TextViewLongClickListener tvLongClickListener;
	private static NoteBtnLinstener noteBtnListener;

	public QuestionAdapter2(Context context, QuestionDoExamActivity2 activity, ArrayList<ExamQuestion> questionList,String username, String paperid) {
		this.context = context;
		// /mnt/sdcard/eschool/hahaha/image/1001
		this.imageSavePath = Environment.getExternalStorageDirectory()
				.getPath()
				+ File.separator
				+ "eschool"
				+ File.separator
				+ username
				+ File.separator
				+ "image"
				+ File.separator
				+ "question";
		this.activity2 = activity;
//		if(activity instanceof QuestionDoExamActivity2)
//			this.activity2 = (QuestionDoExamActivity2) activity;
//		else if(activity instanceof QuestionDoExamActivity1)
//			this.activity1 = (QuestionDoExamActivity1) activity;
		this.questionList = questionList;
		tvLongClickListener = new TextViewLongClickListener();
		noteBtnListener = new NoteBtnLinstener(activity);
	}

	@Override
	public int getCount() {
		if (questionList != null) return questionList.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (questionList != null) return questionList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ViewTag") 
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		
//		String action = null;
//		if(activity2 != null)
//		{
//			action = activity2.getAction();
//		}else
//		{
//			action = activity1.getAction();
//		}
		String action = activity2.getAction();
		ExamQuestion currentQuestion = questionList.get(position); // 当前的题目
		AnswerViewHolder answerHolder = new AnswerViewHolder();
		
		if (v == null) {
			System.out.println("QuestionAdapter调用getView方法，View===NULL position = "+position);
			v = LayoutInflater.from(context).inflate(R.layout.single_question, null);
			contentHolder = new ContentViewHolder();
			v.setTag(R.id.tag_first, contentHolder);
			v.setTag(R.id.tag_second,answerHolder);
			contentHolder.modeLayout = (LinearLayout) v.findViewById(R.id.doexam_mode2layout);
			contentHolder.examContent = (TextView) v.findViewById(R.id.exam_Content2);// 题目内容
			contentHolder.examContent3 = (TextView) v.findViewById(R.id.exam_Content3);
			contentHolder.examContent.setOnLongClickListener(tvLongClickListener);
			contentHolder.examOption = (CheckBoxGroup2) v.findViewById(R.id.examOption2);// checkbox组的容器
			contentHolder.modeLayout4 = (LinearLayout) v.findViewById(R.id.doexam_mode3layout);
			contentHolder.examImages = (LinearLayout) v.findViewById(R.id.examImages2);
			contentHolder.examImages3 = (LinearLayout) v.findViewById(R.id.examImages3);
			contentHolder.answerEditText = (EditText) v.findViewById(R.id.exam_answerEditText);
			contentHolder.submitExamBtn = (Button) v.findViewById(R.id.submitExamBtn);
			contentHolder.scrollView = (ScrollView) v.findViewById(R.id.ContentscrollView);
			contentHolder.noteBtn = (ImageButton) v.findViewById(R.id.btn_note);
			contentHolder.checkBoxListener = new CheckBoxClickListener(activity2, contentHolder.examOption);
			contentHolder.noteBtn.setOnClickListener(noteBtnListener);
//			contentHolder.examAnswerLayout.setVisibility(View.GONE);
		} else {
			System.out.println("QuestionAdapter调用getView方法  view!=!=!=!=!= null position = "+position);
			contentHolder = (ContentViewHolder) v.getTag(R.id.tag_first);
			contentHolder.examOption.clearCheck();
		}
		
		//答案与解析
		answerHolder.examAnswerLayout = (LinearLayout) v.findViewById(R.id.exam_answer_layout); //整个答案的布局
		answerHolder.myAnswerTextView = (TextView) v.findViewById(R.id.myAnswerTextView); //我的答案
		answerHolder.sysAnswerTextView = (TextView) v.findViewById(R.id.sysAnswerTextView);	//正确答案
		answerHolder.answerResultImg = (ImageView) v.findViewById(R.id.answerResultImg);	//判断图片
		answerHolder.analysisTextView = (TextView) v.findViewById(R.id.exam_analysisTextView); //解析
		answerHolder.analysisTextView.setOnLongClickListener(tvLongClickListener);
		answerHolder.examAnswerLayout.setVisibility(View.GONE); 	//隐藏答案
		v.setTag(R.id.tag_second,answerHolder);
		
		// holder.scrollView.fullScroll(33); //滑动到最开始?
		contentHolder.examImages.removeAllViews();
		String type = currentQuestion.getQType(); // 题型
		String answer = currentQuestion.getUserAnswer(); // 学员的答案
		if ("单选题".equals(type) || "多选题".equals(type)) {	//单选题
			contentHolder.modeLayout.setVisibility(0);
			contentHolder.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("[A-Z][.．、)]", "@@@").split("@@@");
			String title = arr[0];
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(position, title, imageSavePath, zuheName, contentHolder.examImages, contentHolder.examContent);
			// this.examOption1.clearCheck();
			if (contentHolder.examOption.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < contentHolder.examOption.getChildCount(); j++) {
					contentHolder.examOption.removeViewAt(j);
				}
			}
			for (int i = 1; i < arr.length; i++) {
				int viewCount = contentHolder.examOption.getChildCount();
				OptionLayout option;
				if (i > viewCount) {
					option = new OptionLayout(context,null);
					option.setId(i);
					option.setFontColor(context.getResources().getColor(R.color.black));
					contentHolder.examOption.addView(option, i - 1);
				}
				option = (OptionLayout) contentHolder.examOption.getChildAt(i - 1);
				option.resetColor();
				option.setText((char) (64 + i) + "．" + arr[i]);
				option.setValue((char) (64 + i)+"");
				if("单选题".equals(type))
				{
					option.setButtonDrawable(R.drawable.radio_button);
					option.setType(OptionLayout.RADIO_BUTTON);
				}
				else
				{
					option.setButtonDrawable(R.drawable.checkbox_button_img);
					option.setType(OptionLayout.CHECK_BOX);
				}
				if (answer != null && answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					option.setChecked(true);
				}
				option.setOnClickListener(contentHolder.checkBoxListener);
			}
//			holder.examOption1
//					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//						@Override
//						public void onCheckedChanged(RadioGroup group, int checkedId) {
//							int id = checkedId;
//							if (id == -1) return;
//							activity.saveChoiceAnswer(((char) (64 + id)) + "");
//						}
//					});
		} 
//		else if ("2".equals(type)||"3".equals(type)) {	//多选和不定选
//			contentHolder.modeLayout.setVisibility(0);
//			contentHolder.modeLayout4.setVisibility(8);
//			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("&nbsp;", " ") .split("###");
//			String title = arr[0];
//			// 显示图片
//			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
//			showPics(position, title, imageSavePath, zuheName, contentHolder.examImages2, contentHolder.examContent2);
//			// 显示选项
//			if (contentHolder.examOption2.getChildCount() > arr.length - 1) {
//				for (int j = arr.length - 1; j < contentHolder.examOption2.getChildCount(); j++) {
//					contentHolder.examOption2.removeViewAt(j);
//				}
//			}
//			// this.examOption2.clearCheck();
//			for (int i = 1; i < arr.length; i++) {
//				int viewCount = contentHolder.examOption2.getChildCount();
//				MyCheckBox cb;
//				if (i > viewCount) {
//					cb = new MyCheckBox(context);
//					cb.setTextColor(context.getResources().getColor(R.color.black));
//					cb.setButtonDrawable(R.drawable.checkbox_button_img);
//					cb.setValue(String.valueOf((char) (64 + i)));
//					cb.setLayoutParams(layoutParams);
////				cb.setBackgroundResource(R.drawable.layout_selector_background);
////				cb.setPadding(0, 5, 0, 5);
//					contentHolder.examOption2.addView(cb, i - 1);
//				}
//				cb = (MyCheckBox) contentHolder.examOption2.getChildAt(i - 1);
//				cb.setText((char) (64 + i) + "．" + arr[i]);
//				if (answer != null
//						&& answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
//					cb.setChecked(true);
//				}
////				cb.setOnClickListener(new OnClickListener() {
////					@Override
////					public void onClick(View v) {
////						MyCheckBox mcb = (MyCheckBox) v;
////						if (mcb.getFlag() == -1) {
////							mcb.setFlag(0);
////							return;
////						}
////						String s = holder.examOption2.getValue();
////						System.out.println("多选题的答案:"+holder.examOption2.getValue());
////						activity.saveChoiceAnswer(s);
////					}
////				});
//				cb.setOnClickListener(contentHolder.checkBoxListener);
//			}
//		} 
		else if ("判断题".equals(type)) { //判断题
			contentHolder.modeLayout.setVisibility(0);
			contentHolder.modeLayout4.setVisibility(8);
			// this.examContent1.setText(questionCursor + 1 + "、"
			// + currentQuestion.getContent());
			// 加载图片
			String title = currentQuestion.getContent().replaceFirst("&nbsp;", " ");
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(position, title, imageSavePath, zuheName, contentHolder.examImages, contentHolder.examContent);
			//
			OptionLayout rb_t, rb_f;
			if (contentHolder.examOption.getChildCount() == 0) {
				rb_t = new OptionLayout(context,null);
				rb_t.setId(1);
				rb_f = new OptionLayout(context,null);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setValue("T");
				rb_t.setFontColor(context.getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_button);
				rb_t.setType(OptionLayout.RADIO_BUTTON);
				rb_f.setText(" ×");
				rb_f.setFontColor(context.getResources().getColor(R.color.black));
				rb_f.setValue("F");
				rb_f.setButtonDrawable(R.drawable.radio_button);
				rb_f.setType(OptionLayout.RADIO_BUTTON);
				contentHolder.examOption.addView(rb_t, 0);
				contentHolder.examOption.addView(rb_f, 1);
			}
			// this.examOption1.clearCheck();
			rb_t = (OptionLayout) contentHolder.examOption.getChildAt(0);
			rb_f = (OptionLayout) contentHolder.examOption.getChildAt(1);
			if (contentHolder.examOption.getChildCount() > 2) {
				contentHolder.examOption.removeAllViews();
				rb_t.setId(1);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setFontColor(context.getResources().getColor(R.color.black));
				rb_t.setValue("T");
				rb_t.setButtonDrawable(R.drawable.radio_button);
				rb_t.setType(OptionLayout.RADIO_BUTTON);
				rb_f.setText(" ×");
				rb_f.setFontColor(context.getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_button);
				rb_f.setType(OptionLayout.RADIO_BUTTON);
				rb_f.setValue("F");
				contentHolder.examOption.addView(rb_t, 0);
				contentHolder.examOption.addView(rb_f, 1);
			}
			rb_t.setOnClickListener(contentHolder.checkBoxListener);
			rb_f.setOnClickListener(contentHolder.checkBoxListener);
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
//			holder.examOption1
//					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//						@Override
//						public void onCheckedChanged(RadioGroup group, int checkedId) { 
//							if (checkedId == -1)
//								return;
//							if (checkedId == 1)
//								activity.saveChoiceAnswer("T");
//							else
//								activity.saveChoiceAnswer("F");
//						}
//					});
		} else if ("问答题".equals(type)) {
			contentHolder.modeLayout.setVisibility(8);
			contentHolder.modeLayout4.setVisibility(0);
			String title = currentQuestion.getContent();
			// 显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();
			showPics(position, title, imageSavePath, zuheName, contentHolder.examImages3, contentHolder.examContent3);
			if (answer != null) {
				contentHolder.answerEditText.setText(answer);
			}
			contentHolder.submitExamBtn.setVisibility(0);
			contentHolder.submitExamBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String txtAnswer = contentHolder.answerEditText.getText().toString();
					activity2.saveTextAnswer(txtAnswer);
				}
			});
		}
		if (!"DoExam".equals(action)) {
			if(("practice".equals(action)||"myErrors".equals(action))&&currentQuestion.getUserAnswer()==null){
				answerHolder.examAnswerLayout.setVisibility(View.GONE);
			}else if(("practice".equals(action)||"myErrors".equals(action)||"showQuestionWithAnswer".equals(action)) && currentQuestion.getUserAnswer()!=null){
				answerHolder.examAnswerLayout.setVisibility(View.VISIBLE);
				contentHolder.examOption.setFontColor(context.getResources().getColor(R.color.green), currentQuestion.getAnswer());
				contentHolder.examOption.forbidden(false);
//				if("2".equals(currentQuestion.getQType())||"3".equals(currentQuestion.getQType()))
//				{
//					contentHolder.examOption2.forbidden(false);
//				}else
//				{
//					setRadioEnable(contentHolder.examOption1, false);
//				}
			}else {
				answerHolder.examAnswerLayout.setVisibility(View.VISIBLE);
				//禁用选择
				contentHolder.examOption.forbidden(false);
			}
			showAnswer(answerHolder,currentQuestion, answer);
		}
		return v;
	}
	private String answerToTF(String answer)
	{
		return "T".equals(answer)?" √":"F".equals(answer)?" ×":answer;
	}
	public static class ContentViewHolder {
		ScrollView scrollView;
		LinearLayout modeLayout,modeLayout4,examImages3, examImages;
		TextView examContent, examContent3,
		myAnswerTextView,sysAnswerTextView, analysisTextView;
		EditText answerEditText;
		public CheckBoxGroup2 examOption;
		Button submitExamBtn;
//		ImageView answerResultImg;
		ImageButton noteBtn;
		OnClickListener checkBoxListener;
//		public LinearLayout examAnswerLayout;
	}
	public static class AnswerViewHolder
	{
		public ImageView answerResultImg;
		TextView sysAnswerTextView, analysisTextView;
		public LinearLayout examAnswerLayout;
		public TextView myAnswerTextView;
	}
	
	public void showAnswer(AnswerViewHolder holder,ExamQuestion currentQuestion,String userAnswer)
	{
		String trueAnswer = currentQuestion.getAnswer();
		String type = currentQuestion.getQType();
		holder.myAnswerTextView.setText(answerToTF(userAnswer));
		holder.sysAnswerTextView.setText(answerToTF(trueAnswer));
		holder.analysisTextView.setText(currentQuestion.getAnalysis());
		if ("问答题".equals(type)) {
			holder.answerResultImg.setVisibility(View.GONE);
		} else {
			holder.answerResultImg.setVisibility(View.VISIBLE);
			if (trueAnswer.equals(userAnswer)) {
				holder.answerResultImg .setImageResource(R.drawable.correct_answer_pto);
			} else if (userAnswer != null && !"".equals(userAnswer) && isContain(trueAnswer, userAnswer)) {
				holder.answerResultImg.setImageResource(R.drawable.halfcorrect_pto);
			} else {
				holder.answerResultImg.setImageResource(R.drawable.wrong_answer_pto);
			}
		}
	}

	// 判断答案是否包含
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

	// 显示题目中的图片
	private void showPics(int questionCursor, String title, String imageSavePath, String zuheName, LinearLayout examImages, TextView examContent) {
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
						ImageView img = new ImageView(context);
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
						new GetImageTask(zuheName + "-" + i, examImages).execute(url);
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
					new GetImageTask(zuheName + "-" + i, examImages).execute(url);
				}
			}
			examContent.setText(questionCursor + 1 + "、" + title.replaceAll("<IMG[\\S\\s]+>", ""));
		} else
			examContent.setText(questionCursor + 1 + "、" + title);
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
		private LinearLayout examImages1;

		public GetImageTask(String fileName, LinearLayout examImages1) {
			this.fileName = fileName;
			this.examImages1 = examImages1;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			URL url;
			//byte[] b = null;
			try {
				fileName += params[0].substring(params[0].lastIndexOf("."));
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
			ImageView img = new ImageView(context);
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
//	//显示或者隐藏答案
//	public void showAnswer() {
//		System.out.println("显示或者隐藏答案");
//		if (holder.examAnswerLayout.getVisibility() == View.GONE) {
//			holder.examAnswerLayout.setVisibility(View.VISIBLE);
//		} else {
//			holder.examAnswerLayout.setVisibility(View.GONE);
//		}
//	}
	//清除选择项
	public void clearCheck() {
		contentHolder.examOption.clearCheck();
	}
	
//	private class RadioClickListener implements OnClickListener
//	{
//		private BaseActivity activity;
//		private String action;
//		private AnswerViewHolder viewHolder;
//		private ContentViewHolder contentHolder;
//		private ExamQuestion currentQuestion;
//		public RadioClickListener(BaseActivity activity,View v,String action,ExamQuestion q) {
//			this.activity = activity;
//			this.action = action;
//			this.currentQuestion =q;
//			this.contentHolder = (ContentViewHolder) v.getTag(R.id.tag_first);
//			this.viewHolder = (AnswerViewHolder) v.getTag(R.id.tag_second);
//		}
//		@Override
//		public void onClick(View v) {
////			int id = contentHolder.examOption.getCheckedRadioButtonId();
////			if (id == -1)
////				return;
////			activity.saveChoiceAnswer(((char) (64 + id)) + "");
//			//viewholder不是确定的那个题的holder,将这个放到activity中
////			if("practice".equals(action))
////			{
////				contentHolder.examOption1.setEnabled(false);
////				setRadioEnable(contentHolder.examOption1,false);
////				showAnswer(viewHolder,currentQuestion, ((char) (64 + id)) + "");
////				viewHolder.examAnswerLayout.setVisibility(View.VISIBLE);
////			}
//		}
////		@Override
////		public void onCheckedChanged(RadioGroup group, int checkedId) {
//////			System.out.println("调用onCheckedChanged方法, checkedId = "+ checkedId);
////			int id = checkedId;
////			if (id == -1)
////				return;
////			activity.saveChoiceAnswer(((char) (64 + id)) + "");
////		}
//	}
	public void setRadioEnable(RadioGroup group,boolean flag)
	{
		System.out.println("设置禁用radiobutton");
		int viewCount = group.getChildCount();
		for(int i=0;i<viewCount;i++)
		{
			group.getChildAt(i).setEnabled(flag);
		}
	}
	
	private class CheckBoxClickListener implements OnClickListener
	{
		private QuestionDoExamActivity2 activity;
		private CheckBoxGroup2 group;
		
		public CheckBoxClickListener(QuestionDoExamActivity2 activity2,CheckBoxGroup2 group) {
			this.activity = activity2;
			this.group = group;
		}
		@Override
		public void onClick(View v) {
			OptionLayout option = (OptionLayout) v;
			if(option.isChecked()&&option.getType()==OptionLayout.CHECK_BOX)
				option.setChecked(false);
			else
				option.setChecked(true);
			if(option.getType() == OptionLayout.RADIO_BUTTON)
			{
				group.setOnlyOneCheck(option);
			}
			this.activity.saveChoiceAnswer(group.getValue());
		}
	}
	
	private class NoteBtnLinstener implements OnClickListener
	{
		private QuestionDoExamActivity2 activity;
		public NoteBtnLinstener(QuestionDoExamActivity2 activity) {
			this.activity = activity;
		}
		@Override
		public void onClick(View v) {
			this.activity.writeNote();
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint({ "NewApi", "ServiceCast" })
	public void setClipBoard(String content) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("label", content);
			clipboard.setPrimaryClip(clip);
		} else {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(content);
		}
	}
	
	private class TextViewLongClickListener implements View.OnLongClickListener{
		@Override
		public boolean onLongClick(final View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setItems(new CharSequence[]{"复制内容"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setClipBoard(((TextView)v).getText().toString());
					Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
				}
			});
			builder.create().show();
			return true;
		}
	}
}
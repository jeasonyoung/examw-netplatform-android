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

import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.examw.netschool.QuestionDoExamActivity2;
import com.examw.netschool.R;
import com.examw.netschool.customview.CheckBoxGroup;
import com.examw.netschool.customview.MyCheckBox;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRule;

public class QuestionAdapter extends BaseAdapter{
	private Context context;
	private QuestionDoExamActivity2 activity;
	private ArrayList<ExamQuestion> questionList;
	//private ArrayList<ExamRule> ruleList;
	//private TextView examTypeTextView;
	private String action;
	//private ImageButton favoriteBtn;
	private ViewHolder holder;
	// 图片保存目录
	private String imageSavePath;
	public QuestionAdapter(Context context,QuestionDoExamActivity2 activity,ArrayList<ExamQuestion> questionList,ArrayList<ExamRule> rules,
							String action,String username,String paperid,TextView examTypeTextView,ImageButton favoriteBtn) 
	{
		this.context = context;
		this.questionList = questionList;
		//this.ruleList= rules;
		// /mnt/sdcard/eschool/hahaha/image/1001
		this.imageSavePath = Environment.getExternalStorageDirectory().getPath()
						+ File.separator + "eschool" + File.separator + username
						+ File.separator + "image" + File.separator + paperid;
		this.action = action;
		//this.examTypeTextView = examTypeTextView;
		//this.favoriteBtn = favoriteBtn;
		this.activity = activity;
	}
	@Override
	public int getCount() {
		if(questionList != null) return questionList.size();
		return 0;
	}
	@Override
	public Object getItem(int position) {
		if(questionList != null) return questionList.get(position);
		return null;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if(v == null)
		{
			v = LayoutInflater.from(context).inflate(com.examw.netschool.R.layout.single_question, null);
			holder = new ViewHolder();
			holder.modeLayout1 = (LinearLayout) v.findViewById(R.id.doexam_mode1layout);
			holder.modeLayout2 = (LinearLayout) v.findViewById(R.id.doexam_mode2layout);
			holder.modeLayout3 = (LinearLayout) v.findViewById(R.id.doexam_mode3layout);
			holder.modeLayout4 = (LinearLayout) v.findViewById(R.id.doexam_mode4layout);
			holder.examContent1 = ((TextView) v.findViewById(R.id.exam_Content)); // exam_Content1
			holder.examImages1 = (LinearLayout) v.findViewById(R.id.examImages1);
			holder.examOption1 = ((RadioGroup) v.findViewById(R.id.examOption)); // examOption1
			holder.examContent2 = (TextView) v.findViewById(R.id.exam_Content2);// 题目内容
			holder.examOption2 = (CheckBoxGroup) v.findViewById(R.id.examOption2);// checkbox组的容器
			holder.examImages2 = (LinearLayout) v.findViewById(R.id.examImages2);
			holder.examContent3 = (TextView) v.findViewById(R.id.exam_Content3);
			holder.answerEditText = (EditText) v.findViewById(R.id.exam_answerEditText);
			holder.examAnswerLayout = (LinearLayout) v.findViewById(R.id.exam_answer_layout);
			holder.examImages3 = (LinearLayout) v.findViewById(R.id.examImages3);
			holder.submitExamBtn = (Button) v.findViewById(R.id.submitExamBtn);
			holder.scrollView = (ScrollView) v.findViewById(R.id.ContentscrollView);
			v.setTag(holder);
		}else
		{
			holder = (ViewHolder) v.getTag();
			holder.examOption1.clearCheck();
			holder.examOption2.clearCheck();
		}
		//holder.scrollView.fullScroll(33); //滑动到最开始?
		ExamQuestion currentQuestion = questionList.get(position); //当前的题目
		holder.examImages1.removeAllViews();
		String type = currentQuestion.getQType();	//题型
		String answer = currentQuestion.getUserAnswer();	//学员的答案
		System.out.println(currentQuestion.getAnswer());
		if ("单选题".equals(type)) {
			holder.modeLayout1.setVisibility(0);
			holder.modeLayout2.setVisibility(8);
			holder.modeLayout3.setVisibility(8);
			holder.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("[A-Z][.．、)]", "@@@").split("@@@");
			String title = arr[0];
			//显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();;
			showPics(position,title,imageSavePath,zuheName,holder.examImages1,holder.examContent1);
			// this.examOption1.clearCheck();
			if (holder.examOption1.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < holder.examOption1.getChildCount(); j++) {
					holder.examOption1.removeViewAt(j);
				}
			}
			for (int i = 1; i < arr.length; i++) {
				int viewCount = holder.examOption1.getChildCount();
				RadioButton rb;
				if (i > viewCount) {
					rb = new RadioButton(context);
					rb.setId(i);
					rb.setTextColor(context.getResources().getColor(R.color.black));
					rb.setButtonDrawable(R.drawable.radio_btn_img);
					holder.examOption1.addView(rb, i - 1);
				}
				rb = (RadioButton) holder.examOption1.getChildAt(i - 1);
				rb.setText((char) (64 + i) + "．" + arr[i]);
				if (answer != null && answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					rb.setChecked(true);
				}
			}
			holder.examOption1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int id = checkedId;
						if (id == -1) return;
						activity.saveChoiceAnswer(((char) (64 + id)) + "");
					}
			});
		} else if ("多选题".equals(type)) {
			holder.modeLayout1.setVisibility(8);
			holder.modeLayout2.setVisibility(0);
			holder.modeLayout3.setVisibility(8);
			holder.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "").replaceAll("[A-Z][.．、)]", "@@@").split("@@@");
			String title = arr[0];
			//显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();;
			showPics(position,title,imageSavePath,zuheName,holder.examImages2,holder.examContent2);
			//显示选项
			if (holder.examOption2.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < holder.examOption2.getChildCount(); j++) {
					holder.examOption2.removeViewAt(j);
				}
			}
			// this.examOption2.clearCheck();
			for (int i = 1; i < arr.length; i++) {
				int viewCount = holder.examOption2.getChildCount();
				MyCheckBox cb;
				if (i > viewCount) {
					cb = new MyCheckBox(context);
					cb.setTextColor(context.getResources().getColor(R.color.black));
					cb.setButtonDrawable(R.drawable.checkbox_button_img);
					cb.setValue(String.valueOf((char) (64 + i)));
					holder.examOption2.addView(cb, i - 1);
				}
				cb = (MyCheckBox) holder.examOption2.getChildAt(i - 1);
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
						String s = holder.examOption2.getValue();
						activity.saveChoiceAnswer(s);
					}
				});
			}
		} else if ("判断题".equals(type)) {
			holder.modeLayout1.setVisibility(0);
			holder.modeLayout2.setVisibility(8);
			holder.modeLayout3.setVisibility(8);
			holder.modeLayout4.setVisibility(8);
			// this.examContent1.setText(questionCursor + 1 + "、"
			// + currentQuestion.getContent());
			// 加载图片
			String title = currentQuestion.getContent();
			//显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();;
			showPics(position,title,imageSavePath,zuheName,holder.examImages1,holder.examContent1);
			//
			RadioButton rb_t, rb_f;
			if (holder.examOption1.getChildCount() == 0) {
				rb_t = new RadioButton(context);
				rb_t.setId(1);
				rb_f = new RadioButton(context);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setTextColor(context.getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ×");
				rb_f.setTextColor(context.getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				holder.examOption1.addView(rb_t, 0);
				holder.examOption1.addView(rb_f, 1);
			}
			// this.examOption1.clearCheck();
			rb_t = (RadioButton) holder.examOption1.getChildAt(0);
			rb_f = (RadioButton) holder.examOption1.getChildAt(1);
			if (holder.examOption1.getChildCount() > 2) {
				holder.examOption1.removeAllViews();
				rb_t.setId(1);
				rb_f.setId(2);
				rb_t.setText(" √");
				rb_t.setTextColor(context.getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ×");
				rb_f.setTextColor(context.getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				holder.examOption1.addView(rb_t, 0);
				holder.examOption1.addView(rb_f, 1);
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
			holder.examOption1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {		
					if (checkedId == -1) return;
					if (checkedId == 1)
						activity.saveChoiceAnswer("T");		
					else		
						activity.saveChoiceAnswer("F");
					}	
			});
		} else if ("问答题".equals(type)) {
			holder.modeLayout1.setVisibility(8);
			holder.modeLayout2.setVisibility(8);
			holder.modeLayout3.setVisibility(0);
			holder.modeLayout4.setVisibility(8);
			String title = currentQuestion.getContent();
			//显示图片
			String zuheName = currentQuestion.getRuleId() + "-" + currentQuestion.getQid();;
			showPics(position,title,imageSavePath,zuheName,holder.examImages3,holder.examContent3);
			if (answer != null) {
				holder.answerEditText.setText(answer);
			}
			holder.submitExamBtn.setVisibility(0);
			holder.submitExamBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String txtAnswer = holder.answerEditText.getText().toString();
					activity.saveTextAnswer(txtAnswer);
				}
			});
		}
		if (!"DoExam".equals(action)) {
			String trueAnswer = currentQuestion.getAnswer();
			holder.myAnswerTextView.setText(answer);
			holder.sysAnswerTextView.setText(trueAnswer);
			holder.analysisTextView.setText(currentQuestion.getAnalysis());
			if("问答题".equals(type)){
				holder.answerResultImg.setVisibility(View.GONE);
			}else {
				holder.answerResultImg.setVisibility(View.VISIBLE);
				if(trueAnswer.equals(answer)) {
					holder.answerResultImg.setImageResource(R.drawable.correct_answer_pto);
				}else if(answer!=null&&!"".equals(answer)&&isContain(trueAnswer,answer)) {
					holder.answerResultImg.setImageResource(R.drawable.halfcorrect_pto);
				}else {
					holder.answerResultImg.setImageResource(R.drawable.wrong_answer_pto);
				}
			}
		}
		return v;
	}
	static class ViewHolder
	{
		ScrollView scrollView;
		LinearLayout modeLayout1, modeLayout2, modeLayout3, modeLayout4,examAnswerLayout,examImages1,examImages2,examImages3;
		TextView examContent1, examContent2, examContent3,myAnswerTextView,sysAnswerTextView,analysisTextView;
		RadioGroup examOption1;
		EditText answerEditText;
		CheckBoxGroup examOption2;
		Button submitExamBtn;
		ImageView answerResultImg;
	}
	//判断答案是否包含
	private boolean isContain(String trueAnswer,String answer)
	{
		if(answer.length()==1)
		{
			return trueAnswer.contains(answer);
		}
		String[] arr = answer.split(",");
		boolean flag = true;
		for(String s :arr)
		{
			flag=flag&&trueAnswer.contains(s);
		}
		return flag;
	}
	
	// 显示题目中的图片
	private void showPics(int questionCursor,String title, String imageSavePath, String zuheName, LinearLayout examImages, TextView examContent) {
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
						new GetImageTask(zuheName + "-" + i,examImages).execute(url);
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
						new GetImageTask(zuheName + "-" + i,examImages).execute(url);
					}
				}
				examContent.setText(questionCursor + 1 + "、" + title.replaceAll("<IMG[\\S\\s]+>", ""));
			} else{
				examContent.setText(questionCursor + 1 + "、" + title);
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
			private LinearLayout examImages1;
			
			public GetImageTask(String fileName,LinearLayout examImages1) {
				this.fileName = fileName;
				this.examImages1 = examImages1;
			}

			@Override
			protected Bitmap doInBackground(String... params) {
				URL url;
				//byte[] b = null;
				try {
					fileName = fileName + params[0].substring(params[0].lastIndexOf("."));
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
				if (result == null)  return;
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
		public void showAnswer()
		{
			if(holder.examAnswerLayout.getVisibility()==View.GONE) {
				holder.examAnswerLayout.setVisibility(View.VISIBLE);
			}else {
				holder.examAnswerLayout.setVisibility(View.GONE);
			}
		}
}
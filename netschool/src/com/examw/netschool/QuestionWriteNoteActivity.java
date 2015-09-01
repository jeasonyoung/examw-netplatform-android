package com.examw.netschool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.ExamNote;

public class QuestionWriteNoteActivity extends Activity implements OnClickListener{
	private ImageButton returnbtn;
	private EditText editNoteEditText;
	private Button submitBtn,submitBtn2;
	private TextView editSizeText;
	private ExamNote note;
	private String qid,username,paperId;
	private final static int maxLength = 1000;
	private PaperDao dao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doexam_notebook);
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.editNoteEditText = (EditText) this
				.findViewById(R.id.editNoteEditText);
		this.submitBtn = (Button) this.findViewById(R.id.exam_notebook_btn);
		this.submitBtn2 = (Button) this.findViewById(R.id.submitBtn);
		this.editSizeText = (TextView) this
				.findViewById(R.id.notebook_editSizeText);
		this.dao = new PaperDao(this);
		Intent mIntent = this.getIntent();
		this.qid = mIntent.getStringExtra("qid");
		this.username = mIntent.getStringExtra("username");
		this.paperId = mIntent.getStringExtra("paperid");
		String text = this.dao.findNoteContent(qid,username);
		if(text!=null)
			this.editNoteEditText.setText(text);
		this.editNoteEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				QuestionWriteNoteActivity.this.editSizeText.setText("已输入: "
						+ s.length() + "/" + maxLength);
			}
		});
		//限制字数
		this.editNoteEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
		//设置监听
		this.returnbtn.setOnClickListener(this);
		this.submitBtn.setOnClickListener(this);
		this.submitBtn2.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.returnbtn:
				this.finish();
				return;
			case R.id.exam_notebook_btn:
				submit();
				return;
			case R.id.submitBtn:
				submit();
				return;
		}
	}
	private void submit()
	{
		String content = this.editNoteEditText.getText().toString();
		if(content.trim().length()==0)
		{
			Toast.makeText(this, "还没有输入内容", Toast.LENGTH_SHORT).show();
			return;
		}
		String addTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
		note = new ExamNote(qid,addTime,content,username,paperId);
		dao.insertNote(note);
		
		Toast.makeText(this, "笔记已添加", Toast.LENGTH_SHORT).show();
		
		this.finish();
	}
}
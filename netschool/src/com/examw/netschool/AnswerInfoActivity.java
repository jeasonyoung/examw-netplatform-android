package com.examw.netschool;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.examw.netschool.entity.Problem;
import com.google.gson.Gson;

public class AnswerInfoActivity extends Activity{
	private ImageButton returnbtn;
	private TextView askText,askTimeText,answerText,replyTimeText;
	private LinearLayout noBestLayout,theBestLayout;
	private Problem p;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_answerinfos);
		findViews();
		initViews();
	}
	private void findViews()
	{
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.askText = (TextView) this.findViewById(R.id.askText);
		this.askTimeText = (TextView) this.findViewById(R.id.askTimeText);
		this.answerText = (TextView) this.findViewById(R.id.bestcontentText);
		this.replyTimeText = (TextView) this.findViewById(R.id.bestReplyTimeText);
		this.noBestLayout = (LinearLayout) this.findViewById(R.id.noBestLayout);
		this.theBestLayout = (LinearLayout) this.findViewById(R.id.theBestLayout);
		this.returnbtn.setOnClickListener(new ReturnBtnClickListener(this));
	}
	private void initViews()
	{
		p = new Gson().fromJson(getIntent().getStringExtra("problem"), Problem.class);
		this.askText.setText(Html.fromHtml(p.getContent()));
		this.askTimeText.setText(p.getAddTime());
		try{
			JSONArray json = new JSONArray(p.getAnswersJson());
			JSONObject obj = json.getJSONObject(0);
			String answer = obj.getString("answerContent");
			this.answerText.setText(Html.fromHtml(answer));
			this.replyTimeText.setText(obj.getString("answerTime").split("T")[0]);
			this.noBestLayout.setVisibility(View.GONE);
			this.theBestLayout.setVisibility(View.VISIBLE);
		}catch(Exception e)
		{
			e.printStackTrace();
			this.noBestLayout.setVisibility(View.VISIBLE);
			this.theBestLayout.setVisibility(View.GONE);
		}
	}
}
package com.examw.netschool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.umeng.analytics.MobclickAgent;

public class QuestionMainActivity extends Activity{
	private GridView g;
	private ImageButton returnBtn;
	private String username;
	
	private Class<?>[] classes = new Class[]{
																		QuestionFromCourseActivity.class,
																		QuestionCommonFirstActivity.class,
																		QuestionRecordActivity.class,
																		QuestionCommonFirstActivity.class,
																		QuestionCommonFirstActivity.class
																	};
	
	private String[] actions = new String[]{null,"myErrors","myRecord","myFavors","myNotes"};
	private String loginType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_main);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.g = (GridView) this.findViewById(R.id.question_main_grid);
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		this.g.setAdapter(new QuestionMainAdapter(this));
		Intent intent = this.getIntent();
		username = intent.getStringExtra("username");
		loginType = intent.getStringExtra("loginType");
		if("local".equals(loginType)){
			classes[0] = QuestionPaperListActivity.class;
		}
		this.g.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//Toast.makeText(QuestionMainActivity.this, "点击了Item", Toast.LENGTH_LONG).show();
				Intent mIntent = new Intent(QuestionMainActivity.this,classes[arg2]);
				mIntent.putExtra("username", username);
				mIntent.putExtra("actionName", actions[arg2]);
				mIntent.putExtra("loginType", loginType);
				QuestionMainActivity.this.startActivity(mIntent);
			}
		});
	}
	private class QuestionMainAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;
		
		private int[] imgs = new int[]{	R.drawable.cccc,
															R.drawable.question_error_img,
															R.drawable.question_doproblemrecord_img,
															R.drawable.question_myfavorite_img,
															R.drawable.question_mynotebook_img };
		
		private int[] txts = new int[]{	R.string.question_bank,
														R.string.errorQuesitionStr,
														R.string.doProblem_recordStr,
														R.string.my_favoriteStr,
														R.string.my_notebookStr };
		
		public QuestionMainAdapter(Context context) { 
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return imgs.length;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@SuppressLint("ViewHolder") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = mInflater.inflate(R.layout.grid_question_main, null);
			ImageView iv = (ImageView) v.findViewById(R.id.question_grid_img);
			TextView tv = (TextView) v.findViewById(R.id.question_grid_text);
			iv.setImageResource(imgs[position]);
			tv.setText(txts[position]);
			return v;
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
}
package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.examw.netschool.adapter.MyCourseListAdapter2;
import com.examw.netschool.dao.CourseDao;
import com.examw.netschool.entity.Course;

public class MyCourseDetailActivity extends BaseActivity implements OnClickListener{
	private ImageButton rbtn;
	private TextView title;
	private ListView list;
	private LinearLayout online,outline,playrecord,downloaded;
	private LinearLayout nodata;
	private List<Course> courseList;
	private String gradeId;
	private CourseDao dao;
	private MyCourseListAdapter2 mAdapter;
	private String loginType,username;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_mycourse);
		dao = new CourseDao(this);
		rbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		title = (TextView) this.findViewById(R.id.courseTitle);
		list = (ListView) this.findViewById(R.id.courserList);
		initLayoutBtn();
		Intent intent = this.getIntent();
		title.setText(intent.getStringExtra("name"));
		this.loginType = intent.getStringExtra("loginType");
		this.username = intent.getStringExtra("username");
		if("local".equals(loginType))
		{
			gradeId = intent.getStringExtra("classid");
		}else
		{
			setList(intent.getStringExtra("classDetails"));
		}
		//设置缓存颜色为透明
		list.setCacheColorHint(Color.TRANSPARENT);
		list.setAlwaysDrawnWithCacheEnabled(true); 
		rbtn.setOnClickListener(this);
		
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if(courseList==null)
		{
			courseList = dao.findByClassId(gradeId,username);
		}else
		{
			courseList.clear();
			courseList.addAll(dao.findByClassId(gradeId, username));
		}
		if(list.getAdapter()==null)
		{
			mAdapter = new MyCourseListAdapter2(this,courseList,loginType,username);
			list.setAdapter(mAdapter);
		}else
		{
			mAdapter.notifyDataSetChanged();
		}
		if(courseList.size()==0)
		{
			nodata.setVisibility(View.VISIBLE);
		}
		super.onStart();
	}
	private void initLayoutBtn()
	{
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.downloaded = (LinearLayout) this.findViewById(R.id.Downloadto_layout_btn);
		this.outline = (LinearLayout) this.findViewById(R.id.MyfileDown_layout_btn);
		this.online = (LinearLayout) this.findViewById(R.id.Lookonline_layout_btn);
		this.playrecord = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		this.outline.setOnClickListener(this);
		this.online.setOnClickListener(this);
		this.playrecord.setOnClickListener(this);
		this.downloaded.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.Lookonline_layout_btn://在线播放
			break;
		case R.id.MyfileDown_layout_btn://离线播放
			Intent intent = new Intent(this,DownloadActivity.class);
			intent.putExtra("actionName", "outline");
			intent.putExtra("username", username);
			this.startActivity(intent);
			break;
		case R.id.LearningRecord_layout_btn://学习记录
			Intent mIntent = new Intent(this,PlayrecordActivity.class);
			mIntent.putExtra("username", username);
			mIntent.putExtra("loginType", loginType);
			this.startActivity(mIntent);
			break;
		case R.id.Downloadto_layout_btn:
			Intent intent2 = new Intent(this,DownloadActivity.class);
			intent2.putExtra("username", username);
			this.startActivity(intent2);
			break;
		case R.id.returnbtn:
			this.finish();
			break;
		}
	}
	private void setList(String array)
	{
		if(array==null||array.equals(""))
		{
			return;
		}
		try {
			JSONArray json = new JSONArray(array);
			List<Course> list = new ArrayList<Course>();
			gradeId = json.getJSONObject(0).getInt("gradeId")+"";
			for(int i=0;i<json.length();i++)
			{
				JSONObject j = json.getJSONObject(i);
				Course data = new Course();
				data.setCourseId(j.getInt("classId")+"");
				data.setCourseName(j.getString("classTitle"));
				data.setClassId(j.getInt("gradeId")+"");
				data.setFileUrl(j.getString("classHdUrl"));
				data.setState(0);
				data.setUserName(username);
				 list.add(data);
			}
			dao.save(username, list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
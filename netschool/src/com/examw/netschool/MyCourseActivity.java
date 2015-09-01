package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.dao.UserClassDao;
import com.examw.netschool.entity.UserClass;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;

public class MyCourseActivity extends BaseActivity implements OnClickListener {
	private SharedPreferences userinfo;
	private ProgressDialog dialog;
	private Handler handler;
	private ExpandableListView expandList;
	private LinearLayout nodata;
	private ImageButton returnBtn;
	private String[] group;
	private String[][] child;
	private String[][] classDetail;
	private int id;
	private boolean isLocalLogin;
	private LinearLayout outlineCourse, playrecord;
	private UserClassDao dao;
	private String username,loginType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_mycourselist);
		Intent intent = this.getIntent();
		this.loginType = intent.getStringExtra("loginType");
		this.isLocalLogin = "local".equals(loginType);
		this.username = intent.getStringExtra("username");
		userinfo = getSharedPreferences("userinfo", 0);
		id = userinfo.getInt("id", 0);
		expandList = (ExpandableListView) this.findViewById(R.id.explist2);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		outlineCourse = (LinearLayout) this.findViewById(R.id.MyfileDown_layout_btn);
		playrecord = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		returnBtn.setOnClickListener(this);
		outlineCourse.setOnClickListener(this);
		playrecord.setOnClickListener(this);
		expandList.setGroupIndicator(null);
		expandList.setOnChildClickListener(new ChildClickListener());
		expandList.setOnGroupClickListener(new GroupClickListener());
		nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		handler = new MyHandler(this);
		dao = new UserClassDao(this);
		dialog = ProgressDialog.show(MyCourseActivity.this, null, "努力加载中请稍候",true, true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		new GetMyLessonThread().start();
		
	}

	private class GetMyLessonThread extends Thread {
		@Override
		public void run() {
			try {
				if (!isLocalLogin) {
					String result = HttpConnectUtil.httpGetRequest(
							MyCourseActivity.this,
							Constant.DOMAIN_URL+"mobile/myLessons?stuId="
									+ id);
					//解析字符串
					if (result != null && !result.equals("null")) {
						// 解析json字符串,配置expandableListView的adapter
						try {
							JSONObject json = new JSONObject(result);
							JSONArray packages = json.optJSONArray("classPackages");
							JSONArray grades = json.getJSONArray("grade");
							int plength = packages.length();
							int glength = grades.length();
							List<UserClass> list = new ArrayList<UserClass>();
							// 大项,套餐或单班级
							group = new String[plength + glength];
							// 子项,套餐下的班级,单班级没有子项
							// 注意子项的长度比大项的短
							child = new String[plength][];
							// 课程详细,点击班级时跳转
							classDetail = new String[plength + glength][];
							// 循环套餐( classid,classname,username,fatherid,classtype)
							for (int i = 0; i < plength; i++) {
								JSONObject p = packages.getJSONObject(i);
								group[i] = p.optString("pkgName");
								JSONArray p_grades = p.getJSONArray("grade");
								child[i] = new String[p_grades.length()];
								classDetail[i] = new String[p_grades
										.length()];
								int pkgId = p.optInt("pkgId");
								UserClass c = new UserClass( pkgId + "",
										p.optString("pkgName"), username, 0 + "",
										1 + "");
								list.add(c);
								// 循环套餐下的班级
								for (int k = 0; k < p_grades.length(); k++) {
									p = p_grades.getJSONObject(k);
									child[i][k] = p.getString("name");
									JSONArray cd = p.optJSONArray("classDetails");
									classDetail[i][k] = cd.toString();
									//
									UserClass c1 = new UserClass(
											p.optInt("gradeId") + "",
											p.optString("name"), username,
											pkgId + "", 0 + "");
									list.add(c1);
								}
							}
							// 循环班级
							for (int j = 0; j < glength; j++) {
								JSONObject p = grades.getJSONObject(j);
								group[j + plength] = p
										.optString("name");
								JSONArray cd = p.optJSONArray("classDetails");
								classDetail[plength + j] = new String[1];
								classDetail[plength + j][0] = cd == null ? "[]"
										: cd.toString();
								UserClass c1 = new UserClass(p.optInt("gradeId")
										+ "", p.optString("name"), username,
										p.optInt("pkgId") + "", 0 + "");
								list.add(c1);
							}
							// 将我的课程保存到数据库
							dao.deleteAll(username);
							dao.addClasses(list);
							Message msg = handler.obtainMessage();
							msg.what = 1;
							handler.sendMessage(msg);
						} catch (Exception e) {
								e.printStackTrace();
								handler.sendEmptyMessage(-1);
							}
						} else {
							//发消息
							Message msg = handler.obtainMessage();
							msg.what = -2;
							handler.sendMessage(msg);
						}
				}else
				{
					//通过数据库找 [group,child]
					group = dao.findBigClassName(username);
					child = dao.findChildrenClass(username);
					classDetail = dao.findChildrenClassid(username);
					Message msg = handler.obtainMessage();
					if(group==null)
					{
						msg.what = -3;
					}else
					{
						msg.what = 2;
					}
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Message msg = handler.obtainMessage();
				msg.what = -1;
				handler.sendMessage(msg);
			}
		}
	}

	static class MyHandler extends Handler {
		WeakReference<MyCourseActivity> mActivity;

		MyHandler(MyCourseActivity activity) {
			mActivity = new WeakReference<MyCourseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MyCourseActivity theActivity = mActivity.get();
			switch (msg.what) {
			case 1:
				theActivity.dialog.dismiss();
				theActivity.expandList.setAdapter(new MyExpandableAdapter(
										theActivity, theActivity.group,
										theActivity.child));
						// 设置adapter
				break;
			case -1:
				// 连不上,
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// 无数据显示
				Toast.makeText(theActivity, "暂时连不上服务器,请稍候", Toast.LENGTH_SHORT)
						.show();// 提示
				break;
			case -2:
				//没有数据
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// 无数据显示
				Toast.makeText(theActivity, "您没有购买课程", Toast.LENGTH_SHORT)
						.show();// 提示
				break;
			case -3:
				//数据库中没有数据
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// 无数据显示
				Toast.makeText(theActivity, "本地数据库中没有数据,请在线登录后进入我的课程", Toast.LENGTH_SHORT)
						.show();// 提示
				break;
			case 2:
				//通过数据库查找
				theActivity.dialog.dismiss();
				theActivity.expandList
						.setAdapter(new MyExpandableAdapter(
								theActivity, theActivity.group,
								theActivity.child));
				break;
			}
		}
	}

	private class ChildClickListener implements OnChildClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MyCourseActivity.this,
					MyCourseDetailActivity.class);
			intent.putExtra("name", ((TextView) v.findViewById(R.id.text3))
					.getText().toString());
			if(isLocalLogin)
			{
				intent.putExtra("classid", MyCourseActivity.this.classDetail[groupPosition][childPosition]);
			}else{
				intent.putExtra(
						"classDetails",
						MyCourseActivity.this.classDetail[groupPosition][childPosition]);
			}
			intent.putExtra("username", username);
			intent.putExtra("loginType", loginType);
			MyCourseActivity.this.startActivity(intent);
			return true;
		}
	}

	private class GroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			// TODO Auto-generated method stub
			// 如果没有子类了,表示是单独的课程
			if (parent.getExpandableListAdapter().getChildrenCount(
					groupPosition) == 0) {
				Intent intent = new Intent(MyCourseActivity.this,
						MyCourseDetailActivity.class);
				intent.putExtra("name", ((TextView) v.findViewById(R.id.text2))
						.getText().toString());
				if(isLocalLogin)
				{
					intent.putExtra("classid", MyCourseActivity.this.classDetail[groupPosition][0]);
				}else{
				intent.putExtra("classDetails",
						MyCourseActivity.this.classDetail[groupPosition][0]);}
				intent.putExtra("username", username);
				intent.putExtra("loginType", loginType);
				MyCourseActivity.this.startActivity(intent);
				return true;
			}
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.LearningRecord_layout_btn: // 播放记录
			Intent mIntent = new Intent(this, PlayrecordActivity.class);
			mIntent.putExtra("username", username);
			mIntent.putExtra("loginType", loginType);
			this.startActivity(mIntent);
			break;
		case R.id.MyfileDown_layout_btn:
			Intent intent = new Intent(this, DownloadActivity.class);
			intent.putExtra("actionName", "outline");
			intent.putExtra("username", username);
			this.startActivity(intent);
			break;
		case R.id.returnbtn:
			this.finish();
			return;
		}
	}
}
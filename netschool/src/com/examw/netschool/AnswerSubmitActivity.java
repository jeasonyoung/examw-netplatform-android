package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.adapter.SpinnerClassAdapter;
import com.examw.netschool.adapter.SpinnerLessonAdapter;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.dao.MyCourseDao;
import com.examw.netschool.model.AQTopicAdd;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.model.MyCourse;
import com.examw.netschool.util.DigestClientUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 提交新的答疑Activity.
 * 
 * @author jeasonyoung
 * @since 2015年9月23日
 */
public class AnswerSubmitActivity extends Activity implements OnClickListener {
	private static final String TAG = "AnswerSubmitActivity";
	private EditText txtTitle,txtContent;
	private ProgressDialog progressDialog;
	private String userId,lessonId;
	
	private final List<MyCourse> courses;
	private final List<Lesson> lessons;
	private final SpinnerClassAdapter classAdapter;
	private final SpinnerLessonAdapter lessonAdapter;
	/**
	 * 构造函数。
	 */
	public AnswerSubmitActivity(){
		Log.d(TAG, "初始化...");
		this.courses = new ArrayList<MyCourse>();
		this.classAdapter = new SpinnerClassAdapter(this.courses);
		
		this.lessons = new ArrayList<Lesson>();
		this.lessonAdapter = new SpinnerLessonAdapter(this.lessons);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//加载布局文件
		this.setContentView(R.layout.activity_answer_submit);
		//获取传递数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
		}
		//返回按钮处理
		final View btnBack = this.findViewById(R.id.btn_return);
		btnBack.setOnClickListener(this);
		//标题
		final TextView tvTopTitle = (TextView)this.findViewById(R.id.top_title);
		tvTopTitle.setText(R.string.answer_submit_title);
		
		//所属班级
		final Spinner classSpinner = (Spinner)this.findViewById(R.id.ddl_class);
		//设置数据适配器
		classSpinner.setAdapter(this.classAdapter);
		//设置选中事件处理
		classSpinner.setOnItemSelectedListener(this.onClassItemSelectedListener);
		//所属课程资源
		final Spinner lessonSpinner = (Spinner)this.findViewById(R.id.ddl_lesson);
		//设置数据适配器
		lessonSpinner.setAdapter(this.lessonAdapter);
		//设置选中事件处理
		lessonSpinner.setOnItemSelectedListener(this.onLessonItemSelectedListener);
		
		//标题
		this.txtTitle = (EditText)this.findViewById(R.id.txt_title);
		//内容
		this.txtContent = (EditText)this.findViewById(R.id.txt_content);
		
		//提交按钮
		final View btnSubmit = this.findViewById(R.id.btn_submit);
		btnSubmit.setOnClickListener(this);
		//
		super.onCreate(savedInstanceState);
	}
	//班级下拉选中事件处理。
	private OnItemSelectedListener onClassItemSelectedListener = new OnItemSelectedListener(){
		/*
		 * 班级下拉选中事件处理。
		 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "班级下拉选中事件处理...");
			if(courses != null && courses.size() >  position){
				final MyCourse course = courses.get(position);
				if(course == null) return;
				//清空课程资源ID
				lessonId = null;
				//异步加载数据
				new AyncLessonLoadData().execute(course.getId());
			}
		}
		/*
		 * 班级下拉未拉事件处理。
		 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
		 */
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			Log.d(TAG, "未选择班级处理..");
			//清空课程资源ID
			lessonId = null;
			//清空课程资源数据
			lessons.clear();
			//通知数据适配器更新
			lessonAdapter.notifyDataSetChanged();
		}
	};
	//课程资源下拉选中事件处理。
	private OnItemSelectedListener onLessonItemSelectedListener = new OnItemSelectedListener(){
		/*
		 * 课程资源下拉事件处理。
		 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "课程资源下拉事件处理..." + position);
			if(lessons != null && lessons.size() > position){
				final Lesson lesson = lessons.get(position);
				if(lesson == null) return;
				lessonId = lesson.getId();
			}
		}
		/*
		 * 未选择处理
		 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
		 */
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			Log.d(TAG, "课程资源下拉未选中...");
			lessonId = null;
		}
	};
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	protected void onStart() {
		Log.d(TAG, "重载开始...");
		//异步加载数据处理
		new AsyncClassLoadData().execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 按钮点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件处理..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回按钮
				Log.d(TAG, "返回按钮事件处理...");
				this.finish();
				break;
			}
			case R.id.btn_submit:{//提交按钮
				Log.d(TAG, "提交数据处理...");
				this.postDataToServer();
				break;
			}
		}
	}
	//提交数据处理
	private void postDataToServer(){
		//检查课程资源
		if(StringUtils.isBlank(lessonId)){
			Log.d(TAG, "课程资源ID不存在!");
			Toast.makeText(this, R.string.answer_submit_lesson_error, Toast.LENGTH_SHORT).show();
			return;
		}
		//检查标题
		final String title = this.txtTitle.getText().toString();
		if(StringUtils.isBlank(title)){
			Log.d(TAG, "疑问标题为空!");
			Toast.makeText(this, R.string.answer_submit_title_error, Toast.LENGTH_SHORT).show();
			return;
		}
		//检查内容
		final String content = this.txtContent.getText().toString();
		if(StringUtils.isBlank(content)){
			Log.d(TAG, "疑问内容为空!");
			Toast.makeText(this, R.string.answer_submit_content_error, Toast.LENGTH_SHORT).show();
			return;
		}
		//初始化上下文
		final AppContext appContext = (AppContext)this.getApplicationContext();
		//初始化等待动画
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, this.getResources().getText(R.string.answer_post_upload_msg), true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//启动等待动画
		this.progressDialog.show();
		//异步线程处理
		new AsyncTask<Void, Void, String>(){
			/*
			 * 后台线程处理数据。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected String doInBackground(Void... params) {
				try{
					Log.d(TAG, "向服务器提交疑问数据...");
					//初始化数据
					final AQTopicAdd topic = new AQTopicAdd();
					//设置标题
					topic.setTitle(title);
					//设置内容
					topic.setContent(content);
					//设置课程资源ID
					topic.setLessonId(lessonId);
					//设置所属机构ID
					topic.setAgencyId(Constant.DOMAIN_AGENCY_ID);
					//设置所属学员ID
					topic.setStudentId(userId);
					//检查网络连接
					if(!appContext.isNetworkConnected()){
						Log.d(TAG, "没有网络!");
						return "没有网络!";
					}
					//上传数据
					final JSONCallback<Object> callback = DigestClientUtil.sendDigestPOSTJSONRequest(Constant.DOMAIN_URL + "/api/m/aq/topic.do", topic);
					if(callback.getSuccess()){
						Log.d(TAG, "上传数据成功...");
						return null;
					}
					Log.e(TAG,  callback.getSuccess() + " / " + callback.getMsg());
					return callback.getMsg();
				}catch(Exception e){
					Log.e(TAG, "提交疑问数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前台主线程更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(String result) {
				//关闭等待动画
				progressDialog.dismiss();
				//检查反馈消息
				if(StringUtils.isNotBlank(result)){
					//显示消息
					Toast.makeText(AnswerSubmitActivity.this, result, Toast.LENGTH_SHORT).show();
				}else{
					//关闭窗体
					finish();
				}
			};
		}.execute((Void)null);
	}
	
	//异步加载班级数据处理
	private class AsyncClassLoadData extends AsyncTask<Void, Void, List<MyCourse>>{
		/*
		 * 后台线程加载班级数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<MyCourse> doInBackground(Void... params) {
			try{
				Log.d(TAG, "后台线程加载班级数据...");
				//初始化
				final MyCourseDao courseDao = new MyCourseDao();
				//返回班级数据
				return courseDao.loadCoursesByClass();
			}catch(Exception e){
				Log.e(TAG, "加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<MyCourse> result) {
			Log.d(TAG, "前台主线程更新数据...");
			//清空数据
			courses.clear();
			lessons.clear();
			//添加数据
			if(result != null && result.size() > 0){
				Log.d(TAG, "更新班级数据...");
				courses.addAll(result);
			}
			//通知班级数据适配器更新数据
			classAdapter.notifyDataSetChanged();
			//通知课时资源数据适配器更新数据
			lessonAdapter.notifyDataSetChanged();
		}
	}
	//异步加载课程资源数据处理
	private class AyncLessonLoadData extends AsyncTask<String, Void, List<Lesson>>{
		/*
		 * 后台线程加载课程资源数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<Lesson> doInBackground(String... params) {
			try{
				Log.d(TAG, "后台线程加载课程资源数据...");
				//检查数据
				if(params == null || StringUtils.isBlank(params[0])) return null;
				//初始化
				final LessonDao lessonDao = new LessonDao();
				//返回
				return lessonDao.loadLessonsByClass(params[0]);
			}catch(Exception e){
				Log.e(TAG, "加载课程资源数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<Lesson> result) {
			//重置课程资源ID
			lessonId = null;
			//清空数据
			lessons.clear();
			//添加数据
			if(result != null && result.size() > 0){
				Log.d(TAG, "更新课程资源数据...");
				lessons.addAll(result);
			}
			//通知数据适配器更新数据
			lessonAdapter.notifyDataSetChanged();
		}
	}
}
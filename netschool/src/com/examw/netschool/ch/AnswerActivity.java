package com.examw.netschool.ch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.dao.AQTopicDao;
import com.examw.netschool.model.AQTopic;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.APIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  答疑主题Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月19日
 */
public class AnswerActivity extends Activity implements OnClickListener,OnItemClickListener {
	private static final String TAG = "AnswerActivity";
	
	public static final String CONST_TOPIC_ID = "topic_Id";
	public static final String CONST_TOPIC_TITLE = "topic_title";
	public static final String CONST_TOPIC_CONTENT = "topic_content";
	
	
	private LinearLayout nodataView;
	private ProgressDialog progressDialog;
	
	private final List<AQTopic> topics;
	private final AnswerAdapter adapter;
	/**
	 * 构造函数。
	 */
	public AnswerActivity(){
		Log.d(TAG, "初始化...");
		//初始化
		this.topics = new ArrayList<AQTopic>();
		this.adapter = new AnswerAdapter(this.topics);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "重载创建...");
		//加载布局XML文件。
		this.setContentView(R.layout.activity_answer_main);
		//返回按钮
		final View btnReturn = this.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(this);
		//刷新按钮
		final View btnRefresh = this.findViewById(R.id.btn_refresh);
		btnRefresh.setOnClickListener(this);
		//提问按钮
		final View btnAnswer = this.findViewById(R.id.btn_answer);
		btnAnswer.setOnClickListener(this);
		
		//无数据View
		this.nodataView = (LinearLayout)this.findViewById(R.id.nodata_view);
		
		//列表数据View
		final ListView listView = (ListView)this.findViewById(R.id.list_answers);
		//设置数据项点击事件
		listView.setOnItemClickListener(this);
		//设置数据适配器
		listView.setAdapter(this.adapter);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "重载启动...");
		//数据加载等待
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, this.getResources().getText(R.string.progress_msg), true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//显示等待
		this.progressDialog.show();
		//异步数据加载。
		new AsyncLoadData().execute((Void)null);
	}
	/*
	 * 按钮点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件处理..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回事件处理
				Log.d(TAG, "返回事件处理...");
				this.finish();
				break;
			}
			case R.id.btn_refresh:{//刷新事件处理
				Log.d(TAG, "刷新事件处理...");
				//加载View
				if(progressDialog != null) progressDialog.show();
				//异步数据加载。
				new AsyncLoadData().execute((Void)null);
				break;
			}
			case R.id.btn_answer:{//提问按钮事件处理
				Log.d(TAG, "提问按钮事件处理...");
				//启动
				this.startActivity(new Intent(this, AnswerSubmitActivity.class));
				break;
			}
		}
	}
	/*
	 * 列表数据项点击事件处理。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "列表数据项点击事件处理(明细)...");
		if(this.topics != null && this.topics.size() > position){
			final AQTopic topic = this.topics.get(position);
			if(topic == null)return;
			//明细
			final Intent intent = new Intent(this, AnswerDetailActivity.class);
			//主题ID
			intent.putExtra(CONST_TOPIC_ID, topic.getId());
			//主题标题
			intent.putExtra(CONST_TOPIC_TITLE, topic.getTitle());
			//主题内容
			intent.putExtra(CONST_TOPIC_CONTENT, topic.getContent());
			//启动
			this.startActivity(intent);
		}
	}
	//异步数据加载。
	private class AsyncLoadData extends AsyncTask<Void, Void, List<AQTopic>>{
		private String msg;
		/*
		 * 后台线程加载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<AQTopic> doInBackground(Void... params) {
			try{
				Log.d(TAG, "后台线程加载数据...");
				//初始化
				final AQTopicDao topicDao = new AQTopicDao();
				//检查网络
				final AppContext appContext = (AppContext)getApplicationContext();
				if(appContext != null && appContext.isNetworkConnected()){
					//初始化参数
					final Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("randUserId", AppContext.getCurrentUserId());					
					//网络下载数据
					final JSONCallback<AQTopic[]> callback = new APIUtils.CallbackJSON<AQTopic[]>(AnswerActivity.this,  AQTopic[].class)
							.sendGETRequest(getResources(), R.string.api_topics_url, parameters);
					//获取数据成功
					if(callback.getSuccess()){
						//更新数据
						for(AQTopic topic : callback.getData()){
							if(topic == null || StringUtils.isBlank(topic.getId())) continue;
							if(topicDao.hasTopic(topic.getId())){//存在
								Log.d(TAG, "更新数据...");
								topicDao.update(topic);
							}else{
								Log.d(TAG, "新增数据...");
								topicDao.insert(topic);
							}
						}
					}else{
						this.msg = callback.getMsg();
						Log.d(TAG, this.msg);
					}
				}
				//加载数据
				return topicDao.loadTopics();
			}catch(Exception e){
				Log.e(TAG, "后台线程加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<AQTopic> result) {
			Log.d(TAG, "前台主线程更新数据...");
			//关闭等待动画
			if(progressDialog != null) progressDialog.dismiss();
			if(StringUtils.isNotBlank(this.msg)){
				Toast.makeText(getApplicationContext(), this.msg, Toast.LENGTH_LONG).show();
			}
			//清除数据源
			topics.clear();
			//填充数据
			if(result != null && result.size() > 0){
				Log.d(TAG, "填充数据...");
				topics.addAll(result);
			}
			//无数据View显示状态
			nodataView.setVisibility(topics.size() > 0 ? View.GONE : View.VISIBLE);
			//通知数据适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器。
	private class AnswerAdapter extends BaseAdapter{
		private static final String TAG = "AnswerAdapter";
		private final List<AQTopic> list;
		/**
		 * 构造函数。
		 * @param list
		 */
		public AnswerAdapter(List<AQTopic> list){
			Log.d(TAG, "初始化...");
			this.list = list;
		}
		/*
		 * 获取数据总数。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (this.list == null) ? 0 : this.list.size();
		}
		/*
		 * 获取数据对象。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return (this.list == null || this.list.size() < position) ? null : this.list.get(position);
		}
		/*
		 * 获取数据对象ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据项View。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取数据项View...." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建数据行..." + position);
				//加载布局文件
				convertView = LayoutInflater.from(AnswerActivity.this).inflate(R.layout.activity_answer_main_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用数据行..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((AQTopic)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//数据项View
	private class ViewHolder{
		private TextView tvTitle, tvLesson, tvContent, tvTime;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			//标题
			this.tvTitle = (TextView)convertView.findViewById(R.id.item_answer_title);
			//课程资源
			this.tvLesson = (TextView)convertView.findViewById(R.id.item_answer_lesson);
			//内容
			this.tvContent = (TextView)convertView.findViewById(R.id.item_answer_content);
			//时间
			this.tvTime = (TextView)convertView.findViewById(R.id.item_answer_time);
		}
		/**
		 * 加载数据。
		 * @param topic
		 */
		public void loadData(AQTopic topic){
			if(topic != null){
				//标题
				this.tvTitle.setText(topic.getTitle());
				//课程资源
				this.tvLesson.setText("课程资源:" + topic.getLessonName());
				//内容
				this.tvContent.setText(topic.getContent());
				//时间
				this.tvTime.setText(topic.getLastTime());
			}
		}
	}
}
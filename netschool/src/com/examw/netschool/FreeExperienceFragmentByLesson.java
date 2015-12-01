package com.examw.netschool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.FreeExperienceActivity.Search;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.util.APIUtils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 课程Frame。
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class FreeExperienceFragmentByLesson extends Fragment implements OnItemClickListener {
	private static final String TAG = "FreeExperienceFragmentByLesson";
	private final String classId;
	private final Search search;
	
	private final List<Lesson> lessons;
	private final LessonAdapter adapter;
	
	private View nodataView;
	/**
	 * 构造函数。
	 * @param classId
	 * @param search
	 */
	public FreeExperienceFragmentByLesson(String classId, Search search){
		Log.d(TAG, "初始化...");
		this.classId = classId;
		this.search = search;
		this.search.setOnClickListener(this.onSearchClickListener);
		
		this.lessons = new ArrayList<Lesson>();
		this.adapter = new LessonAdapter(this.lessons);
	}
	/*
	 * 重载创建View。
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		Log.d(TAG, "加载布局...");
		final View view = inflater.inflate(R.layout.activity_free_experience_lesson, container, false);
		//无数据View
		this.nodataView = view.findViewById(R.id.nodata_view);
		//数据列表
		final ListView listView = (ListView)view.findViewById(R.id.list_lesson);
		listView.setAdapter(this.adapter);
		listView.setOnItemClickListener(this);
		//返回
		return view;
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "异步加载数据...");
		//
		new AsyncLoadData().execute((Void)null);
	}
	/*
	 * 数据项点击事件处理。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "数据项点击事件处理..." + view);
		if(this.lessons != null && this.lessons.size() > position){
			final Lesson data = this.lessons.get(position);
			if(data != null && StringUtils.isNotBlank(data.getId()) && StringUtils.isNotBlank(data.getPriorityUrl())){
				//播放处理
				final Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
				intent.putExtra(Constant.CONST_LESSON_ID, data.getId());
				intent.putExtra(Constant.CONST_LESSON_NAME, data.getName());
				intent.putExtra(Constant.CONST_LESSON_PLAY_URL, data.getPriorityUrl());
				//跳转到播放器
				getActivity().startActivity(intent);
				//关闭当前Activity
				getActivity().finish();
			}
		}
	}
	//搜索事件处理
	private OnClickListener onSearchClickListener = new OnClickListener(){
		/*
		 * 事件处理。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			final String searchKey = search.getSearchKey();
			if(StringUtils.isBlank(searchKey)){
				//异步加载数据
				new AsyncLoadData().execute((Void)null);
				return;
			}
			//没有数据
			if(lessons == null || lessons.size() == 0) return;
			//过滤数据
			new AsyncTask<String, Void, List<Lesson>>(){
				/*
				 * 后台线程处理数据。
				 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
				 */
				@Override
				protected List<Lesson> doInBackground(String... params) {
					if(StringUtils.isBlank(params[0])) return lessons;
					//初始化 
					final  List<Lesson> taget = new ArrayList<Lesson>();
					for(Lesson data : lessons){
						if(data == null || StringUtils.isBlank(data.getName())) continue;
						if(data.getName().indexOf(params[0]) > -1){
							taget.add(data);
						}
					}
					return taget;
				}
				/*
				 * 前台主线程更新UI。
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 */
				protected void onPostExecute(List<Lesson> result) {
					Log.d(TAG, "前台数据处理...");
					//清空数据源
					lessons.clear();
					//填充结果数据
					if(result != null && result.size() > 0){
						lessons.addAll(result);
					}
					//没有数据
					nodataView.setVisibility(lessons.size() == 0 ? View.VISIBLE : View.GONE);
					//通知适配器更新
					adapter.notifyDataSetChanged();
				};
			}.execute(searchKey);
		}
	};
	//异步加载数据
	private class AsyncLoadData extends AsyncTask<Void, Void, List<Lesson>>{
		private static final String TAG = "AsyncLoadData";
		private String msg;
		/*
		 * 后台线程下载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<Lesson> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程下载数据...");
				//检查数据
				if(StringUtils.isBlank(classId)){
					Log.d(TAG, "所属班级ID为空!");
					return null;
				}
				//检查网络
				final AppContext appContext = (AppContext)getActivity().getApplicationContext();
				if(appContext == null || !appContext.isNetworkConnected()){
					Log.d(TAG, "获取上线文失败或网络不可用!");
					return null;
				}
				//初始化参数
				final Map<String, Object> parameters = new HashMap<String, Object>();
				//设置用户ID
				parameters.put("randUserId", AppContext.getCurrentUserId());
				//设置班级ID
				parameters.put("classId", classId);
				//是否免费
				parameters.put("free", true);
				//
				final JSONCallback<Lesson[]> callback = new APIUtils.CallbackJSON<Lesson[]>(getActivity(), Lesson[].class)
						.sendGETRequest(getResources(), R.string.api_lessons_url, parameters);
				//
			    if(callback.getSuccess()){
			    	return Arrays.asList(callback.getData());
			    }else{
			    	this.msg = callback.getMsg();
			    	Log.e(TAG, "下载网络异常:" + callback.getMsg());
			    }
			}catch(Exception e){
				Log.e(TAG, "异步线程下载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<Lesson> result) {
			Log.d(TAG, "前台数据处理...");
			if(StringUtils.isNotBlank(this.msg)){
				Toast.makeText(getActivity(), this.msg, Toast.LENGTH_LONG).show();
			}
			//清空数据源
			lessons.clear();
			//填充结果数据
			if(result != null && result.size() > 0){
				lessons.addAll(result);
			}
			//没有数据
			nodataView.setVisibility(lessons.size() == 0 ? View.VISIBLE : View.GONE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器。
	private class LessonAdapter extends BaseAdapter{
		private static final String TAG = "LessonAdapter";
		private final List<Lesson> list;
		/**
		 * 构造函数。
		 * @param list
		 */
		public LessonAdapter(List<Lesson> list){
			Log.d(TAG, "初始化...");
			this.list = list;
		}
		/*
		 * 获取数据总数。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (list == null) ? 0 : list.size();
		}
		/*
		 * 获取数据。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			if(this.list != null && this.list.size() > position){
				return this.list.get(position);
			}
			return null;
		}
		/*
		 * 获取数据ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据View。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取数据项View..." + position);
			ViewHolder viewHolder;
			if(convertView == null){
				Log.d(TAG, "新建数据项..." + position);
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_free_experience_lesson_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用数据项..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((Lesson)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//
	private class ViewHolder{
		private final TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.lesson_title);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(Lesson data){
			if(data != null){
				this.tvTitle.setText(data.getName());
			}
		}
	}
}
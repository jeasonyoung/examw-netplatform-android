package com.examw.netschool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.FreeExperienceActivity.Search;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.model.Exam;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.APIUtils;

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

/**
 * 考试类别下的考试Fragment。
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class FreeExperienceFragmentByExams extends Fragment implements OnItemClickListener {
	private static final String TAG = "FreeExperienceFragmentByExams";
	private final String userId;
	private final Search search;
	
	private final List<Exam> exams;
	private final ExamAdapter adapter;
	
	private View nodataView;
	/**
	 * 构造函数。
	 * @param userId
	 * @param search
	 */
	public FreeExperienceFragmentByExams(String userId,Search search){
		Log.d(TAG, "初始化...");
		this.userId = userId;
		this.search = search;
		this.search.setOnClickListener(this.onSearchClickListener);
		
		this.exams = new ArrayList<Exam>();
		this.adapter = new ExamAdapter(this.exams);
	}
	/*
	 * 重载创建View。
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "加载布局文件...");
		final View view = inflater.inflate(R.layout.activity_free_experience_exams, container, false);
		//无数据View
		 this.nodataView = view.findViewById(R.id.nodata_view);
		 //数据列表
		 final ListView listExam = (ListView)view.findViewById(R.id.list_exam);
		 listExam.setAdapter(this.adapter);
		 listExam.setOnItemClickListener(this);
		//返回
		return view;
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		Log.d(TAG, "异步加载数据...");
		//异步加载数据
		new AsyncLoadData().execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 点击事件处理。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "点击事件处理..." + view);
		if(this.exams != null && this.exams.size() > position){
			//获取数据
			final Exam exam = this.exams.get(position);
			if(exam == null) return;
			//替换Frame
			getActivity().getSupportFragmentManager()
							.beginTransaction()
							.addToBackStack(null)
							.replace(R.id.fragment_container, new FreeExperienceFragmentByPackages(userId, exam.id, this.search))
							.commit();
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
			if(exams == null || exams.size() == 0) return;
			//过滤数据
			new AsyncTask<String, Void, List<Exam>>(){
				/*
				 * 后台线程处理数据。
				 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
				 */
				@Override
				protected List<Exam> doInBackground(String... params) {
					if(StringUtils.isBlank(params[0])) return exams;
					//初始化 
					final  List<Exam> taget = new ArrayList<Exam>();
					for(Exam exam : exams){
						if(exam == null || StringUtils.isBlank(exam.name)) continue;
						if(exam.name.indexOf(params[0]) > -1){
							taget.add(exam);
						}
					}
					return taget;
				}
				/*
				 * 前台主线程更新UI。
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 */
				protected void onPostExecute(List<Exam> result) {
					Log.d(TAG, "前台数据处理...");
					//清空数据源
					exams.clear();
					//填充结果数据
					if(result != null && result.size() > 0){
						exams.addAll(result);
					}
					//没有数据
					nodataView.setVisibility(exams.size() == 0 ? View.VISIBLE : View.GONE);
					//通知适配器更新
					adapter.notifyDataSetChanged();
				};
			}.execute(searchKey);
		}
	};
	//异步加载数据
	private class AsyncLoadData extends AsyncTask<Void, Void, List<Exam>>{
		private static final String TAG = "AsyncLoadData";
		/*
		 * 后台线程加载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<Exam> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程下载数据...");
				//检查网络
				final AppContext appContext = (AppContext)getActivity().getApplicationContext();
				if(appContext == null || !appContext.isNetworkConnected()){
					Log.d(TAG, "获取上线文失败或网络不可用!");
					return null;
				}
				//初始化参数
				final Map<String, Object> parameters = new HashMap<String, Object>();
				//请求数据
				final JSONCallback<Exam[]> callback = new APIUtils.CallbackJSON<Exam[]>().sendGETRequest(getResources(),
						R.string.api_exams_url, parameters);
				//
			    if(callback.getSuccess()){
			    	return Arrays.asList(callback.getData());
			    }else{
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
		protected void onPostExecute(List<Exam> result) {
			Log.d(TAG, "前台数据处理...");
			//清空数据源
			exams.clear();
			//填充结果数据
			if(result != null && result.size() > 0){
				exams.addAll(result);
			}
			//没有数据
			nodataView.setVisibility(exams.size() == 0 ? View.VISIBLE : View.GONE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器
	private class ExamAdapter extends BaseAdapter{
		private static final String TAG = "ExamAdapter";
		private final List<Exam> list;
		/**
		 * 构造函数。
		 * @param list
		 */
		public ExamAdapter(List<Exam> list){
			Log.d(TAG, "初始化...");
			this.list = list;
		}
		/*
		 * 获取数据量。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (list == null) ? 0 : list.size();
		}
		/*
		 * 获取数据对象。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			if(list != null && list.size() > position){
				return list.get(position);
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
		 * 获取数据行。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "加载数据行..." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建数据行..." + position);
				//加载数据行布局
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_free_experience_exams_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用数据行..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((Exam)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//ViewHolder
	private class ViewHolder  {
		private static final String TAG = "ViewHolder";
		private TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			Log.d(TAG, "构造函数...");
			this.tvTitle = (TextView)convertView.findViewById(R.id.title);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(Exam data){
			if(data != null){
				//设置名称
				this.tvTitle.setText(data.name);
			}
		}
	}
}
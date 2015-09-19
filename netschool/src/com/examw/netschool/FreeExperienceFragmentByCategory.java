package com.examw.netschool;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.FreeExperienceActivity.Search;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.model.Category;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
 * 考试类别Fragment。
 * 
 * @author jeasonyoung
 * @since 2015年9月17日
 */
public class FreeExperienceFragmentByCategory extends Fragment implements OnItemClickListener {
	private static final String TAG = "FreeExperienceFragmentByCategory";
	private final String userId;
	private final Search search;
	private final List<Category> categories;
	private final CategoryAdapter adapter;
	
	private View nodataView;
	/**
	 * 构造函数。
	 * @param userId
	 * @param search
	 */
	public FreeExperienceFragmentByCategory(String userId, Search search){
		Log.d(TAG, "初始化...");
		this.userId = userId;
		this.search = search;
		this.search.setOnClickListener(this.onSearchClickListener);
		this.categories = new ArrayList<Category>();
		this.adapter = new CategoryAdapter(this.categories);
	}
	/*
	 * 重载创建View。
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 Log.d(TAG, "重载初始化...");
		 //加载布局文件
		 final View  view = inflater.inflate(R.layout.activity_free_experience_category, container, false);
		 //无数据View
		 this.nodataView = view.findViewById(R.id.nodataView);
		 //数据列表
		 final ListView listCategory = (ListView)view.findViewById(R.id.list_category);
		 listCategory.setOnItemClickListener(this);
		 listCategory.setAdapter(this.adapter);
		 //返回
		 return view;
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		Log.d(TAG, "异步线程加载数据...");
		//异步加载数据
		new AsyncLoadData().execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 数据项点击事件处理。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "点击事件处理..." + view);
		if(this.categories != null && this.categories.size() > position){
			final Category data = this.categories.get(position);
			if(data == null || StringUtils.isBlank(data.getId())) return;
			//替换Frame
			getActivity().getSupportFragmentManager()
				.beginTransaction()
				.addToBackStack(null)
				.replace(R.id.fragment_container, new FreeExperienceFragmentByExams(userId, data.getId(),this.search))
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
			if(categories == null || categories.size() == 0) return;
			//过滤数据
			new AsyncTask<String, Void, List<Category>>(){
				/*
				 * 后台线程处理数据。
				 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
				 */
				@Override
				protected List<Category> doInBackground(String... params) {
					if(StringUtils.isBlank(params[0])) return categories;
					//初始化 
					final  List<Category> taget = new ArrayList<Category>();
					for(Category category : categories){
						if(category == null || StringUtils.isBlank(category.getName())) continue;
						if(category.getName().indexOf(params[0]) > -1){
							taget.add(category);
						}
					}
					return taget;
				}
				/*
				 * 前台主线程更新UI。
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 */
				protected void onPostExecute(List<Category> result) {
					Log.d(TAG, "前台数据处理...");
					//清空数据源
					categories.clear();
					//填充结果数据
					if(result != null && result.size() > 0){
						categories.addAll(result);
					}
					//没有数据
					nodataView.setVisibility(categories.size() == 0 ? View.VISIBLE : View.GONE);
					//通知适配器更新
					adapter.notifyDataSetChanged();
				};
			}.execute(searchKey);
		}
	};
	//异步线程加载数据。
	private class AsyncLoadData extends AsyncTask<Void, Void, List<Category>>{
		/*
		 * 后台线程下载数据处理。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<Category> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程下载数据...");
				final AppContext appContext = (AppContext)getActivity().getApplicationContext();
				//检查网络
				if(appContext == null || !appContext.isNetworkConnected()){
					Log.d(TAG, "获取上线文失败或网络不可用!");
					return null;
				}
				//查询数据
			    final String result =	DigestClientUtil.sendDigestGetRequest(Constant.DOMAIN_URL + "/api/m/categories.do");
			    if(StringUtils.isBlank(result)) return null;
			    // 解析字符串
				final Gson gson = new Gson();
				final Type type = new TypeToken<JSONCallback<Category[]>>(){}.getType();
				final JSONCallback<Category[]> callback = gson.fromJson(result, type);
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
		 * 前台主线程更新数据处理。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<Category> result) {
			Log.d(TAG, "前台数据处理...");
			//清空数据源
			categories.clear();
			//填充结果数据
			if(result != null && result.size() > 0){
				categories.addAll(result);
			}
			//没有数据
			nodataView.setVisibility(categories.size() == 0 ? View.VISIBLE : View.GONE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器
	private class CategoryAdapter extends BaseAdapter{
		private static final String TAG = "CategoryAdapter";
		private final List<Category> list;
		/**
		 * 构造函数。
		 * @param list
		 */
		public CategoryAdapter(List<Category> list){
			this.list = list;
		}
		/*
		 * 获取数据量。
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
		 * 获取数据ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据项View
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "加载数据行..." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建数据行..." + position);
				//加载数据行布局
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_free_experience_category_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用数据行..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((Category)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//ViewHolder
	private class ViewHolder {
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
		public void loadData(Category data){
			if(data != null){
				//设置名称
				this.tvTitle.setText(data.getName());
			}
		}
	}
}
package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
/**
 * 引导页Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月25日
 */
public class GuideActivity extends Activity {
	private static final String TAG = "GuideActivity";
	private View btnStartView;
	
	private final List<View> views;
	private final GuidePagerAdapter adapter;
	/**
	 * 构造函数。
	 */
	public GuideActivity(){
		Log.d(TAG, "初始化...");
		this.views = new ArrayList<View>();
		this.adapter = new GuidePagerAdapter(this.views);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//设置布局文件
		this.setContentView(R.layout.activity_guide);
		//引导ViewPaper
		final ViewPager viewPager = (ViewPager)this.findViewById(R.id.guide_pages);
		//设置数据适配器
		viewPager.setAdapter(this.adapter);
		//设置事件处理
		viewPager.setOnPageChangeListener(this.onPageChangeListener);
		
		//开始按钮
		this.btnStartView = this.findViewById(R.id.btn_guide_start);
		//设置事件处理
		this.btnStartView.setOnClickListener(this.onClickListener);
		//
		super.onCreate(savedInstanceState);
	}
	//事件处理
	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener(){
		
		@Override
		public void onPageScrollStateChanged(int arg0) { }
	
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) { }
		
		@Override
		public void onPageSelected(int index) {
			if(btnStartView != null){
				//设置按钮是否可见
				btnStartView.setVisibility(views.size() - 1 == index ? View.VISIBLE : View.GONE);
			}
		}
	};
	//点击事件处理
	private OnClickListener onClickListener = new OnClickListener() {
		//初始化
		final AppContext appContext = (AppContext)getApplicationContext();
		
		@Override
		public void onClick(View v) {
			Log.d(TAG, "点击事件处理..." + v);
			//获取版本 
			final int versionCode = appContext.getVersionCode();
			if(versionCode > 0){
				//初始化存储
				final SharedPreferences preferences = getSharedPreferences(Constant.PREFERENCES_CONFIG_GUIDEFILE, Context.MODE_PRIVATE);
				//设置版本已不是第一次
				preferences.edit()
								  .putBoolean(Constant.PREFERENCES_CONFIG_GUIDEFILE_ISFIRST+ versionCode, true)
								  .commit();
			}
			//启动登录
			startActivity(new Intent(GuideActivity.this, LoginActivity.class));
			//关闭当前Activity
			finish();
		}
	};
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//初始化
		final LayoutInflater layoutInflater = this.getLayoutInflater();
		//异步线程处理
		new AsyncTask<Void, Void, List<View>>() {
			/*
			 * 后台线程加载处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected List<View> doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载数据...");
					//初始化
					final int [] views_array = new int[]{
							R.layout.guide_page1,
							R.layout.guide_page2,
							R.layout.guide_page3,
							R.layout.guide_page4,
							R.layout.guide_page5
					};
					//初始化
					final List<View> list = new ArrayList<View>();
					//加载数据
					if(layoutInflater != null){
						for(int res : views_array){
							final View view = layoutInflater.inflate(res, null);
							if(view == null) continue;
							list.add(view);
						}
					}
					//返回
					return list;
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
			protected void onPostExecute(List<View> result) {
				Log.d(TAG, "前台主线程更新数据...");
				//清除数据
				views.clear();
				//添加数据
				if(result != null && result.size() > 0){
					Log.d(TAG, "添加数据处理...");
					views.addAll(result);
				}
				//通知数据适配器更新数据
				adapter.notifyDataSetChanged();
			}
		}.execute((Void)null);
		//
		super.onStart();
	};
	//数据适配器。
	private class GuidePagerAdapter extends PagerAdapter{
		private static final String TAG = "GuidePagerAdapter";
		private final List<View> list;  
		/**
		 * 构造函数。
		 * @param views
		 */
	    public GuidePagerAdapter(List<View> views) {  
	        Log.d(TAG, "初始化...");
	        this.list = views;
	    }
	    /*
	     * 获取数据总数。
	     * @see android.support.v4.view.PagerAdapter#getCount()
	     */
	    @Override  
	    public int getCount() {           
	        return  this.list == null ? 0 : this.list.size();
	    }
	    /*
	     * 对象比较。
	     * @see android.support.v4.view.PagerAdapter#isViewFromObject(android.view.View, java.lang.Object)
	     */
	    @Override  
	    public boolean isViewFromObject(View arg0, Object arg1) {             
	        return arg0 == arg1;//官方提示这样写  
	    }
	    /*
	     * 删除数据。
	     * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
	     */
	    @Override  
	    public void destroyItem(ViewGroup container, int position, Object object){
	    	Log.d(TAG, "删除数据..." + position);
	    	if(this.list.size() >  position){
	    		final View view = this.list.get(position);
	    		if(view != null){
	    			container.removeView(view);//删除页卡  
	    		}	
	    	}
	    }  
	    /*
	     * 实例化。
	     * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
	     */
	    @Override  
	    public Object instantiateItem(ViewGroup container, int position) {  //这个方法用来实例化页卡    
	    	Log.d(TAG, "加载View..." + position);
	    	if(this.list != null && this.list.size() > position){
	    		final View view = this.list.get(position);
	    		if(view != null){
	    			//添加到View
	    			container.addView(view, 0);
	    			//返回
	    			return view;
	    		}
	    	}
	    	return null;
	    }
	}
}
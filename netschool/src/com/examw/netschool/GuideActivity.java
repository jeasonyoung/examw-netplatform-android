package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;

/**
 * 引导页Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class GuideActivity extends Activity {
	private static final String TAG = "GuideActivity";
	private ImageButton startBtn;	//立即体验
	private SharedPreferences b;	//存储第一次启动信息
	private ViewPager guidePage;	//引导界面轮转  
	private ArrayList<View> d;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle paramBundle)
	{
	    super.onCreate(paramBundle);
	    Log.d(TAG, "初始化...");
	    
	    requestWindowFeature(1);
	    this.d = new ArrayList<View>();
	    
	    final LayoutInflater localLayoutInflater = getLayoutInflater();
	    this.d.add(localLayoutInflater.inflate(R.layout.guide_page1, null));
	    this.d.add(localLayoutInflater.inflate(R.layout.guide_page2, null));
	    this.d.add(localLayoutInflater.inflate(R.layout.guide_page3, null));
	    this.d.add(localLayoutInflater.inflate(R.layout.guide_page4, null));
	    this.d.add(localLayoutInflater.inflate(R.layout.guide_page5, null));
	    
	    this.setContentView(R.layout.activity_guide);
	    
	    this.guidePage = ((ViewPager)this.findViewById(R.id.guidePage));
	    this.startBtn = ((ImageButton)this.findViewById(R.id.starBtn));
	    
	    this.b = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_GUIDEFILE, Context.MODE_PRIVATE);
	    final int versionCode = ((AppContext)this.getApplication()).getVersionCode();
	    this.startBtn.setOnClickListener(new OnClickListener(){
	    	@Override
	    	public void onClick(View v) {
	    		//设置版本
	    		b.edit().putBoolean(Constant.PREFERENCES_CONFIG_GUIDEFILE_ISFIRST+ versionCode, true).commit();
	    		//启动
	    	    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
	    	    //关闭
	    	    finish();
	    	}
	    });
	   
	    final PagerAdapter pagerAdapter = new MyViewPagerAdapter(this.d);
	    this.guidePage.setAdapter(pagerAdapter);
	    
	    this.guidePage.setOnPageChangeListener(new OnPageChangeListener() {
			
	    	@Override
			public void onPageSelected(int arg0) {
				if(arg0==4)
				{
					GuideActivity.this.startBtn.setVisibility(View.VISIBLE);
				}else
				{
					GuideActivity.this.startBtn.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }
			
			@Override
			public void onPageScrollStateChanged(int arg0) { }
		});
	  }

	 private class MyViewPagerAdapter extends PagerAdapter{  
	    private List<View> mListViews;  
	  
	    //构造方法，参数是我们的页卡，这样比较方便。  
	    public MyViewPagerAdapter(List<View> mListViews) {  
	        this.mListViews = mListViews;
	    }
	    
	    @Override  
	    public void destroyItem(ViewGroup container, int position, Object object)   {     
	        container.removeView(mListViews.get(position));//删除页卡  
	    }  
	    
	    @Override  
	    public Object instantiateItem(ViewGroup container, int position) {  //这个方法用来实例化页卡         
	         container.addView(mListViews.get(position), 0);//添加页卡  
	         return mListViews.get(position);  
	    }
	    
	    @Override  
	    public int getCount() {           
	        return  mListViews.size();//返回页卡的数量  
	    }
	    
	    @Override  
	    public boolean isViewFromObject(View arg0, Object arg1) {             
	        return arg0 == arg1;//官方提示这样写  
	    }
	 }
}
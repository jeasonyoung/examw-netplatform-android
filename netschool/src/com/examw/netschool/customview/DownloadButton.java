package com.examw.netschool.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.examw.netschool.R;

public class DownloadButton extends LinearLayout{
	private ImageView iv;
	private TextView tv;
	public DownloadButton(Context context) {
		// TODO Auto-generated constructor stub
		this(context,null);
	}
	public DownloadButton(Context context, AttributeSet  attrs) {
		// TODO Auto-generated constructor stub
		 super(context, attrs); 
	        // 导入布局 
	        LayoutInflater.from(context).inflate(R.layout.download_pause_continue_btn, this, true); 
	        iv = (ImageView) findViewById(R.id.btnImg); 
	        tv = (TextView) findViewById(R.id.btnTxt);  
	       
	}
	 /**
     * 设置图片资源
     */ 
    public void setImageResource(int resId) { 
        iv.setImageResource(resId); 
    } 
 
    /**
     * 设置显示的文字
     */ 
    public void setText(String text) { 
        tv.setText(text); 
    }  
	public void setText(int resId){
		tv.setText(resId);
	}
}

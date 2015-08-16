package com.examw.netschool;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class ReturnBtnClickListener implements OnClickListener {
	private Activity context;
	public ReturnBtnClickListener(Activity context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(context!=null)
		{
			context.finish();
			context=null;
		}
	}
}

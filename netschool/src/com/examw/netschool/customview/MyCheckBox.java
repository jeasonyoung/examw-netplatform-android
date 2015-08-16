package com.examw.netschool.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class MyCheckBox extends CheckBox{
	private String value;
	private int flag;
	public MyCheckBox(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}
	public MyCheckBox(Context context, AttributeSet attrs)
	{
		super(context,attrs);
	}
	public MyCheckBox(Context context, AttributeSet attrs, int defStyle)
	{
		super(context,attrs,defStyle);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	@Override
	public void setChecked(boolean checked) {
		// TODO Auto-generated method stub
		super.setChecked(checked);
	}
}

package com.examw.netschool.customview;

import android.content.Context;
import android.widget.RadioButton;

public class MyRadioButton extends RadioButton{
	private String value;
	public MyRadioButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
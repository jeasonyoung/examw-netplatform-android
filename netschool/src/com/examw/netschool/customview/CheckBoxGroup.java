package com.examw.netschool.customview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class CheckBoxGroup extends LinearLayout{
	private List<MyCheckBox> checkboxList = new ArrayList<MyCheckBox>();
	private StringBuffer buf = new StringBuffer();
	public CheckBoxGroup(Context context) {
		super(context);
	}
	public CheckBoxGroup(Context context, AttributeSet attrs)
	{
		super(context,attrs);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void addView(View child, int index) {
		LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
		lp.setMargins(0, 0, 0, 10);
		child.setLayoutParams(lp);
		super.addView(child, index);
		checkboxList.add((MyCheckBox) child);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void addView(View child) {
		LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
		lp.setMargins(0, 0, 0, 10);
		child.setLayoutParams(lp);
		super.addView(child);
		checkboxList.add((MyCheckBox) child);
	}
	public String getValue()
	{
		if(buf.length()>0)
		{
			buf.delete(0, buf.length());
		}
		for(MyCheckBox cb: checkboxList)
		{
			if(cb.isChecked())
			{
				buf.append(cb.getValue()).append(",");
			}
		}
		return buf.length()>0?buf.deleteCharAt(buf.length()-1).toString():"";
	}
	@Override
	public void removeAllViews() {
		super.removeAllViews();
		checkboxList.removeAll(checkboxList);
	}
	@Override
	public void removeViewAt(int index) {
		super.removeViewAt(index);
		checkboxList.remove(index);
	}
	public void clearCheck() {
		for(MyCheckBox cb: checkboxList)
		{
			if(cb.isChecked())
			{
				cb.setFlag(-1);
				cb.setChecked(false);
			}
		}
	}
}
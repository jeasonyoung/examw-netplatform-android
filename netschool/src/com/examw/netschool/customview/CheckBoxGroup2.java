package com.examw.netschool.customview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class CheckBoxGroup2 extends LinearLayout{
	private List<OptionLayout> checkboxList = new ArrayList<OptionLayout>();
	private StringBuffer buf = new StringBuffer();
	public CheckBoxGroup2(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}
	public CheckBoxGroup2(Context context, AttributeSet attrs)
	{
		super(context,attrs);
	}
	@Override
	public void addView(View child, int index) {
		// TODO Auto-generated method stub
		super.addView(child, index);
		checkboxList.add((OptionLayout) child);
	}
	@Override
	public void addView(View child) {
		// TODO Auto-generated method stub
		super.addView(child);
		checkboxList.add((OptionLayout) child);
	}
	public String getValue()
	{
		if(buf.length()>0)
		{
			buf.delete(0, buf.length());
		}
		for(OptionLayout cb: checkboxList)
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
		// TODO Auto-generated method stub
		super.removeAllViews();
		checkboxList.removeAll(checkboxList);
	}
	
	//����ѡ�л���
	public void setOnlyOneCheck(OptionLayout box)
	{
		for(OptionLayout cb: checkboxList)
		{
			if(box.getId() != cb.getId() && cb.getType()==OptionLayout.RADIO_BUTTON)
			{
				cb.setChecked(false);
			}
		}
	}
	public void clearCheck()
	{
		for(OptionLayout cb: checkboxList)
		{
			cb.setChecked(false);
		}
	}
	public void forbidden(boolean flag)
	{
		for(OptionLayout cb: checkboxList)
		{
			cb.setClickable(flag);
		}
	}
	public int getChildCount()
	{
		if(checkboxList!=null)
		return checkboxList.size();
		return 0;
	}
	public OptionLayout getChildAt(int index)
	{
		if(checkboxList!=null)
			return checkboxList.get(index);
		return null;
	}
	/**
	 * 设置特定答案值的字体的颜�?
	 * @param color
	 * @param value
	 */
	public void setFontColor(int color,String value)
	{
		for(OptionLayout cb: checkboxList)
		{
			if(value.contains(cb.getValue()))
			{
				cb.setFontColor(color);
			}
		}
	}
}

package com.examw.netschool;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {

	private Drawable icon;
	private String title;
	private OnClickListener clickListener;

	public ActionItem() {
		// TODO Auto-generated constructor stub
	}

	public ActionItem(Drawable icon) {
		this.icon = icon;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public OnClickListener getClickListener() {
		return clickListener;
	}

	public void setClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

}

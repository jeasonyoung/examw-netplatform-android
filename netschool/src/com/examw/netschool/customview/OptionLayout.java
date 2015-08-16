package com.examw.netschool.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.examw.netschool.R;

public class OptionLayout extends LinearLayout {
	public ImageView button_line;
	public CheckBox check_option;
	//private Context context;
	public ImageTextView image_text_view;
	public boolean isclick = true;
	private View view;
	private int type;
	public static final int RADIO_BUTTON = 1;
	public static final int CHECK_BOX = 2;
	private String value;
	private int flag;

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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public OptionLayout(Context paramContext) {
		super(paramContext);
	}

	public OptionLayout(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		//this.context = paramContext;
		this.view = LayoutInflater.from(paramContext).inflate(R.layout.options, this, false);
		init();
	}

	private void init() {
		this.check_option = ((CheckBox) this.view.findViewById(R.id.check_option));
		this.image_text_view = (ImageTextView) this.view.findViewById(R.id.image_textview);
		this.button_line = ((ImageView) this.view.findViewById(R.id.button_line));
		this.check_option.setChecked(false);
		this.check_option.setFocusable(false);
		this.check_option.setClickable(false);
		this.setBackgroundResource(R.drawable.layout_selector_background);
		this.setClickable(true);
		addView(this.view);
		this.view = null;
		System.gc();
	}

	public boolean isChecked() {
		return this.check_option.isChecked();
	}

	public boolean isIsclick() {
		return this.isclick;
	}

	public void reSetButtonDrawable(int paramInt) {
		this.check_option.setButtonDrawable(paramInt);
		this.check_option.setChecked(false);
	}

	public void resetColor() {
		this.image_text_view.setTextColor(getResources().getColor(R.color.black));
	}

	public void setButtonClickable(boolean paramBoolean) {
		this.isclick = false;
	}

	public void setButtonDrawable(int paramInt) {
		this.check_option.setButtonDrawable(paramInt);
	}

	public void setButtonIsClickable(boolean paramBoolean) {
		this.check_option.setClickable(paramBoolean);
		this.isclick = paramBoolean;
	}

	public boolean setCheckBoxClicked() {
		if (this.check_option.isChecked()) {
			if (this.isclick) this.check_option.setChecked(false);
			return false;
		}
		if (this.isclick) this.check_option.setChecked(true);
		return true;
	}

	public void setCheckBoxIsClicked(int paramInt) {
		if (this.isclick) {
			if (this.check_option.isChecked()) this.check_option.setChecked(false);
		} else {
			return;
		}
		this.check_option.setChecked(true);
	}

	public void setChecked(boolean paramBoolean) {
		this.check_option.setChecked(paramBoolean);
		this.check_option.setFocusable(false);
	}

	public void setFontColor(int paramInt) {
		this.image_text_view.setTextColor(paramInt);
	}

	public void setFontSize(float size) {
		this.image_text_view.setTextSize(size);
	}

	public void setText(String text) {
		this.image_text_view.setText(text);
	}
	// public void setOptionContent(String paramString1, String paramString2)
	// {
	// this.image_text_view.init(paramString1, paramString2);
	// }
}
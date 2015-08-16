package com.examw.netschool.quickactions;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class CustomPopupWindow
{
  protected final View anchor;
  private Drawable background = null;
  private View root;
  protected final PopupWindow window;
  protected final WindowManager windowManager;

  @SuppressLint("ClickableViewAccessibility") 
  public CustomPopupWindow(View paramView)
  {
    this.anchor = paramView;
    this.window = new PopupWindow(paramView.getContext());
    this.window.setTouchInterceptor(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        if (paramAnonymousMotionEvent.getAction() == 4)
        {
          CustomPopupWindow.this.window.dismiss();
          return true;
        }
        return false;
      }
    });
    this.windowManager = ((WindowManager)paramView.getContext().getSystemService("window"));
    onCreate();
  }

  public void dismiss()
  {
    this.window.dismiss();
  }

  protected void onCreate()
  {
  }

  protected void onShow()
  {
  }

  @SuppressWarnings("deprecation")
  protected void preShow(){
	    if (this.root == null) throw new IllegalStateException("setContentView was not called with a view to display.");
	    onShow();
	    if (this.background == null) this.window.setBackgroundDrawable(new BitmapDrawable());
	    while (true)
	    {
	      this.window.setWidth(-2);
	      this.window.setHeight(-2);
	      this.window.setTouchable(true);
	      this.window.setFocusable(true);
	      this.window.setOutsideTouchable(true);
	      this.window.setContentView(this.root);
	      return;
	//      this.window.setBackgroundDrawable(this.background);
	    }  
  }

  public void setBackgroundDrawable(Drawable paramDrawable)
  {
    this.background = paramDrawable;
  }

  public void setContentView(int paramInt)
  {
    setContentView(((LayoutInflater)this.anchor.getContext().getSystemService("layout_inflater")).inflate(paramInt, null));
  }

  public void setContentView(View paramView)
  {
    this.root = paramView;
    this.window.setContentView(paramView);
  }

  public void setOnDismissListener(PopupWindow.OnDismissListener paramOnDismissListener)
  {
    this.window.setOnDismissListener(paramOnDismissListener);
  }

  public void showDropDown()
  {
    showDropDown(0, 0);
  }

  public void showDropDown(int paramInt1, int paramInt2)
  {
    preShow();
    this.window.setAnimationStyle(2131230722);
    this.window.showAsDropDown(this.anchor, paramInt1, paramInt2);
  }

  public void showLikeQuickAction()
  {
    showLikeQuickAction(0, 0);
  }

  @SuppressWarnings("deprecation")
  public void showLikeQuickAction(int paramInt1, int paramInt2)
  {
    preShow();
    this.window.setAnimationStyle(2131230728);
    int[] arrayOfInt = new int[2];
    this.anchor.getLocationOnScreen(arrayOfInt);
    Rect localRect = new Rect(arrayOfInt[0], arrayOfInt[1], arrayOfInt[0] + this.anchor.getWidth(), arrayOfInt[1] + this.anchor.getHeight());
    this.root.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
    this.root.measure(-2, -2);
    int i = this.root.getMeasuredWidth();
    int j = this.root.getMeasuredHeight();
    int k = paramInt1 + (this.windowManager.getDefaultDisplay().getWidth() - i) / 2;
    int m = paramInt2 + (localRect.top - j);
    if (j > localRect.top)
    {
      m = paramInt2 + localRect.bottom;
      this.window.setAnimationStyle(2131230724);
    }
    this.window.showAtLocation(this.anchor, 0, k, m);
  }
}
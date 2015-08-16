package com.examw.netschool;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class QuickActionPopupWindow extends PopupWindow {

	private View root;

	private ImageView mArrowUp;
	private HorizontalScrollView scroll;
	//private ImageView mArrowDown;

	private Animation mTrackAnim;

	private LayoutInflater inflater;

	private Context context;

	private View anchor;

	private PopupWindow window;

	private Drawable background = null;

	private WindowManager windowManager;

	public static final int ANIM_GROW_FROM_LEFT = 1;

	public static final int ANIM_GROW_FROM_RIGHT = 2;

	public static final int ANIM_GROW_FROM_CENTER = 3;

	public static final int ANIM_AUTO = 4;

	private int animStyle;
	private boolean animateTrack;
	private ViewGroup mTrack;
	private ArrayList<ActionItem> actionItems;
	/**
	 * 为了公用一个popwindow
	 * @param context
	 */
	public QuickActionPopupWindow(Context context) {
		super(context);
		this.window = new PopupWindow(context);
		/**
		 * 在popwindow外点击即关闭该window
		 */
		window.setTouchInterceptor(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View view, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {

					// 让其消失
					QuickActionPopupWindow.this.window.dismiss();
					return true;

				}

				return false;
			}
		});
		this.context = context;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		actionItems = new ArrayList<ActionItem>();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (ViewGroup) inflater.inflate(R.layout.quickbar, null);
		// 上下两个箭头
		//mArrowDown = (ImageView) root.findViewById(R.id.arrow_down);
		mArrowUp = (ImageView) root.findViewById(R.id.arrow_up);
		scroll = (HorizontalScrollView) root.findViewById(R.id.scroll);
		setContentView(root);
		mTrackAnim = AnimationUtils.loadAnimation(context, R.anim.rail);
		/**
		 * 设置加速效果
		 */
		mTrackAnim.setInterpolator(new Interpolator() {

			@Override
			public float getInterpolation(float t) {
				final float inner = (t * 1.55f) - 1.1f;
				return 1.2f - inner * inner;
			}
		});
		// 这个是弹出窗口内的水平布局
		mTrack = (ViewGroup) root.findViewById(R.id.tracks);
		animStyle = ANIM_AUTO;// 设置动画风格
		animateTrack = true;
	}
	
	public QuickActionPopupWindow(View anchor) {
		super(anchor);
		this.anchor = anchor;
		this.window = new PopupWindow(anchor.getContext());
		/**
		 * 在popwindow外点击即关闭该window
		 */
		window.setTouchInterceptor(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					// 让其消失
					QuickActionPopupWindow.this.window.dismiss();
					return true;
				}
				return false;
			}
		});
		context = anchor.getContext();
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		actionItems = new ArrayList<ActionItem>();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (ViewGroup) inflater.inflate(R.layout.quickbar, null);
		// 上下两个箭头
		//mArrowDown = (ImageView) root.findViewById(R.id.arrow_down);
		mArrowUp = (ImageView) root.findViewById(R.id.arrow_up);
		
		setContentView(root);

		mTrackAnim = AnimationUtils.loadAnimation(context, R.anim.rail);
		/**
		 * 设置加速效果
		 */
		mTrackAnim.setInterpolator(new Interpolator() {
			@Override
			public float getInterpolation(float t) {
				final float inner = (t * 1.55f) - 1.1f;
				return 1.2f - inner * inner;
			}
		});

		// 这个是弹出窗口内的水平布局
		mTrack = (ViewGroup) root.findViewById(R.id.tracks);
		animStyle = ANIM_AUTO;// 设置动画风格
		animateTrack = true;
	}

	/**
	 * 设置一个flag 来标识动画显示
	 * 
	 * @param animateTrack
	 */
	public void animateTrack(boolean animateTrack) {
		this.animateTrack = animateTrack;
	}

	/**
	 * 设置动画风格
	 * 
	 * @param animStyle
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	/**
	 * 增加一个Action
	 * 
	 * @param actionItem
	 */
	public void addActionItem(ActionItem actionItem) {
		actionItems.add(actionItem);
	}

	/**
	 * 弹出窗体
	 */
	@SuppressWarnings("deprecation")
	public void show() {
		preShow();
		int[] location = new int[2];
		// 得到anchor的位置
		anchor.getLocationOnScreen(location);

		// 以anchor的位置构造一个矩形
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth = root.getMeasuredWidth();
		int rootHeight = root.getMeasuredHeight();

		// 得到屏幕的宽
		int screenWidth = windowManager.getDefaultDisplay().getWidth();

		// 设置弹窗弹出的位置的X y
		int xPos = (screenWidth - rootWidth) / 2;
		int yPos = anchorRect.top - rootHeight;

		boolean onTop = true;
		// 在底部弹出
		if (rootHeight > anchorRect.top) {
			yPos = anchorRect.bottom;
			onTop = false;
		}

		 //根据弹出位置，设置不同的方向箭头图片
//		 showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up),
//		 anchorRect.centerX());

		// 设置弹出动画风格
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		// 创建action list
		createActionList();
		// 在指定位置弹出弹窗
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);

		// 设置弹窗内部的水平布局的动画
		if (animateTrack) {
			mTrack.startAnimation(mTrackAnim);
		}

	}
	@SuppressWarnings("deprecation")
	public void show(View v) {

		preShow();
		int[] location = new int[2];
		// 得到anchor的位置
		v.getLocationOnScreen(location);

		// 以anchor的位置构造一个矩形
		Rect anchorRect = new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		
		int rootWidth = root.getMeasuredWidth();
		int rootHeight = root.getMeasuredHeight();
		
		// 得到屏幕的宽,高
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		
		// 设置弹窗弹出的位置的X y
		int xPos = (screenWidth - rootWidth) / 2;
		int yPos = anchorRect.bottom;
		// 初始化背景
		scroll.setBackgroundResource(R.drawable.pop_back);	//开始是向上的箭头
		boolean onTop = true;
		// 在底部弹出
		if (anchorRect.top > screenHeight/2) {
			yPos = anchorRect.top - rootHeight;
			scroll.setBackgroundResource(R.drawable.pop_back2);
			onTop = false;
		}
//		System.out.println("view 的位置:"+"x1:"+location[0]+",y1:"+location[1]+",x2:"+(location[0]
//				+ v.getWidth())+",y2:"+(location[1] + v.getHeight()));
//		System.out.println("root的大小:width"+rootWidth+",height:"+rootHeight);
//		System.out.println("矩形顶部:"+anchorRect.top+",矩形底部:"+anchorRect.bottom);
//		System.out.println("x:"+xPos+",y:"+yPos);
		
		// 设置弹出动画风格
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		// 创建action list
		createActionList();
		
		// 在指定位置弹出弹窗
		window.showAtLocation(v, Gravity.NO_GRAVITY, xPos, yPos);

		// 设置弹窗内部的水平布局的动画
		if (animateTrack) {
			mTrack.startAnimation(mTrackAnim);
		}
	}
	/**
	 * 预处理窗口
	 */
	@SuppressWarnings("deprecation")
	protected void preShow() {
		if (root == null) {
			throw new IllegalStateException("需要为弹窗设置布局");
		}

		if (background == null) {
			window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			window.setBackgroundDrawable(background);
		}

		// 设置宽度
		window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		// 设置高度
		window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

		window.setTouchable(true);
		window.setFocusable(true);
		window.setOutsideTouchable(true);

		// 指定布局
		window.setContentView(root);
	}
	/**
	 * 设置动画风格
	 * 
	 * @param screenWidth
	 * @param requestedX
	 * @param onTop
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;
		switch (animStyle) {
			case ANIM_GROW_FROM_LEFT:
				window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Left : R.style.Animations_PopDownMenu_Left);
				break;
	
			case ANIM_GROW_FROM_RIGHT:
				window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right : R.style.Animations_PopDownMenu_Right);
				break;
	
			case ANIM_GROW_FROM_CENTER:
				window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Center : R.style.Animations_PopDownMenu_Center);
				break;
	
			case ANIM_AUTO:
				if (arrowPos < screenWidth / 4) {
					window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Left : R.style.Animations_PopDownMenu_Left);
				} else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
					window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Center : R.style.Animations_PopDownMenu_Center);
				} else {
					window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right : R.style.Animations_PopDownMenu_Right);
				}
				break;
		}
	}

	/**
	 * 创建Action List
	 */
	private void createActionList() {
		View view;
		String title;
		Drawable icon;
		OnClickListener clickListener;

		int index = 1;
		if(mTrack.getChildCount()>2)
		{
			return;
		}
		for (int i = 0; i < actionItems.size(); i++) {

			title = actionItems.get(i).getTitle();
			icon = actionItems.get(i).getIcon();

			clickListener = actionItems.get(i).getClickListener();
			// 得到Action item
			view = getActionItem(title, icon, clickListener);
			view.setFocusable(true);
			view.setClickable(true);
			mTrack.addView(view, index);
			index++;
		}

	}

	/**
	 * 得到Action Item
	 * 
	 * @param title
	 * @param icon
	 * @param listener
	 * @return
	 */
	private View getActionItem(String title, Drawable icon, OnClickListener listener) {
		// 装载Action布局
		LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.action_item, null);
		ImageView img_icon = (ImageView) linearLayout.findViewById(R.id.icon);
		TextView tv_title = (TextView) linearLayout.findViewById(R.id.title);
		if (icon != null) {
			img_icon.setImageDrawable(icon);
		} else {
			img_icon.setVisibility(View.GONE);
		}
		if (tv_title != null) {
			tv_title.setText(title);
		} 
		if(listener!=null)
		{
			linearLayout.setOnClickListener(listener);
		}
		return linearLayout;
	}

//	 /**
//	 * 显示箭头
//	 *
//	 * @param whichArrow箭头资源id
//	 * @param requestedX
//	 * 距离屏幕左边的距离
//	 */
//	 private void showArrow(int whichArrow, int requestedX) {
//	
//	 final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp
//	 : mArrowDown;
//	 final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown
//	 : mArrowUp;
//	 final int arrowWidth = mArrowUp.getMeasuredWidth();
//	 showArrow.setVisibility(View.VISIBLE);
//	 ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)
//	 showArrow
//	 .getLayoutParams();
//	 // 以此设置距离左边的距离
//	 param.leftMargin = requestedX - arrowWidth / 2;
//	 hideArrow.setVisibility(View.INVISIBLE);
//	
//	 }
	 public void dismiss()
	 {
		 this.window.dismiss();
	 }
}
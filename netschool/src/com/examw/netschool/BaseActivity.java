package com.examw.netschool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity{
	protected static String username;
	protected static String loginType;
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "设置").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST+2, 2, "帮助").setIcon(android.R.drawable.ic_menu_help);
		//menu.add(Menu.NONE, Menu.FIRST+3, 3, "关于").setIcon(android.R.drawable.ic_menu_info_details);
		//menu.add(Menu.NONE, Menu.FIRST+4, 4, "注销").setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		 switch(item.getItemId()){
		 case Menu.FIRST+1:
			 //设置
			 startSettingActivity();
		 	 break;
		 case Menu.FIRST+2:
			 //帮助
			 this.startActivity(new Intent(this,HelpActivity.class));
			 break;
//		 case Menu.FIRST+3:
//			 Toast.makeText(this, "删除菜单被点击了", Toast.LENGTH_LONG).show();
//		 	break;
//		 case Menu.FIRST+4:
//			 showDialog();
//		 	break;
		 }
		 return true;
		 
	 }
	 /**
		 * 自定义一个消息提示窗口
		 * @param msg
		 */
		protected void showDialog(){
			 AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			 localBuilder.setTitle("注销").setMessage("是否注销用户").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						//停止下载服务
						Toast.makeText(BaseActivity.this, "发请求注销", Toast.LENGTH_LONG).show();
						BaseActivity.this.startActivity(new Intent(BaseActivity.this,LoginActivity.class));
						BaseActivity.this.finish();
						
					}                      
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}                      
				});
			 localBuilder.create().show();
		}
		protected void startSettingActivity(){
			Intent intent = new Intent(this,SettingActivity.class);
			intent.putExtra("username", username);
			this.startActivity(intent);
		};
}

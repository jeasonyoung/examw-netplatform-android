package com.examw.netschool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RegSuccessActivity extends Activity{
	private Button btn;
	private TextView txt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_regsuccess); 
		txt = (TextView) this.findViewById(R.id.username);
		btn = (Button) this.findViewById(R.id.ReLoginBtn);
		Intent intent = this.getIntent();
		txt.setText(intent.getStringExtra("username"));
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RegSuccessActivity.this.startActivity(new Intent(RegSuccessActivity.this,LoginActivity.class));
				RegSuccessActivity.this.finish();
			}
		});
	}
}
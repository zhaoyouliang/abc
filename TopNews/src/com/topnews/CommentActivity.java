package com.topnews;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.topnews.base.BaseActivity;

public class CommentActivity extends BaseActivity {
	private Button submmit;
	private EditText content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment);
		initView();
	}

	void initView() {
		submmit = (Button) findViewById(R.id.bt_submit);
		content = (EditText) findViewById(R.id.edit_comment);
		submmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String string = content.getText().toString();
				setResult(RESULT_OK);
				finish();
			}
		});
	}
}

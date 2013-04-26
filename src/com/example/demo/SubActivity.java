package com.example.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SubActivity extends MainActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Button subActivityButton = (Button) findViewById( R.id.sub_activity_button );
		subActivityButton.setVisibility( View.INVISIBLE );
		setTitle( R.string.sub_activity );
	}

}

package com.example.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.common.DemoService;

public class MainActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );

		Button sync_button = (Button) findViewById( R.id.sync_button );
		sync_button.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startService( new Intent( DemoService.SYNC ) );
			}
		} );

		Button scan_button = (Button) findViewById( R.id.scan_button );
		scan_button.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startService( new Intent( DemoService.SCAN ) );
			}
		} );
	}
}

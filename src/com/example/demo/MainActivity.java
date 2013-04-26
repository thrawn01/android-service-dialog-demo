package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
	DemoServiceClient mClient = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main );
		mClient = new DemoServiceClient( this, new OnCompleteInterface()
		{
			@Override
			public void onComplete(int count, String who)
			{
				new AlertDialog.Builder( MainActivity.this )
						.setTitle( getString( R.string.service_finished ) )
						.setMessage( String.format( getString( R.string.service_task ), who ) )
						.setPositiveButton( getString( R.string.ok ), null )
						.show();
			}
		} );

		Button syncButton = (Button) findViewById( R.id.sync_button );
		syncButton.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mClient.start( DemoService.SYNC );
			}
		} );

		Button scanButton = (Button) findViewById( R.id.scan_button );
		scanButton.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mClient.start( DemoService.SCAN );
			}
		} );

		Button subActivityButton = (Button) findViewById( R.id.sub_activity_button );
		subActivityButton.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivity( new Intent( MainActivity.this, SubActivity.class ) );
			}
		} );
	}

	@Override
	public void onResume()
	{
		Log.e( "DEMO", "onResume()" );
		super.onResume();
		mClient.show();
	}

	@Override
	public void onPause()
	{
		Log.e( "DEMO", "onPause()" );
		super.onPause();
		mClient.dismiss();
	}

	@Override
	public void onDestroy()
	{
		Log.e( "DEMO", "onDestroy()" );
		super.onDestroy();
	}
}

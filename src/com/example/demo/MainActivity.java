package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

		// Create the service client, this will also re-create the progress dialog
		// on orientation change
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

		// -------------------------------------
		// Setup our Buttons and Actions
		// -------------------------------------

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

		Button runBoth = (Button) findViewById( R.id.run_both_button );
		runBoth.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mClient.start( DemoService.BOTH );
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
	public void onPause()
	{
		super.onPause();
		mClient.dismiss();
	}
}

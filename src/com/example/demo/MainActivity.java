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
						.setTitle( getString(R.string.service_finished) )
						.setMessage( String.format(getString(R.string.service_task), who) )
						.setPositiveButton( getString( R.string.ok ), null )
						.show();
			}
		} );

		Button sync_button = (Button) findViewById( R.id.sync_button );
		sync_button.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.e( "DEMO", "SCAN Button Pressed" );
				mClient.start(DemoService.SYNC);
			}
		} );

		Button scan_button = (Button) findViewById( R.id.scan_button );
		scan_button.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.e( "DEMO", "SCAN Button Pressed" );
				mClient.start(DemoService.SYNC);
			}
		} );

		/*// If this activity was restarted by orientation change
		// we need to reconnect with the service and display the dialog again
		DemoServiceClient.resumeWith( this, new OnCompleteInterface()
		{
			@Override
			public void onComplete(int count, String who)
			{
				new AlertDialog.Builder( getApplicationContext() )
						.setTitle( getString(R.string.service_finished) )
						.setMessage( String.format(getString(R.string.service_task), who) )
						.setPositiveButton( getString( R.string.ok ), null )
						.show();
			}
		});*/
	}

	@Override
	public void onStop()
	{
		super.onStop();
		mClient.dismiss();
	}

}

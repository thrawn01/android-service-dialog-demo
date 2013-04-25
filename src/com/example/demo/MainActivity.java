package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.common.DemoService;
import com.example.common.DemoServiceClient;
import com.example.common.OnCompleteInterface;

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

		// If this activity was restarted by orientation change
		// we need to reconnect with the service and display the dialog again
		DemoServiceClient.resumeWith( this, new OnCompleteInterface()
		{
			@Override
			public void onComplete(int count, String who)
			{
				new AlertDialog.Builder( getApplicationContext() )
						.setTitle( getString(R.string.service_finished) )
						.setMessage( String.format(getString(R.string.service_task), who) )
						.setPositiveButton( getString(R.string.ok), null)
						.show();
			}
		});
	}

	@Override
	public void onStop()
	{
		DemoServiceClient.dismiss();
	}

}

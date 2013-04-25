package com.example.common;

import android.app.IntentService;
import android.content.Intent;
import com.google.common.collect.ImmutableList;

public class DemoService extends IntentService
{
	public static final String SCAN = "DEMO_SCAN";
	public static final String SYNC = "DEMO_SYNC";

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public DemoService()
	{
		super( "DemoService" );
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		String action = intent.getAction();
		if ( action.equals( SYNC ) ) {
			sync();
		}
		if ( action.equals( SCAN ) ) {
			scan();
		}
	}

	private void sync()
	{
		doWhile( ImmutableList.<String>of( "sync-one", "sync-two", "sync-three" ) );
	}

	private void scan()
	{
		doWhile( ImmutableList.<String>of( "scan-one", "scan-two", "scan-three" ) );
	}

	private void doWhile(ImmutableList<String> files)
	{
		for ( String file : files ) {
			// TODO: Some Notifications here
			sleep();
		}
	}

	/**
	 *  Normally we would do some work here, like download a file.
	 * For our demo, we just sleep for 5 seconds.
	 */
	private void sleep()
	{
		long endTime = System.currentTimeMillis() + 5 * 1000;
		while ( System.currentTimeMillis() < endTime ) {
			synchronized ( this ) {
				try {
					wait( endTime - System.currentTimeMillis() );
				} catch ( Exception e ) {
				}
			}
		}
	}
}

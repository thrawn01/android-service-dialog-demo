package com.example.common;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import com.google.common.collect.ImmutableList;


public class DemoService extends IntentService
{
	public static final String SCAN = "DEMO_SCAN";
	public static final String SYNC = "DEMO_SYNC";
	public static final int PROGRESS_STRING = 0x01;
	public static final int ON_COMPLETE = 0x02;
	public static final int REGISTER_CLIENT = 0x03;
	public static final int UNREGISTER_CLIENT = 0x04;

	Messenger mMessengerSender = null;
	Messenger mMessengerReceiver = null;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public DemoService()
	{
		super( "DemoService" );
		mMessengerReceiver = new Messenger( new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch ( msg.what ) {
					case REGISTER_CLIENT:
						mMessengerSender = msg.replyTo;
						break;
					case UNREGISTER_CLIENT:
						mMessengerSender = null;
						break;
				}
			}
		} );
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
			Log.e( "DEBUG", "Sync Started" );
			sync();
		}
		if ( action.equals( SCAN ) ) {
			Log.e( "DEBUG", "Scan Started" );
			scan();
		}
	}

	private void sync()
	{
		doWhile( ImmutableList.<String>of( "sync-one", "sync-two", "sync-three" ) );
		send( ON_COMPLETE, 0, SYNC );
	}

	private void scan()
	{
		doWhile( ImmutableList.<String>of( "scan-one", "scan-two", "scan-three" ) );
		send( ON_COMPLETE, 0, SCAN );
	}

	private void doWhile(ImmutableList<String> files)
	{
		for ( String file : files ) {
			send( PROGRESS_STRING, 0, file );
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

	@Override
	public IBinder onBind(Intent intent)
	{
		return mMessengerReceiver.getBinder();
	}

	private void send(int type, int arg1, String message)
	{
		try {
			if ( mMessengerSender != null ) {
				Message msg = Message.obtain( null, type, message );
				msg.arg1 = arg1;
				mMessengerSender.send( msg );
			}
		} catch ( RemoteException e ) {
			// Do nothing
		}
	}
}

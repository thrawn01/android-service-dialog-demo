package com.example.demo;

import android.app.IntentService;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import com.google.common.collect.ImmutableList;


public class DemoService extends IntentService
{
	public static final String SCAN = "com.example.demo.action.SCAN";
	public static final String SYNC = "com.example.demo.action.SYNC";
	public static final int PROGRESS_STRING = 0x01;
	public static final int ON_COMPLETE = 0x02;
	public static final int REGISTER_CLIENT = 0x03;
	public static final int UNREGISTER_CLIENT = 0x04;

	Messenger mMessengerSender = null;
	private Message mLastMessage = null;

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public DemoService()
	{
		super( "DemoService" );
	}

	private void resendLastMessage()
	{
		Log.e( "DEMO", "resendLastMessage()" );
		if ( mLastMessage != null ) {
			try {
				Log.e( "DEMO", "Sending last Message" );
				mMessengerSender.send( mLastMessage );
			} catch ( RemoteException e ) {
				// Do nothing
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		onStart( intent, startId );
		return START_STICKY;
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		if ( intent == null )
			return;

		Log.e( "DEMO", "onHandleIntent()" );
		String action = intent.getAction();
		if ( action.equals( SYNC ) ) {
			Log.e( "DEMO", "Sync Started" );
			sync();
		}
		if ( action.equals( SCAN ) ) {
			Log.e( "DEMO", "Scan Started" );
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
		Messenger mMessengerReceiver = new Messenger( new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch ( msg.what ) {
					case REGISTER_CLIENT:
						Log.e( "DEMO", "REGISTER_CLIENT" );
						mMessengerSender = msg.replyTo;
						resendLastMessage();
						break;
					case UNREGISTER_CLIENT:
						Log.e( "DEMO", "UNREGISTER_CLIENT" );
						mMessengerSender = null;
						break;
				}
			}
		} );
		return mMessengerReceiver.getBinder();
	}

	private void send(int type, int arg1, String message)
	{
		try {
			Log.e( "DEMO", "Sending Message " + message );
			mLastMessage = Message.obtain( null, type, message );
			mLastMessage.arg1 = arg1;

			if ( mMessengerSender != null ) {
				mMessengerSender.send( mLastMessage );
			}
		} catch ( RemoteException e ) {
			Log.e( "DEMO", "Failed to send - " + message );
			// Do nothing
		}
	}
}

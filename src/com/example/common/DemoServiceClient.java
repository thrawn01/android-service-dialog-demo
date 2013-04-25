package com.example.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.*;
import com.example.demo.R;

/**
 * Client that manages the progress dialog and communication with the service
 */
public class DemoServiceClient
{
	private static ProgressDialog mProgressDialog = null;
	private static OnCompleteInterface mCallBack = null;
	private Messenger mMessengerReceiver = null;
	private Messenger mMessengerSender = null;
	ServiceConnection mConnection = null;

	public DemoServiceClient() {
		mConnection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				try {
					mMessengerSender = new Messenger( service );
					Message msg = Message.obtain(null, DemoService.REGISTER_CLIENT);
					msg.replyTo = mMessengerReceiver;
					mMessengerSender.send( msg );
				} catch (RemoteException e) {
					// In this case the service has crashed before we could even
					// do anything with it; we can count on soon being
					// disconnected (and then reconnected if it can be restarted)
					// so there is no need to do anything here.
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName)
			{
				try {
					Message msg = Message.obtain(null, DemoService.UNREGISTER_CLIENT);
					msg.replyTo = mMessengerReceiver;
					mMessengerSender.send( msg );
				} catch (RemoteException e) {
					// In this case the service has crashed before we could even
					// do anything with it; we can count on soon being
					// disconnected (and then reconnected if it can be restarted)
					// so there is no need to do anything here.
				}
			}
		};

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what) {
					case DemoService.PROGRESS_STRING:
						mProgressDialog.setMessage( (String) msg.obj );
						break;
					case DemoService.ON_COMPLETE:
						mCallBack.onComplete( msg.arg1, (String) msg.obj );
						break;
				}
			}
		};
		mMessengerReceiver = new Messenger(handler);
	}

	/**
	 * Call this from your Activity onCreate() method, to redisplay the progress dialog
	 * during orientation changes, will only show dialog if the service is running
	 */
	public static void resumeWith(Activity activity, OnCompleteInterface _callBack)
	{
		if ( isServiceRunning() ) {
			init( activity, _callBack );
		}
	}

	/**
	 * Show the progress dialog and set our onComplete callback
	 */
	public synchronized static void init(Activity activity, OnCompleteInterface _callBack)
	{
		mProgressDialog = new ProgressDialog( activity );
		mProgressDialog.setCancelable( false );
		mProgressDialog.setTitle( R.string.service_running );
		mProgressDialog.setIndeterminate( true );
		mProgressDialog.show();
		mCallBack = _callBack;
	}

	/**
	 * Call this from your Activity onStop() method, so the resumeWith() method works correctly
	 */
	public synchronized static void dismiss()
	{
		if ( mProgressDialog != null ) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
		mCallBack = null;
	}

	private static boolean isServiceRunning()
	{
		return false;
	}

}

package com.example.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;

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
	private Activity mActivity;

	public DemoServiceClient(Activity _activity, OnCompleteInterface _callBack) {
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
						Log.e( "DEMO", "Client - Progress Message " + msg.obj );
						mProgressDialog.setMessage( (String) msg.obj );
						break;
					case DemoService.ON_COMPLETE:
						Log.e( "DEMO", "Client - On Complete " + msg.obj );
						mCallBack.onComplete( msg.arg1, (String) msg.obj );
						dismiss();
						break;
				}
			}
		};
		mMessengerReceiver = new Messenger(handler);
		mActivity = _activity;
		mCallBack = _callBack;

		if ( isServiceRunning() ) {
			showProgress();
			_activity.bindService( new Intent( _activity, DemoService.class ), mConnection, 0 );
		}
	}

	private void showProgress() {
		mProgressDialog = new ProgressDialog( mActivity );
		mProgressDialog.setCancelable( false );
		mProgressDialog.setTitle( R.string.service_running );
		mProgressDialog.setIndeterminate( true );
		mProgressDialog.show();
	}

	/**
	 * Call this from your Activity onStop() method, so the resumeWith() method works correctly
	 */
	public  void dismiss()
	{
		if ( mProgressDialog != null ) {
			mProgressDialog.dismiss();
		}
		if ( mConnection != null ) {
			mActivity.unbindService( mConnection );
		}
		mProgressDialog = null;
		mConnection = null;
		mCallBack = null;
	}

	private boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) mActivity.getSystemService( Context.ACTIVITY_SERVICE );
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (DemoService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void start(String job)
	{
		mActivity.startService( new Intent( job ) );
		mActivity.bindService( new Intent( mActivity, DemoService.class ), mConnection, 0 );
		showProgress();
	}
}

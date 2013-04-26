package com.example.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * Client that manages the progress dialog and communication with the service
 */
public class DemoServiceClient
{
	private ProgressDialog mProgressDialog = null;
	private OnCompleteInterface mCallBack = null;
	private ServiceConnection mConnection = null;
	private DemoService.LocalBinder mBinder;
	private Activity mActivity;
	private Handler mHandler;

	public DemoServiceClient(Activity _activity, OnCompleteInterface _callBack)
	{

		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch ( msg.what ) {
					case DemoService.ON_PROGRESS:
						mProgressDialog.setMessage( (String) msg.obj );
						break;
					case DemoService.ON_COMPLETE:
						mCallBack.onComplete( msg.arg1, (String) msg.obj );
						dismiss();
						break;
				}
			}
		};
		mActivity = _activity;
		mCallBack = _callBack;
		show();
	}

	private void showProgress()
	{
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog( mActivity );
			mProgressDialog.setCancelable( false );
			mProgressDialog.setTitle( R.string.service_running );
			mProgressDialog.setIndeterminate( true );
		}
		mProgressDialog.show();
	}

	public void show() {
		if ( isServiceRunning() ) {
			showProgress();
			connectToService();
		}
	}

	/**
	 * Call this from your Activity onStop() method, so the resumeWith() method works correctly
	 */
	public void dismiss()
	{
		if ( mProgressDialog != null ) {
			mProgressDialog.dismiss();
		}
		if ( mConnection != null ) {
			mActivity.unbindService( mConnection );
			mBinder.clearHandler();
		}
		mProgressDialog = null;
		mConnection = null;
		mBinder = null;
	}

	private boolean isServiceRunning()
	{
		ActivityManager manager = (ActivityManager) mActivity.getSystemService( Context.ACTIVITY_SERVICE );
		for ( ActivityManager.RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) ) {
			if ( DemoService.class.getName().equals( service.service.getClassName() ) ) {
				return true;
			}
		}
		return false;
	}

	public void start(String job)
	{
		mActivity.startService( new Intent( job ) );
		showProgress();
		connectToService();
	}

	private void connectToService()
	{
		if (mConnection != null ) {
			return;
		}
		mConnection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				mBinder = (DemoService.LocalBinder) service;
				mBinder.setHandler( mHandler );
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName)
			{
				mBinder.clearHandler();
				mBinder = null;
			}
		};
		mActivity.bindService( new Intent( mActivity, DemoService.class ), mConnection, 0 );
	}
}
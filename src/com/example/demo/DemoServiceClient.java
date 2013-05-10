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

	/**
	 * Manages the progress bar and connection to the running service
	 *
	 * Always call this constructor in the onCreate() of your Activity, it
	 * will re-display the progress dialog on orientation change
	 */
	public DemoServiceClient(Activity _activity, OnCompleteInterface _callBack)
	{

		// Create a handler the service will use to communicate with us
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch ( msg.what ) {
					// Message sent when progress changes
					case DemoService.ON_PROGRESS:
						if ( mProgressDialog != null )
							mProgressDialog.setMessage( (String) msg.obj );
						break;
					// Message sent when task is complete
					case DemoService.ON_COMPLETE:
						if ( mCallBack != null )
							mCallBack.onComplete( msg.arg1, (String) msg.obj );
						dismiss();
						break;
					// Message sent when service switches tasks
					case DemoService.ON_TASK_CHANGE:
						setTitle( mBinder.getTask() );
						break;
				}
			}
		};
		mActivity = _activity;
		mCallBack = _callBack;

		// If the service is running
		if ( isServiceRunning() ) {
			// Re-connect so we can receive progress messages
			connectToService();
			showProgress();
		}
	}

	/**
	 * Create and show the progress dialog
	 */
	private void showProgress()
	{
		if ( mProgressDialog == null ) {
			mProgressDialog = new ProgressDialog( mActivity );
			mProgressDialog.setCancelable( false );
			mProgressDialog.setTitle( R.string.service_running );
			mProgressDialog.setIndeterminate( true );
		}
		mProgressDialog.show();
	}

	/**
	 * Call this from your Activity's onPause() method
	 */
	public void dismiss()
	{
		if ( mProgressDialog != null ) mProgressDialog.dismiss();
		if ( mConnection != null ) mActivity.unbindService( mConnection );
		if ( mBinder != null ) mBinder.clearHandler();
		mProgressDialog = null;
		mConnection = null;
		mBinder = null;
	}

	/**
	 * Asks android if the service is running
	 */
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

	/**
	 * Queues up a task for the service to run, starts the service if its not already running
	 */
	public void start(String job)
	{
		mActivity.startService( new Intent( job ) );
		connectToService();
		showProgress();
	}

	/**
	 * Connects to an already running service
	 */
	private void connectToService()
	{
		// If a connection already exists, return
		if ( mConnection != null ) {
			return;
		}

		mConnection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				// Get an instance of the Binder and cast it to something we know about
				mBinder = (DemoService.LocalBinder) service;
				// Tell the binder how to pass messages to us
				mBinder.setHandler( mHandler );
				// Change the title of progress bar to the task the service is preforming
				setTitle( mBinder.getTask() );
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName)
			{
				// onServiceDisconnected is not always called on disconnect, but if it is
				mBinder.clearHandler();
				mBinder = null;
			}
		};
		// Pass 0 as the last argument to bindService so we don't create the service on connect this allows
		// bindService to fail if the service doesn't exist when we attempt to connect
		mActivity.bindService( new Intent( mActivity, DemoService.class ), mConnection, 0 );
	}

	/**
	 * Sets the title of our progress dialog to the task the service is currently preforming
	 */
	private void setTitle(String title)
	{
		if ( title == DemoService.SYNC ) {
			title = mActivity.getString( R.string.sync_running );
		} else {
			title = mActivity.getString( R.string.scan_running );
		}
		if ( mProgressDialog != null ) {
			mProgressDialog.setTitle( title );
		}
	}
}